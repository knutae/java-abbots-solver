var tilesize = 48;
var innertilesize = tilesize * 0.9;
var wallsize = 6;
var wallcolor = '#555';
var abbotColors = {
    'r': '#f44',
    'g': '#4f4',
    'b': '#44f',
    'y': '#ffff00',
}
var targetColors = {
    'r': '#f88',
    'g': '#8f8',
    'b': '#88f',
    'y': '#ff6',
}
var abbotsize = tilesize * 0.5;
var targetsize = innertilesize;
var activesize = 5;
var activecolor = "#555";

function draw_tile(x, y, color) {
    Crafty.e('2D, DOM, Color').attr({
        x: x * tilesize + wallsize/2 + (tilesize - innertilesize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - innertilesize)/2,
        w: innertilesize,
        h: innertilesize
    }).color(color);
}

function draw_horizontal_wall(x, y) {
    Crafty.e('2D, DOM, Color').attr({
        x: x * tilesize + wallsize/2,
        y: y * tilesize,
        w: tilesize,
        h: wallsize
    }).color(wallcolor);
}

function draw_vertical_wall(x, y) {
    Crafty.e('2D, DOM, Color').attr({
        x: x * tilesize,
        y: y * tilesize + wallsize/2,
        w: wallsize,
        h: tilesize
    }).color(wallcolor);
}

function draw_target(abbot, x, y) {
    Crafty.e('2D, DOM, Color').attr({
        x: x * tilesize + wallsize/2 + (tilesize - targetsize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - targetsize)/2,
        w: targetsize,
        h: targetsize
    }).color(targetColors[abbot]);
}

function draw_abbot(abbot, x, y) {
    Crafty.e('2D, DOM, Color, Tween, abbot:' + abbot).attr({
        x: x * tilesize + wallsize/2 + (tilesize - abbotsize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - abbotsize)/2,
        w: abbotsize,
        h: abbotsize,
        rotation: 45
    }).origin("center").color(abbotColors[abbot]);
}

function draw_active_indicator(x, y) {
    Crafty.e('2D, DOM, Color, Tween, activeAbbot').attr({
        x: x * tilesize + wallsize/2 + (tilesize - activesize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - activesize)/2,
        w: activesize,
        h: activesize,
        rotation: 45
    }).origin("center").color(activecolor);
}

function move_abbot_with_duration(abbot, x, y, duration, moveDoneFunc) {
    Crafty('abbot:' + abbot).tween({
        x: x * tilesize + wallsize/2 + (tilesize - abbotsize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - abbotsize)/2,
    }, duration, "smoothStep");
    Crafty('activeAbbot').tween({
        x: x * tilesize + wallsize/2 + (tilesize - activesize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - activesize)/2,
    }, duration, "smoothStep").one("TweenEnd", moveDoneFunc);
}

function move_abbot(abbot, x, y, moveDoneFunc) {
    move_abbot_with_duration(abbot, x, y, 150, moveDoneFunc);
}

function move_abbot_fast(abbot, x, y, moveDoneFunc) {
    move_abbot_with_duration(abbot, x, y, 100, moveDoneFunc);
}

function move_abbot_slow(abbot, x, y, moveDoneFunc) {
    move_abbot_with_duration(abbot, x, y, 300, moveDoneFunc);
}

function switch_abbot_indicator(x, y) {
    Crafty('activeAbbot').attr({
        x: x * tilesize + wallsize/2 + (tilesize - activesize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - activesize)/2,
    });
}

function draw_board(board) {
    // floor tiles
    for (var x = 0; x < board.width; x++) {
        for (var y = 0; y < board.height; y++) {
            draw_tile(x, y, '#ddd');
        }
    }
    // targets
    for (var target of board.listTargets()) {
        var x, y;
        [x, y] = board.targets[target];
        draw_target(target, x, y);
    }
    // horizontal walls
    for (var x = 0; x < board.width; x++) {
        for (var y = 0; y < board.height+1; y++) {
            if (board.hasHorizonalWall(x, y)) {
                draw_horizontal_wall(x, y);
            }
        }
    }
    // vertical walls
    for (var x = 0; x < board.width+1; x++) {
        for (var y = 0; y < board.height; y++) {
            if (board.hasVerticalWall(x, y)) {
                draw_vertical_wall(x, y);
            }
        }
    }
    // abbots
    for (var abbot of board.listAbbots()) {
        var x, y;
        [x, y] = board.abbots[abbot];
        draw_abbot(abbot, x, y);
    }
}

function _createCraftyElement(document) {
    const parentElement = document.getElementById('game');
    const craftyElement = document.createElement("div");
    parentElement.appendChild(craftyElement);
    return craftyElement;
}

class CraftyBoard {
    constructor(board, document) {
        this.board = board;
        this.document = document;
        this.craftyElement = _createCraftyElement(document);
        this.onmovedhandlers = [];
        this._onmove = () => {
            for (let handler of this.onmovedhandlers) {
                handler();
            }
        };
    }

    start() {
        const board = this.board;
        Crafty.init(board.width * tilesize + wallsize, board.height * tilesize + wallsize, this.craftyElement);
        draw_board(board);
        const otherAbbots = board.listAbbots();
        let activeAbbot = otherAbbots.shift();
        let activePos = board.abbots[activeAbbot];
        draw_active_indicator(activePos[0], activePos[1]);
        let awaitingKeyUp = false; // basic blocking of repeated KeyDown events
        Crafty('*').bind('KeyDown', e => {
            if (awaitingKeyUp) {
                return;
            }
            awaitingKeyUp = true;
            let direction;
            if (e.key == Crafty.keys.LEFT_ARROW) {
                direction = "left";
            } else if (e.key == Crafty.keys.RIGHT_ARROW) {
                direction = "right";
            } else if (e.key == Crafty.keys.UP_ARROW) {
                direction = "up";
            } else if (e.key == Crafty.keys.DOWN_ARROW) {
                direction = "down";
            } else if (e.key == Crafty.keys.Q) {
                otherAbbots.unshift(activeAbbot);
                activeAbbot = otherAbbots.pop();
                activePos = board.abbots[activeAbbot];
                switch_abbot_indicator(activePos[0], activePos[1]);
                //console.log("active abbot: " + activeAbbot);
                return;
            } else if (e.key == Crafty.keys.E) {
                otherAbbots.push(activeAbbot);
                activeAbbot = otherAbbots.shift();
                activePos = board.abbots[activeAbbot];
                switch_abbot_indicator(activePos[0], activePos[1]);
                //console.log("active abbot: " + activeAbbot);
                return;
            } else {
                return;
            }
            var oldPos, newPos;
            [oldPos, newPos] = board.move(activeAbbot, direction);
            if (oldPos[0] === newPos[0] && oldPos[1] === newPos[1]) {
                // no movement
                return;
            }
            move_abbot(activeAbbot, newPos[0], newPos[1], this._onmove);
        });
        Crafty('*').bind('KeyUp', function(e) {
            awaitingKeyUp = false;
        });
    }

    stop() {
        if (this.craftyElement) {
            Crafty.stop(true);
            this.craftyElement = null;
        }
    }

    onmoved(cb) {
        this.onmovedhandlers.push(cb);
    }

    canUndo() {
        return this.board.canUndo();
    }

    canRedo() {
        return this.board.canRedo();
    }

    undoAll() {
        const historyElement = this.board.undo();
        if (historyElement) {
            switch_abbot_indicator(historyElement.newPos[0], historyElement.newPos[1]);
            move_abbot_fast(historyElement.abbot, historyElement.oldPos[0], historyElement.oldPos[1], () => {
                this._onmove();
                this.undoAll();
            })
        } else {
            this._onmove();
        }
    }

    redoAll() {
        const historyElement = this.board.redo();
        if (historyElement) {
            switch_abbot_indicator(historyElement.oldPos[0], historyElement.oldPos[1]);
            move_abbot_slow(historyElement.abbot, historyElement.newPos[0], historyElement.newPos[1], () => {
                this._onmove();
                this.redoAll();
            });
        } else {
            this._onmove();
        }
    }

    undo() {
        const historyElement = this.board.undo();
        if (historyElement) {
            switch_abbot_indicator(historyElement.newPos[0], historyElement.newPos[1]);
            move_abbot(historyElement.abbot, historyElement.oldPos[0], historyElement.oldPos[1], this._onmove);
        }
    }

    redo() {
        const historyElement = this.board.redo();
        if (historyElement) {
            switch_abbot_indicator(historyElement.oldPos[0], historyElement.oldPos[1]);
            move_abbot(historyElement.abbot, historyElement.newPos[0], historyElement.newPos[1], this._onmove);
        }
    }
}

var _craftyBoard = null;

function init_board(board, document) {
    if (_craftyBoard) {
        _craftyBoard.stop();
    }
    _craftyBoard = new CraftyBoard(board, document);
    _craftyBoard.start();
    return _craftyBoard;
}

function _direction_char(c) {
    switch (c) {
        case "<": return "left";
        case ">": return "right";
        case "^": return "up";
        case ",": return "down";
        default: throw new Error("Not a valid direction: " + c);
    }
}

function init_board_with_solution(board, document, solution) {
    init_board(board, document);
    for (let i = 0; i < solution.length/2; i++) {
        const abbot = solution[i*2];
        const direction = _direction_char(solution[i*2+1]);
        board.move(abbot, direction);
    }
    while (board.canUndo()) {
        board.undo();
    }
    return _craftyBoard;
}
