package players;

import game.CheckersCanvas;
import game.CheckersData;
import game.CheckersMove;
import utils.Pair;

public class MMPlayer {
	
	public void play(CheckersMove[] moves, CheckersCanvas canvas, int[][] board){
		
		Pair p = findPlay(moves, board, CheckersData.BLACK, 0);
		
		if(moves == null) return;
		canvas.doMakeMove(moves[p.first]);
	}
	
	//Finds the best play checking 3 levels of the plays tree
	private Pair findPlay(CheckersMove[] moves, int[][] board, int player, int it) {
		//If there are no moves, the game is over
		if(moves == null) {
			//If the game is over for the BLACK player, the AI has lost, so it receives a very low score
			if(player == CheckersData.BLACK) return new Pair(0, -49);
			//If the game is over for the RED player, the AI has won, so it receives a very high score
			else return new Pair(0, 49);
		}

		Pair p = null;
		Pair aux;
		CheckersData data;
		int newPlayer, count = 0, countAux = 0;
		
		if(player == CheckersData.RED) newPlayer = CheckersData.BLACK;
		else newPlayer = CheckersData.RED;

		if(player == CheckersData.BLACK) p = new Pair(-1, -50);
		else p = new Pair(-1, 50);
		
		for(CheckersMove move : moves) {			
			
			//Create a new board to simulate the moves
			data = new CheckersData();
			data.setBoard(board);
			
			//Execute the specified move
			executeMove(data, move, player);
						
			//If it's the third iteration, the recursion stops
			if(it == 2) {
				aux = new Pair(count++, data.boardStateMM(CheckersData.BLACK));
			}else {
				aux = findPlay(data.getLegalMoves(newPlayer), data.getBoard(), newPlayer, it+1);
				if(aux != null) aux.first = countAux++;
			}
			
			/*if(aux == null) {
				System.out.println("NULL");
				continue;
			}*/
			//If it's the AI's turn, it looks for the best play
			if(player == CheckersData.BLACK) {
				if(aux.second > p.second) {
					p.first = aux.first;
					p.second = aux.second;
				}
			//If it's the opponent's turn, the AI expects it to make the best possible move for them 
			}else {
				if(aux.second < p.second) {
					p.first = aux.first;
					p.second = aux.second;
				}
			}	
		}
		
		return p;
	}
	
	private void executeMove(CheckersData data, CheckersMove move, int player) {
		CheckersMove[] jumpMoves;
		
		data.makeMove(move);
		
		if(move.isJump()) {	
	         jumpMoves = data.getLegalJumpsFrom(player, move.toRow, move.toCol);
	         if (jumpMoves != null) {
	            data.makeMove(jumpMoves[0]);
	         }
	    }
	}
	

}
