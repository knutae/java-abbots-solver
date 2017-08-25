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

function move_abbot(abbot, x, y) {
    Crafty('abbot:' + abbot).tween({
        x: x * tilesize + wallsize/2 + (tilesize - abbotsize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - abbotsize)/2,
    }, 150, "smoothStep");
    Crafty('activeAbbot').tween({
        x: x * tilesize + wallsize/2 + (tilesize - activesize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - activesize)/2,
    }, 150, "smoothStep");
}

function switch_abbot_indicator(x, y) {
    Crafty('activeAbbot').attr({
        x: x * tilesize + wallsize/2 + (tilesize - activesize)/2,
        y: y * tilesize + wallsize/2 + (tilesize - activesize)/2,
    });
}

function draw_board(board, element) {
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

var crafty_initialized = false;

function init_board(board, element) {
    if (crafty_initialized) {
        // bah, init() does not seem to work after stop(),
        // and keyboard focus is buggy after calling init() more than once
        //Crafty.stop(true);
    }
    Crafty.init(board.width * tilesize + wallsize, board.height * tilesize + wallsize, element);
    crafty_initialized = true;
    draw_board(board, element);
    var otherAbbots = board.listAbbots();
    var activeAbbot = otherAbbots.shift();
    var activePos = board.abbots[activeAbbot];
    draw_active_indicator(activePos[0], activePos[1]);
    var awaitingKeyUp = false; // basic blocking of repeated KeyDown events
    Crafty('*').bind('KeyDown', function(e) {
        if (awaitingKeyUp) {
            return;
        }
        awaitingKeyUp = true;
        var direction;
        if (e.key == Crafty.keys.LEFT_ARROW) {
            direction = "left";
        } else if (e.key == Crafty.keys.RIGHT_ARROW) {
            direction = "right";
        } else if (e.key == Crafty.keys.UP_ARROW) {
            direction = "up";
        } else if (e.key == Crafty.keys.DOWN_ARROW) {
            direction = "down";
        } else if (e.key == Crafty.keys.SPACE) {
            otherAbbots.push(activeAbbot);
            activeAbbot = otherAbbots.shift();
            activePos = board.abbots[activeAbbot];
            switch_abbot_indicator(activePos[0], activePos[1]);
            //console.log("active abbot: " + activeAbbot);
        } else {
            return;
        }
        [oldPos, newPos] = board.move(activeAbbot, direction);
        if (oldPos[0] === newPos[0] && oldPos[1] === newPos[1]) {
            // no movement
            return;
        }
        move_abbot(activeAbbot, newPos[0], newPos[1]);
    });
    Crafty('*').bind('KeyUp', function(e) {
        awaitingKeyUp = false;
    });
}