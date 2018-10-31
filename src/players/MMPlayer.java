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
	
	private Pair findPlay(CheckersMove[] moves, int[][] board, int player, int it) {
		if(moves == null) return null;

		Pair p = null;
		Pair aux;
		CheckersData data;
		int newPlayer, count = 0, countAux = 0;
		
		if(player == CheckersData.RED) newPlayer = CheckersData.BLACK;
		else newPlayer = CheckersData.RED;

		if(player == CheckersData.BLACK) p = new Pair(-1, -50);
		else p = new Pair(-1, 50);
		
		for(CheckersMove move : moves) {			
			
			data = new CheckersData();
			data.setBoard(board);
			
			executeMove(data, move, player);
						
			if(it == 2) {
				aux = new Pair(count++, data.boardState(CheckersData.BLACK));
			}else {
				aux = findPlay(data.getLegalMoves(newPlayer), data.getBoard(), newPlayer, it+1);
				if(aux != null) aux.first = countAux++;
			}
			
			if(aux == null) {
				System.out.println("NULL");
				continue;
			}
			if(player == CheckersData.BLACK) {
				if(aux.second > p.second) {
					p.first = aux.first;
					p.second = aux.second;
				}
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
