package game;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.activation.ActivationGroupID;

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
	Button startButton; // This button starts a new game. It is enabled only
							// when the current game has ended.

	CheckboxGroup cbg;
	Checkbox checkMMH, checkMMAG, checkAGAG, checkAGH, check;
	

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

	GAPlayer[] gapRed, gapBlack; // Genetic algorithm player.
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
		startButton = new Button("New Game");
		startButton.addActionListener(this);
		cbg = new CheckboxGroup();
		checkMMH = new Checkbox("Minimax VS Human", true, cbg);
		checkMMAG = new Checkbox("Minimax VS Genetic", false, cbg);
		checkAGAG = new Checkbox("Genetic VS Genetic", false, cbg);
		checkAGH = new Checkbox("Genetic VS Human", false, cbg);
		check = new Checkbox("Use previous generation", false);
		message = new Label("", Label.CENTER);
		board = new CheckersData();
	}

	public void actionPerformed(ActionEvent evt) {
		// Respond to user's click on one of the two buttons.
		Object src = evt.getSource();
		if (src == startButton)
			startGame();
		else if (src == resignButton)
			doResign();
	}
	
	private void startGame() {
		if(checkAGAG.getState()) {
			gapRed = Population.newPop();
			gapBlack = Population.newPop();
		}else if(checkAGH.getState()) {
			gapBlack = Population.newPop();
		}else if(checkMMAG.getState()) {
			mmp = new MMPlayer();
			gapBlack = Population.newPop();
		}else if(checkMMH.getState()) {
			mmp = new MMPlayer();
		}
		
		doNewGame();
		makePlay(CheckersData.RED);
		
	}
	
	private void makePlay(int player) {
		if(player == CheckersData.RED) {
			if(checkAGAG.getState()) {
				gapRed[GAIndex].play(legalMoves, this, board.getBoard());
			}else if(checkMMAG.getState() || checkMMH.getState()) {
				mmp.play(legalMoves, this, board.getBoard());
			}
		}else {
			if(checkAGAG.getState() || checkMMAG.getState() || checkAGH.getState()) {
				gapBlack[GAIndex].play(legalMoves, this, board.getBoard());
			}
		}
	}

	void doNewGame() {
		// Begin a new game.
		/*if (gameInProgress == true) {
			// This should not be possible, but it doens't
			// hurt to check.
			message.setText("Finish the current game first!");
			return;
		}*/
		board.setUpGame(); // Set up the pieces.
		currentPlayer = CheckersData.RED; // RED moves first.
		legalMoves = board.getLegalMoves(CheckersData.RED); // Get RED's legal moves.
		selectedRow = -1; // RED has not yet selected a piece to move.
		// message.setText("Red: Make your move.");
		//message.setText("PLAYER[" + GAIndex + "]. PREVIOUS FITNESS: " + gap[GAIndex].fitness);
		gameInProgress = true;
		//startButton.setEnabled(false);
		resignButton.setEnabled(true);
		turns = 0;
		
		//Reading Gen files if checkbox is checked
		if (check.getState()) {
			if(checkAGAG.getState()) {
				float[][] pRed, pBlack;
				
				pRed = readGenFile("geninfoRED.txt");
				pBlack = readGenFile("geninfoBLACK.txt");
				
				for(int i=0; i<Population.N; i++) {
					for(int j=0; j<5; j++) {
						if(pRed != null) gapRed[i].points[j] = pRed[i][j];
						if(pBlack != null) gapBlack[i].points[j] = pBlack[i][j];
					}
				}
				
				if(pRed == null) {
					for(int i=0; i<Population.N; i++)
						gapRed[i] = Population.newGAPlayer();
				}
				if(pBlack == null) {
					for(int i=0; i<Population.N; i++)
						gapBlack[i] = Population.newGAPlayer();
				}
			}else if(checkAGH.getState() || checkMMAG.getState()) {
				float[][] pBlack = readGenFile("geninfoBLACK.txt");
				
				if(pBlack == null) {
					for(int i=0; i<Population.N; i++)
						gapBlack[i] = Population.newGAPlayer();
				}else {
					for(int i=0; i<Population.N; i++) {
						for(int j=0; j<5; j++) {
							gapBlack[i].points[j] = pBlack[i][j];
						}
					}
				}
			}
		}

		repaint();
	}
	
	private float[][] readGenFile(String filename) {
		File file = new File(filename);
		FileReader fr = null;
		float[][] points = new float[Population.N][5]; 
		
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
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
						//gapRed[i].points[j] = Float.valueOf(sPoints[j]);
						points[i][j] = Float.valueOf(sPoints[j]);
					}
				}
				//System.out.printf("%f %f %f %f %f\n", gap[i].points[0], gap[i].points[1], gap[i].points[2], gap[i].points[3], gap[i].points[4]);
			}
			
			try {
				bf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return points;
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
		startButton.setEnabled(true);
		resignButton.setEnabled(false);
		gameInProgress = false;

		//Scoring the fitness of the Genetic algorithms
		if(checkAGAG.getState()) {
			if (winner == CheckersData.RED)
				gapRed[GAIndex].fitness = 500 - turns;
			else if (winner == CheckersData.EMPTY)
				gapRed[GAIndex].fitness = board.boardStateMM(CheckersData.RED) + 200;
			else
				gapRed[GAIndex].fitness = turns;
		}
		if(checkMMAG.getState() || checkAGAG.getState()) {
			if (winner == CheckersData.BLACK)
				gapBlack[GAIndex].fitness = 500 - turns;
			else if (winner == CheckersData.EMPTY)
				gapBlack[GAIndex].fitness = board.boardStateMM(CheckersData.BLACK) + 200;
			else
				gapBlack[GAIndex].fitness = turns;
		}
		
		
		if (GAIndex < 9) {
			GAIndex++;
			doNewGame();
			
			//Making the first play after starting a new game
			makePlay(CheckersData.RED);
			/*if(checkAGAG.getState())
				gapRed[GAIndex].play(legalMoves, this, board.getBoard());
			else if(checkMMAG.getState())
				mmp.play(legalMoves, this, board.getBoard());*/
			
		} else {
			
			if(checkAGAG.getState() || checkMMAG.getState()) {
				Pair best = new Pair(0, gapBlack[0].fitness);
				Pair worst[] = new Pair[2];
	
				if (gapBlack[0].fitness < gapBlack[1].fitness) {
					worst[0] = new Pair(0, gapBlack[0].fitness);
					worst[1] = new Pair(1, gapBlack[1].fitness);
				} else {
					worst[1] = new Pair(0, gapBlack[0].fitness);
					worst[0] = new Pair(1, gapBlack[1].fitness);
				}
	
				for (int i = 1; i < 10; i++) {
					if (best.second < gapBlack[i].fitness) {
						best.first = i;
						best.second = gapBlack[i].fitness;
					}
	
					if (worst[1].second > gapBlack[i].fitness) {
						if (worst[0].second > gapBlack[i].fitness) {
							worst[0].first = i;
							worst[0].second = gapBlack[i].fitness;
						} else {
							worst[1].first = i;
							worst[1].second = gapBlack[i].fitness;
						}
					}
	
				}
	
				gapBlack = Population.crossOver(gapBlack, best.first);
				gapBlack[worst[0].first] = Population.newGAPlayer();
				gapBlack[worst[1].first] = Population.newGAPlayer();
				gen++;
	
				System.out.println("GENERATION " + gen + ": ");
				System.out.println("BEST: " + best.first);
				System.out.printf("WORST WERE: %d [%d] - %d [%d]\n", worst[0].first, worst[0].second, worst[1].first,
						worst[1].second);
				Population.printPop(gapBlack);
	
				GAIndex = 0;
	
				if (gen < 5) {
					doNewGame();
					makePlay(CheckersData.RED);
				}else {
					if(checkMMAG.getState())
					try {
						writeGenInfo(CheckersData.BLACK);
					} catch (IOException e) {
						// TODO: handle exception
					}
				}
			}
			
			if(checkAGAG.getState()) {
				Pair best = new Pair(0, gapRed[0].fitness);
				Pair worst[] = new Pair[2];
	
				if (gapRed[0].fitness < gapRed[1].fitness) {
					worst[0] = new Pair(0, gapRed[0].fitness);
					worst[1] = new Pair(1, gapRed[1].fitness);
				} else {
					worst[1] = new Pair(0, gapRed[0].fitness);
					worst[0] = new Pair(1, gapRed[1].fitness);
				}
	
				for (int i = 1; i < 10; i++) {
					if (best.second < gapRed[i].fitness) {
						best.first = i;
						best.second = gapRed[i].fitness;
					}
	
					if (worst[1].second > gapRed[i].fitness) {
						if (worst[0].second > gapRed[i].fitness) {
							worst[0].first = i;
							worst[0].second = gapRed[i].fitness;
						} else {
							worst[1].first = i;
							worst[1].second = gapRed[i].fitness;
						}
					}
	
				}
	
				gapRed = Population.crossOver(gapRed, best.first);
				
				gapRed[worst[0].first] = Population.newGAPlayer();
				gapRed[worst[1].first] = Population.newGAPlayer();
				gen++;
	
				System.out.println("GENERATION " + gen + ": ");
				System.out.println("BEST: " + best.first);
				System.out.printf("WORST WERE: %d [%d] - %d [%d]\n", worst[0].first, worst[0].second, worst[1].first,
						worst[1].second);
				Population.printPop(gapRed);
	
				GAIndex = 0;
	
				if (gen < 5)
					doNewGame();
				else
					try {
						writeGenInfo(CheckersData.RED);
					} catch (IOException e) {
						// TODO: handle exception
					}
			}

		}

	}

	private void writeGenInfo(int player) throws IOException {
		System.out.println("WRITING");
		File file = null;
		if(player == CheckersData.RED)
			file = new File("geninfoRED.txt");
		else file = new File("geninfoBLACK.txt");
		FileWriter fw = null;

		fw = new FileWriter(file);

		if (fw != null) {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 5; j++) {
					if(player == CheckersData.RED)
						sb.append(String.valueOf(gapRed[i].points[j]));
					else sb.append(String.valueOf(gapBlack[i].points[j]));
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
		
		//Ends the game in a draw if the turn count is over 150
		//Only does this when none of the players are human
		if (turns > 150 && !checkAGH.getState() && !checkMMH.getState()) {
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
					if(checkAGAG.getState())
						gapRed[GAIndex].play(legalMoves, this, board.getBoard());
					else if(checkMMAG.getState() || checkMMH.getState())
						mmp.play(legalMoves, this, board.getBoard());
				} else {
					// message.setText("BLACK: You must continue jumping.");
					// gap.play(legalMoves, this);
					if(checkAGAG.getState() || checkAGH.getState() || checkMMAG.getState())
						gapBlack[GAIndex].play(legalMoves, this, board.getBoard());
					else if(checkMMH.getState())
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
				/*
				if(checkAGAG.getState())
					gapRed[GAIndex].play(legalMoves, this, board.getBoard());
				else if(checkMMAG.getState())
					mmp.play(legalMoves, this, board.getBoard());*/
			}
			/*
			 * else if (legalMoves[0].isJump())
			 * message.setText("BLACK:  Make your move.  You must jump."); else
			 * message.setText("BLACK:  Make your move.");
			 */

			// gap.play(legalMoves, this)
			if (fl)
				if(checkAGAG.getState() || checkMMAG.getState() || checkAGH.getState())
					gapBlack[GAIndex].play(legalMoves, this, board.getBoard());
				//else if(checkMMH.getState())
					//mmp.play(legalMoves, this, board.getBoard());
		} else {
			currentPlayer = CheckersData.RED;
			legalMoves = board.getLegalMoves(currentPlayer);

			boolean fl = true;
			if (legalMoves == null) {
				gameOver("RED has no moves.  BLACK wins.", CheckersData.BLACK);
				fl = false;
				/*if(checkAGAG.getState())
					gapRed[GAIndex].play(legalMoves, this, board.getBoard());
				else if(checkMMAG.getState())
					mmp.play(legalMoves, this, board.getBoard());*/
			}
			/*
			 * else if (legalMoves[0].isJump())
			 * message.setText("RED:  Make your move.  You must jump."); else
			 * message.setText("RED:  Make your move.");
			 */

			if (fl)
				if(checkAGAG.getState())
					gapRed[GAIndex].play(legalMoves, this, board.getBoard());
				else if(checkMMAG.getState() || checkMMH.getState())
					mmp.play(legalMoves, this, board.getBoard());
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