package game;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import players.GAPlayer;
import players.MMPlayer;
import players.Population;
import utils.Pair;

@SuppressWarnings("serial")

public class CheckersCanvas extends Canvas implements ActionListener, MouseListener {

	// TODO Fix reading file every game
	// TODO Replace New Game button with start button
	// TODO Change fitness of losing(board state)

	// This canvas displays a 160-by-160 checkerboard pattern with
	// a 2-pixel black border. It is assumed that the size of the
	// canvas is set to exactly 164-by-164 pixels. This class does
	// the work of letting the users play checkers, and it displays
	// the checkerboard.

	Button resignButton; // Current player can resign by clicking this button.
	Button newGameButton; // This button starts a new game. It is enabled only
							// when the current game has ended.

	Checkbox check;

	Label message; // A label for displaying messages to the user.

	CheckersData board; // The data for the checkers board is kept here.
						// This board is also responsible for generating
						// lists of legal moves.

	boolean gameInProgress; // Is a game currently in progress?

	/* The next three variables are valid only when the game is in progress. */

	int currentPlayer; // Whose turn is it now? The possible values
						// are CheckersData.RED and CheckersData.BLACK.
	int selectedRow, selectedCol; // If the current player has selected a piece to
									// move, these give the row and column
									// containing that piece. If no piece is
									// yet selected, then selectedRow is -1.
	CheckersMove[] legalMoves; // An array containing the legal moves for the
								// current player.

	GAPlayer[] gap; // Genetic algorithm player.
	MMPlayer mmp; // Minimax algorithm player.
	int GAIndex = 0;

	int aniX, aniY;
	CheckersMove aniMove;

	int turns;

	int gen = 0;

	public CheckersCanvas() {
		// Constructor. Create the buttons and lable. Listen for mouse
		// clicks and for clicks on the buttons. Create the board and
		// start the first game.
		aniX = -1;
		aniY = -1;
		aniMove = null;
		setBackground(Color.black);
		addMouseListener(this);
		setFont(new Font("Serif", Font.BOLD, 14));
		resignButton = new Button("Resign");
		resignButton.addActionListener(this);
		newGameButton = new Button("New Game");
		newGameButton.addActionListener(this);
		check = new Checkbox("Use previous generations");
		message = new Label("", Label.CENTER);
		board = new CheckersData();

		gap = Population.newPop();

		mmp = new MMPlayer();

		doNewGame();
	}

	public void actionPerformed(ActionEvent evt) {
		// Respond to user's click on one of the two buttons.
		Object src = evt.getSource();
		if (src == newGameButton)
			doNewGame();
		else if (src == resignButton)
			doResign();
	}

	void doNewGame() {
		// Begin a new game.
		if (gameInProgress == true) {
			// This should not be possible, but it doens't
			// hurt to check.
			message.setText("Finish the current game first!");
			return;
		}
		board.setUpGame(); // Set up the pieces.
		currentPlayer = CheckersData.RED; // RED moves first.
		legalMoves = board.getLegalMoves(CheckersData.RED); // Get RED's legal moves.
		selectedRow = -1; // RED has not yet selected a piece to move.
		// message.setText("Red: Make your move.");
		message.setText("PLAYER[" + GAIndex + "]. PREVIOUS FITNESS: " + gap[GAIndex].fitness);
		gameInProgress = true;
		newGameButton.setEnabled(false);
		resignButton.setEnabled(true);
		turns = 0;
		if (check.getState()) {
			File file = new File("geninfo.txt");
			FileReader fr = null;
			try {
				fr = new FileReader(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (fr != null) {
				String s = null;
				String[] sPoints;
				BufferedReader bf = new BufferedReader(fr);

				for (int i = 0; i < Population.N; i++) {
					
					try {
						s = bf.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (s != null) {
						sPoints = s.split(" ");
						for (int j = 0; j < 5; j++) {
							gap[i].points[j] = Float.valueOf(sPoints[j]);
							
						}
					}
					System.out.printf("%f %f %f %f %f\n", gap[i].points[0], gap[i].points[1], gap[i].points[2], gap[i].points[3], gap[i].points[4]);
				}
			}
		}

		repaint();
	}

	void doResign() {
		// Current player resigns. Game ends. Opponent wins.
		if (gameInProgress == false) {
			message.setText("There is no game in progress!");
			return;
		}
		if (currentPlayer == CheckersData.RED)
			gameOver("RED resigns.  BLACK wins.", CheckersData.BLACK);
		else
			gameOver("BLACK resigns.  RED wins.", CheckersData.RED);
	}

	void gameOver(String str, int winner) {
		// The game ends. The parameter, str, is displayed as a message
		// to the user. The states of the buttons are adjusted so players
		// can start a new game.
		message.setText(str);
		newGameButton.setEnabled(true);
		resignButton.setEnabled(false);
		gameInProgress = false;

		if (winner == CheckersData.RED)
			gap[GAIndex].fitness = 500 - turns;
		else if (winner == CheckersData.EMPTY)
			gap[GAIndex].fitness = board.boardStateMM(CheckersData.RED) + 200;
		else
			gap[GAIndex].fitness = turns;

		if (GAIndex < 9) {
			GAIndex++;
			doNewGame();
			gap[GAIndex].play(legalMoves, this, board.getBoard());
		} else {
			Pair best = new Pair(0, gap[0].fitness);
			Pair worst[] = new Pair[2];

			if (gap[0].fitness < gap[1].fitness) {
				worst[0] = new Pair(0, gap[0].fitness);
				worst[1] = new Pair(1, gap[1].fitness);
			} else {
				worst[1] = new Pair(0, gap[0].fitness);
				worst[0] = new Pair(1, gap[1].fitness);
			}

			for (int i = 1; i < 10; i++) {
				if (best.second < gap[i].fitness) {
					best.first = i;
					best.second = gap[i].fitness;
				}

				if (worst[1].second > gap[i].fitness) {
					if (worst[0].second > gap[i].fitness) {
						worst[0].first = i;
						worst[0].second = gap[i].fitness;
					} else {
						worst[1].first = i;
						worst[1].second = gap[i].fitness;
					}
				}

			}

			gap = Population.crossOver(gap, best.first);
			gap[worst[0].first] = Population.newGAPlayer();
			gap[worst[1].first] = Population.newGAPlayer();
			gen++;

			System.out.println("GENERATION " + gen + ": ");
			System.out.println("BEST: " + best.first);
			System.out.printf("WORST WERE: %d [%d] - %d [%d]\n", worst[0].first, worst[0].second, worst[1].first,
					worst[1].second);
			Population.printPop(gap);

			GAIndex = 0;

			if (gen < 5)
				doNewGame();
			else
				try {
					writeGenInfo();
				} catch (IOException e) {
					// TODO: handle exception
				}

		}

	}

	private void writeGenInfo() throws IOException {
		System.out.println("WRITING");
		File file = new File("geninfo.txt");
		FileWriter fw = null;

		fw = new FileWriter(file);

		if (fw != null) {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 5; j++) {
					sb.append(String.valueOf(gap[i].points[j]));
					sb.append(" ");
				}
				sb.append("\n");
			}
			System.out.println(sb.toString());
			fw.write(sb.toString());

			fw.close();

		}
	}

	void doClickSquare(int row, int col) {
		// This is called by mousePressed() when a player clicks on the
		// square in the specified row and col. It has already been checked
		// that a game is, in fact, in progress.

		/*
		 * If the player clicked on one of the pieces that the player can move, mark
		 * this row and col as selected and return. (This might change a previous
		 * selection.) Reset the message, in case it was previously displaying an error
		 * message.
		 */

		for (int i = 0; i < legalMoves.length; i++)
			if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
				selectedRow = row;
				selectedCol = col;
				/*
				 * if (currentPlayer == CheckersData.RED)
				 * //message.setText("RED:  Make your move."); else
				 * //message.setText("BLACK:  Make your move.");
				 */

				repaint();
				return;
			}

		/*
		 * If no piece has been selected to be moved, the user must first select a
		 * piece. Show an error message and return.
		 */

		if (selectedRow < 0) {
			// message.setText("Click the piece you want to move.");
			return;
		}

		/*
		 * If the user clicked on a square where the selected piece can be legally
		 * moved, then make the move and return.
		 */

		for (int i = 0; i < legalMoves.length; i++)
			if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
					&& legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
				doMakeMove(legalMoves[i]);
				return;
			}

		/*
		 * If we get to this point, there is a piece selected, and the square where the
		 * user just clicked is not one where that piece can be legally moved. Show an
		 * error message.
		 */

		// message.setText("Click the square you want to move to.");

	} // end doClickSquare()

	public void doMakeMove(CheckersMove move) {
		// This is called when the current player has chosen the specified
		// move. Make the move, and then either end or continue the game
		// appropriately.
		if (turns > 150) {
			gameOver("DRAW!", CheckersData.EMPTY);
			return;
		} else
			turns++;

		aniMove = move;

		board.makeMove(move);

		aniY = 4 + move.fromRow * 20;
		aniX = 4 + move.fromCol * 20;

		boolean flag1 = true;
		boolean flag2 = true;
		boolean flag3 = true;

		if (move.fromRow < move.toRow) {
			for (aniY = 4 + move.fromRow * 20; aniY < 4 + move.toRow * 20; aniY++) {
				paint(this.getGraphics());
				if (move.fromCol < move.toCol)
					aniX++;
				else
					aniX--;

				if (flag1 && flag2 && flag3) {
					aniY--;
					if (move.fromCol < move.toCol)
						aniX--;
					else
						aniX++;
					flag1 = false;
					flag2 = false;
					flag3 = false;
				} else if (!flag1)
					flag1 = true;
				else if (!flag2)
					flag2 = true;
				else
					flag3 = true;
			}
		} else {
			for (aniY = 4 + move.fromRow * 20; aniY > 4 + move.toRow * 20; aniY--) {
				paint(this.getGraphics());
				if (move.fromCol < move.toCol)
					aniX++;
				else
					aniX--;

				if (flag1 && flag2) {
					aniY++;
					if (move.fromCol < move.toCol)
						aniX--;
					else
						aniX++;
					flag1 = false;
					flag2 = false;
					flag3 = false;
				} else if (!flag1)
					flag1 = true;
				else if (!flag2)
					flag2 = true;
				else
					flag3 = true;
			}
		}
		aniMove = null;

		/*
		 * If the move was a jump, it's possible that the player has another jump. Check
		 * for legal jumps starting from the square that the player just moved to. If
		 * there are any, the player must jump. The same player continues moving.
		 */

		if (move.isJump()) {
			legalMoves = board.getLegalJumpsFrom(currentPlayer, move.toRow, move.toCol);
			if (legalMoves != null) {
				if (currentPlayer == CheckersData.RED) {
					// message.setText("RED: You must continue jumping.");
					gap[GAIndex].play(legalMoves, this, board.getBoard());
				} else {
					// message.setText("BLACK: You must continue jumping.");
					// gap.play(legalMoves, this);
					mmp.play(legalMoves, this, board.getBoard());
				}
				selectedRow = move.toRow; // Since only one piece can be moved, select it.
				selectedCol = move.toCol;
				repaint();
				return;
			}
		}

		/*
		 * The current player's turn is ended, so change to the other player. Get that
		 * player's legal moves. If the player has no legal moves, then the game ends.
		 */

		if (currentPlayer == CheckersData.RED) {
			currentPlayer = CheckersData.BLACK;

			boolean fl = true;
			legalMoves = board.getLegalMoves(currentPlayer);
			if (legalMoves == null) {
				gameOver("BLACK has no moves.  RED wins.", CheckersData.RED);
				fl = false;
				gap[GAIndex].play(legalMoves, this, board.getBoard());
			}
			/*
			 * else if (legalMoves[0].isJump())
			 * message.setText("BLACK:  Make your move.  You must jump."); else
			 * message.setText("BLACK:  Make your move.");
			 */

			// gap.play(legalMoves, this)
			if (fl)
				mmp.play(legalMoves, this, board.getBoard());
		} else {
			currentPlayer = CheckersData.RED;
			legalMoves = board.getLegalMoves(currentPlayer);

			boolean fl = true;
			if (legalMoves == null) {
				gameOver("RED has no moves.  BLACK wins.", CheckersData.BLACK);
				fl = false;
				gap[GAIndex].play(legalMoves, this, board.getBoard());
			}
			/*
			 * else if (legalMoves[0].isJump())
			 * message.setText("RED:  Make your move.  You must jump."); else
			 * message.setText("RED:  Make your move.");
			 */

			if (fl)
				gap[GAIndex].play(legalMoves, this, board.getBoard());
		}

		/*
		 * Set selectedRow = -1 to record that the player has not yet selected a piece
		 * to move.
		 */
		selectedRow = -1;

		/*
		 * As a courtesy to the user, if all legal moves use the same piece, then select
		 * that piece automatically so the use won't have to click on it to select it.
		 */

		if (legalMoves != null) {
			boolean sameStartSquare = true;
			for (int i = 1; i < legalMoves.length; i++)
				if (legalMoves[i].fromRow != legalMoves[0].fromRow || legalMoves[i].fromCol != legalMoves[0].fromCol) {
					sameStartSquare = false;
					break;
				}
			if (sameStartSquare) {
				selectedRow = legalMoves[0].fromRow;
				selectedCol = legalMoves[0].fromCol;
			}
		}

		/* Make sure the board is redrawn in its new state. */

		repaint();

	} // end doMakeMove();

	public void update(Graphics g) {
		// The paint method completely redraws the canvas, so don't erase
		// before calling paint().
		paint(g);
	}

	public void paint(Graphics g) {
		// Draw checker board pattern in gray and lightGray. Draw the
		// checkers. If a game is in progress, highlight the legal moves.

		/* Draw a two-pixel black border around the edges of the canvas. */

		g.setColor(Color.black);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		g.drawRect(1, 1, getSize().width - 3, getSize().height - 3);

		/* Draw the squares of the checkerboard and the checkers. */

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (row % 2 == col % 2)
					g.setColor(Color.lightGray);
				else
					g.setColor(Color.gray);
				g.fillRect(2 + col * 20, 2 + row * 20, 20, 20);

				if (aniMove != null) {
					if ((row == aniMove.fromRow && col == aniMove.fromCol)
							|| (row == aniMove.toRow && col == aniMove.toCol)) {
						if (board.pieceAt(aniMove.toRow, aniMove.toCol) == CheckersData.RED
								|| board.pieceAt(aniMove.toRow, aniMove.toCol) == CheckersData.RED_KING)
							g.setColor(Color.red);
						else
							g.setColor(Color.black);

						g.fillOval(aniX, aniY, 16, 16);
						continue;
					}
				}

				switch (board.pieceAt(row, col)) {
				case CheckersData.RED:
					g.setColor(Color.red);
					g.fillOval(4 + col * 20, 4 + row * 20, 16, 16);
					break;
				case CheckersData.BLACK:
					g.setColor(Color.black);
					g.fillOval(4 + col * 20, 4 + row * 20, 16, 16);
					break;
				case CheckersData.RED_KING:
					g.setColor(Color.red);
					g.fillOval(4 + col * 20, 4 + row * 20, 16, 16);
					g.setColor(Color.white);
					g.drawString("K", 7 + col * 20, 16 + row * 20);
					break;
				case CheckersData.BLACK_KING:
					g.setColor(Color.black);
					g.fillOval(4 + col * 20, 4 + row * 20, 16, 16);
					g.setColor(Color.white);
					g.drawString("K", 7 + col * 20, 16 + row * 20);
					break;
				}
			}
		}

		/*
		 * If a game is in progress, highlight the legal moves. Note that legalMoves is
		 * never null while a game is in progress.
		 */

		if (gameInProgress) {
			// First, draw a cyan border around the pieces that can be moved.
			g.setColor(Color.cyan);
			for (int i = 0; i < legalMoves.length; i++) {
				g.drawRect(2 + legalMoves[i].fromCol * 20, 2 + legalMoves[i].fromRow * 20, 19, 19);
			}
			// If a piece is selected for moving (i.e. if selectedRow >= 0), then
			// draw a 2-pixel white border around that piece and draw green borders
			// around eacj square that that piece can be moved to.
			if (selectedRow >= 0) {
				g.setColor(Color.white);
				g.drawRect(2 + selectedCol * 20, 2 + selectedRow * 20, 19, 19);
				g.drawRect(3 + selectedCol * 20, 3 + selectedRow * 20, 17, 17);
				g.setColor(Color.green);
				for (int i = 0; i < legalMoves.length; i++) {
					if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow)
						g.drawRect(2 + legalMoves[i].toCol * 20, 2 + legalMoves[i].toRow * 20, 19, 19);
				}
			}
		}
	} // end paint()

	public Dimension getPreferredSize() {
		// Specify desired size for this component. Note:
		// the size MUST be 164 by 164.
		return new Dimension(164, 164);
	}

	public Dimension getMinimumSize() {
		return new Dimension(164, 164);
	}

	public void mousePressed(MouseEvent evt) {
		// Respond to a user click on the board. If no game is
		// in progress, show an error message. Otherwise, find
		// the row and column that the user clicked and call
		// doClickSquare() to handle it.
		if (gameInProgress == false)
			message.setText("Click \"New Game\" to start a new game.");
		else {
			int col = (evt.getX() - 2) / 20;
			int row = (evt.getY() - 2) / 20;
			if (col >= 0 && col < 8 && row >= 0 && row < 8)
				doClickSquare(row, col);
		}
	}

	public void mouseReleased(MouseEvent evt) {
	}

	public void mouseClicked(MouseEvent evt) {
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

} // end class SimpleCheckerboardCanvas