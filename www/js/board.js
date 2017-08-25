function create2dWallArray(d1, d2) {
    var arr = new Array(d1);
    for (var i = 0; i < d1; i++) {
        // first and last element is true by default (represents the outer walls)
        arr[i] = new Array(d2);
        arr[i][0] = true;
        arr[i][d2-1] = true;
    }
    return arr;
}

class Board {
    constructor(width, height) {
        this.width = width;
        this.height = height;
        this.horizontalWalls = create2dWallArray(width, height+1);
        this.verticalWalls = create2dWallArray(height, width+1);
        this.abbots = {};
        this.targets = {};
    }

    addHorizontalWall(x, y) {
        this.horizontalWalls[x][y] = true;
    }

    addVerticalWall(x, y) {
        this.verticalWalls[y][x] = true;
    }

    hasHorizonalWall(x, y) {
        return this.horizontalWalls[x][y];
    }

    hasVerticalWall(x, y) {
        return this.verticalWalls[y][x];
    }

    setAbbot(bot, x, y) {
        this.abbots[bot] = [x, y];
    }

    setTarget(bot, x, y) {
        this.targets[bot] = [x, y];
    }

    listAbbots() {
        var r = [];
        for (var key in this.abbots) {
            if (this.abbots.hasOwnProperty(key)) {
                r.push(key);
            }
        }
        return r;
    }

    listTargets() {
        var r = [];
        for (var key in this.targets) {
            if (this.targets.hasOwnProperty(key)) {
                r.push(key);
            }
        }
        return r;
    }

    abbotsAtRow(y) {
        var line = new Array(this.width);
        for (var abbot of this.listAbbots()) {
            var ax, ay;
            [ax, ay] = this.abbots[abbot];
            if (ay === y) {
                line[ax] = true;
            }
        }
        return line;
    }

    abbotsAtCol(x) {
        var line = new Array(this.height);
        for (var abbot of this.listAbbots()) {
            var ax, ay;
            [ax, ay] = this.abbots[abbot];
            if (ax === x) {
                line[ay] = true;
            }
        }
        return line;
    }

    move(abbot, direction) {
        var posx, posy;
        [posx, posy] = this.abbots[abbot];
        var otherAbbots, walls, i;
        // walls, other abbots and index in the correct axis:
        switch (direction) {
            case "up":
            case "down":
                otherAbbots = this.abbotsAtCol(posx);
                walls = this.horizontalWalls[posx];
                i = posy;
                break;
            case "left":
            case "right":
                otherAbbots = this.abbotsAtRow(posy);
                walls = this.verticalWalls[posy];
                i = posx;
                break;
        }
        // move: adjust index until a wall or other abbot is found
        switch (direction) {
            case "up":
            case "left":
                while (!walls[i] && !otherAbbots[i-1]) {
                    i--;
                }
                break;
            case "down":
            case "right":
                while (!walls[i+1] && !otherAbbots[i+1]) {
                    i++;
                }
                break;
        }
        // update abbot position
        switch (direction) {
            case "up":
            case "down":
                this.abbots[abbot] = [posx, i];
                break;
            case "left":
            case "right":
                this.abbots[abbot] = [i, posy];
                break;
        }
        // return old + new position
        return [[posx, posy], this.abbots[abbot]]
    }

    static parse(input) {
        const lines = input.trim().split('\n');
        if (lines.length < 3) {
            throw new Error('Too few lines');
        }
        const height = (lines.length-1) / 2;
        const width = lines[0].replace(/[+]/g, '').length;
        const b = new Board(width, height);
        // vertical walls
        for (let y = 0; y < height; y++) {
            const line = lines[y*2+1];
            for (let x = 0; x <= width; x++) {
                switch (line[x*2]) {
                    case ' ':
                        // no wall: do nothing
                        break;
                    case '|':
                        b.addVerticalWall(x, y);
                        break;
                    default:
                        throw new Error("Expected ' ' or '|', got " + line[x*2]);
                }
            }
        }
        // horizontal walls
        for (let y = 0; y <= height; y++) {
            const line = lines[y*2];
            for (let x = 0; x < width; x++) {
                switch (line[x*2+1]) {
                    case ' ':
                        // no wall: do nothing
                        break;
                    case '-':
                        b.addHorizontalWall(x, y);
                        break;
                    default:
                        throw new Error("Expected ' ' or '-', got " + line[x*2+1]);
                }
            }
        }
        // abbots and targets
        for (let y = 0; y < height; y++) {
            const line = lines[y*2+1];
            for (let x = 0; x < width; x++) {
                const c = line[x*2+1];
                if (c != c.toUpperCase()) {
                    // lower-case letter (abbot)
                    b.setAbbot(c, x, y);
                } else if (c != c.toLowerCase()) {
                    // upper-case letter (target)
                    b.setTarget(c.toLowerCase(), x, y);
                } else if (c != ' ') {
                    throw new Error("Expected letter or space, got " + c);
                }
            }
        }
        return b;
    }
}

if (typeof module !== 'undefined') module.exports = Board;
