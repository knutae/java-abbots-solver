const assert = require("assert");
const Board = require("../js/board");

describe('Board', function() {
    describe("constructor", function() {
        it("should create outer walls", function() {
            var b = new Board(2, 2);
            assert.equal(b.hasHorizonalWall(0, 0), true);
            assert.equal(b.hasHorizonalWall(0, 2), true);
            assert.equal(b.hasHorizonalWall(1, 0), true);
            assert.equal(b.hasHorizonalWall(1, 2), true);
            assert.equal(b.hasVerticalWall(0, 0), true);
            assert.equal(b.hasVerticalWall(2, 0), true);
            assert.equal(b.hasVerticalWall(0, 1), true);
            assert.equal(b.hasVerticalWall(2, 1), true);
        });
        it("should not create inner walls", function() {
            var b = new Board(2, 2);
            assert.notEqual(b.hasHorizonalWall(0, 1), true);
            assert.notEqual(b.hasHorizonalWall(1, 1), true);
            assert.notEqual(b.hasVerticalWall(1, 0), true);
            assert.notEqual(b.hasVerticalWall(1, 1), true);
        });
    });
    describe("#move", function() {
        it("should return old and new position", function() {
            var b = new Board(3, 3);
            b.setAbbot('r', 1, 1);
            var oldPos, newPos;
            [oldPos, newPos] = b.move('r', 'right');
            assert.deepEqual(oldPos, [1, 1]);
            assert.deepEqual(newPos, [2, 1]);
        });
        it("should stop at outer walls", function() {
            var b = new Board(3, 3);
            b.setAbbot('r', 1, 1);
            assert.deepEqual(b.abbots['r'], [1, 1]);
            b.move('r', 'left');
            assert.deepEqual(b.abbots['r'], [0, 1]);
            b.move('r', 'down');
            assert.deepEqual(b.abbots['r'], [0, 2]);
            b.move('r', 'right');
            assert.deepEqual(b.abbots['r'], [2, 2]);
            b.move('r', 'up');
            assert.deepEqual(b.abbots['r'], [2, 0]);
        });
        it("should stop at other abbots", function() {
            var b = new Board(4, 4);
            b.setAbbot('r', 0, 0);
            b.setAbbot('a', 0, 3);
            b.setAbbot('b', 3, 2);
            b.setAbbot('c', 2, 0);
            b.move('r', 'down');
            assert.deepEqual(b.abbots['r'], [0, 2]);
            b.move('r', 'right');
            assert.deepEqual(b.abbots['r'], [2, 2]);
            b.move('r', 'up');
            assert.deepEqual(b.abbots['r'], [2, 1]);
        });
    });
    describe("#parse", function() {
        var b;
        beforeEach(function() {
            b = Board.parse(`
+-+-+-+
|r    |
+-+ +-+
|R b| |
+-+-+-+`);
});
        it("should parse dimensions", function() {
            assert.equal(3, b.width);
            assert.equal(2, b.height);
        });
        it("should parse vertical walls", function() {
            assert.equal(b.hasVerticalWall(0, 0), true);
            assert.equal(b.hasVerticalWall(0, 1), true);
            assert.notEqual(b.hasVerticalWall(1, 0), true);
            assert.notEqual(b.hasVerticalWall(1, 1), true);
            assert.notEqual(b.hasVerticalWall(2, 0), true);
            assert.equal(b.hasVerticalWall(2, 1), true);
            assert.equal(b.hasVerticalWall(3, 0), true);
            assert.equal(b.hasVerticalWall(3, 1), true);
        });
        it("should parse horizontal walls", function() {
            assert.equal(b.hasHorizonalWall(0, 0), true);
            assert.equal(b.hasHorizonalWall(1, 0), true);
            assert.equal(b.hasHorizonalWall(2, 0), true);
            assert.equal(b.hasHorizonalWall(0, 1), true);
            assert.notEqual(b.hasHorizonalWall(1, 1), true);
            assert.equal(b.hasHorizonalWall(2, 1), true);
            assert.equal(b.hasHorizonalWall(0, 2), true);
            assert.equal(b.hasHorizonalWall(1, 2), true);
            assert.equal(b.hasHorizonalWall(2, 2), true);
        });
        it("should parse abbots and targets", function() {
            assert.deepEqual(b.abbots['r'], [0, 0]);
            assert.deepEqual(b.abbots['b'], [1, 1]);
            assert.deepEqual(b.targets['r'], [0, 1]);
        });
    });
});