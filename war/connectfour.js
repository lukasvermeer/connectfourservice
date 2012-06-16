// global static 
OFF_BOARD = 2;
BOARDSIZE = 7;
LOOK_AHEAD = 4;
WIN_SCORE = 9999;

// global objects
turn = 1;
blankBoard = new Array(Math.pow(BOARDSIZE, 2) + 1);
blankBoard[Math.pow(BOARDSIZE, 2)] = 9999;
currentBoard = new Board(BOARDSIZE, 0, blankBoard);
scoreCache = new Array();

jQuery(document).ajaxError(function(event, request, settings) {
	alert("There was a JSON error. Please reload the page to reset.");
});

function parseResponse(data) {
	if (data.response == "move") {
		playColumn(parseInt(data.move));
	} else if (data.response == "error") {
		$("#score").html(data.message);
	}
	else {
		alert("Unexpected JSON response.")
	}
}

function requestFirstMove() {
	$.getJSON("service", {
		newgame : true,
		width : 7,
		height : 7
	}, function(data) { parseResponse(data); } );
}

function sendNextMove(c) {
	$.getJSON("service", {
		move: c,
		state: currentBoard.getHash()
	}, function(data) { parseResponse(data); })
}

function detectGameEnd() {
	if (currentBoard.getScore(1) == WIN_SCORE) {
		$("#score").html('you win, game over');
		return true;
	}
	if (currentBoard.getScore(0) == WIN_SCORE) {
		$("#score").html('computer wins, game over');
		return true;
	}
	if (currentBoard.isFull()) {
		$("#score").html('no move possible, game over');
		return true;
	}
	return false;
}

function playColumn(c) {	
	if (!detectGameEnd()) {
		currentBoard.playColumn(c);
		$("#board tr td:nth-child(" + (c + 1) + "):not(.played):last")
				.addClass("played player" + (turn + 1)).data("player", turn);
		
		turn = Math.abs(turn - 1);
	}
	if (!detectGameEnd()) {
		if (turn == 1) {
			$("#score").html('computer thinking');
			sendNextMove(c);
		} else {
			$("#score").html('human thinking');
		}
	}
}

// Board object used to store game states
function Board(size, turn, state) {
	this.size = size;
	this.turn = turn;
	this.state = state;
}

Board.prototype.getHash = function() {
	return this.state.toString();
}

Board.prototype.playColumn = function(c) {
	if (this.getCell(0, c) == null) {
		var i = this.size - 1;
		while (this.getCell(i, c) != null) {
			i--;
		}
		this.state[i * this.size + c] = this.turn;

		this.turn = Math.abs(this.turn - 1);
		return true;
	} else {
		return false;
	}
};

Board.prototype.getCell = function(r, c) {
	if (r >= 0 && c >= 0 && r < this.size && c < this.size) {
		try {
			return (this.state[r * this.size + c]);
		} catch (err) {
			return null;
		}
	} else {
		return OFF_BOARD;
	}
};

Board.prototype.getScore = function(p) {
	if (scoreCache['' + p + this.getHash()] != null) {
		return scoreCache['' + p + this.getHash()];
	} else {
		var score = 0;
		for (i = 0; i < this.size; ++i) {
			for (j = 0; j < this.size - 4 + 1; ++j) {
				var line = new Array(6);
				for (k = 0; k < 6; ++k) {
					line[k] = [ 0, 0, 0 ];
				}

				for (n = j; n < j + 4; ++n) {
					line[0][this.getCell(n, i)]++; // columns
					line[1][this.getCell(i, n)]++; // rows
					line[2][this.getCell(n, n - i)]++; // diagonal southwest
					// half
					line[3][this.getCell(this.size - n - 1, n - i)]++; // diagonal
					// northwest
					// half
					if (i > 0) {
						line[4][this.getCell(n - i, n)]++; // diagonal
						// northweast half
						line[5][this.getCell(n + i, this.size - n - 1)]++; // diagonal
						// southeast
						// half
					}
				}

				for (k = 0; k < 6; ++k) {
					if (line[k][p] == 4)
						return WIN_SCORE; // win
					if (line[k][Math.abs(p - 1)] == 4)
						return -WIN_SCORE; // lose
					if (line[k][Math.abs(p - 1)] == 0
							&& line[k][OFF_BOARD] == 0) {
						score += Math.pow(line[k][p], 2);
					}
				}
			}
		}

		scoreCache['' + p + this.getHash()] = score;
		return score;
	}
};

Board.prototype.isFull = function() {
	for ( var n = 0; n < this.size ^ 2; ++n) {
		if (this.state[n] == null) {
			return false;
		}
	}
	return true;
};

//DOM manipulation for GUI
function buildBoard(size) {
	$("#board").append($("<table id='boardId'>").addClass("boardTable"));
	for (i = 0; i < size; ++i) {
		$(".boardTable").append($("<tr>").addClass("boardRow"));
		for (j = 0; j < size; ++j) {
			$(".boardRow:last").append(
					$("<td>").addClass("boardCell").data("column", j).click(
							function() {
								if (turn == 0) {
									playColumn(jQuery.data(this, "column"));
								}
							}));
		}
	}
}

$(document).ready(
		function() {
			buildBoard(BOARDSIZE);
			requestFirstMove();

			// jQuery + CCS magic for highlighting target column
			$("#boardId").delegate(
					'td',
					'mouseover mouseleave',
					function(e) {
						if (e.type == 'mouseover' && turn == 0 && !detectGameEnd()) {
							$(
									"#board tr td:nth-child("
											+ ($(this).index() + 1)
											+ "):not(.played)").addClass(
									"hover");
							$(
									"#board tr td:nth-child("
											+ ($(this).index() + 1)
											+ "):not(.played):last").addClass(
									"hover-target");
						} else {
							$(
									"#board tr td:nth-child("
											+ ($(this).index() + 1)
											+ "):not(.played)").removeClass(
									"hover");
							$(
									"#board tr td:nth-child("
											+ ($(this).index() + 1)
											+ "):not(.played):last")
									.removeClass("hover-target");
						}
					});			
		});