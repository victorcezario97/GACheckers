package players;

import game.CheckersCanvas;
import game.CheckersData;
import game.CheckersMove;
import utils.Pair;

public class MMPlayer {
	
	public void play(CheckersMove[] moves, CheckersCanvas canvas, int[][] board){
		Pair p = findPlay(moves, board, CheckersData.BLACK, 0);
		//System.out.println("-->" + p.second);
		//System.out.println(p.first + " " + p.second);
		
		if(moves == null) return;
		canvas.doMakeMove(moves[p.first]);
		System.out.println();
	}
	
	private Pair findPlay(CheckersMove[] moves, int[][] board, int player, int it) {
		if(moves == null) return null;
		//int[] playValues = new int[moves.length];
		Pair p = null;
		Pair aux;
		CheckersData data;
		int newPlayer, count = 0, countAux = 0;
		
		if(player == CheckersData.RED) newPlayer = CheckersData.BLACK;
		else newPlayer = CheckersData.RED;
		//System.out.printf("IT: %d, PL: %d, %d\n", it, player, moves.length);
		//System.out.println("--->" + moves.length);
		if(player == CheckersData.BLACK) p = new Pair(-1, -13);
		else p = new Pair(-1, 13);
		
		for(CheckersMove move : moves) {
			//System.out.println(count);
			
			
			data = new CheckersData();
			data.setBoard(board);
			//if(it == 0) System.out.println(count);
			
			executeMove(data, move, player);
			
			//if(move == moves[0]) p = new Pair(0, data.boardState(CheckersData.BLACK));
			
			
			if(it == 2) {
				aux = new Pair(count++, data.boardState(CheckersData.BLACK));
			}else {
				aux = findPlay(data.getLegalMoves(newPlayer), data.getBoard(), newPlayer, it+1);
				if(aux != null) aux.first = countAux++;
				//if(aux != null) System.out.println(aux.first + " " + aux.second);
			}
			//count++;
			
			if(aux == null) {
				System.out.println("NULL");
				continue;
			}
			if(player == CheckersData.BLACK) {
				if(aux.second > p.second) {
					if(it == 0) System.out.printf("Swapping: %d/%d -> %d/%d\n", p.first, p.second, aux.first, aux.second);
					p.first = aux.first;
					p.second = aux.second;
				}
			}else {
				if(aux.second < p.second) {
					//System.out.println("Entrou");
					p.first = aux.first;
					p.second = aux.second;
				}
			}
			
			//if(it ==0) System.out.println(aux.first + " " + aux.second);
			if(it == 0) System.out.printf("\tFrom: [%d][%d], To: [%d][%d], V: %d\n", move.fromRow, move.fromCol, move.toRow, move.toCol, aux.second);	
		}
		
		
		return p;
	}
	
	private void executeMove(CheckersData data, CheckersMove move, int player) {
		CheckersMove[] jumpMoves;
		
		data.makeMove(move);
		
		if(move.isJump()) {
			
	         jumpMoves = data.getLegalJumpsFrom(player, move.toRow, move.toCol);
	         if (jumpMoves != null) {
	        	 System.out.printf("move [%d][%d]->[%d][%d]\n", jumpMoves[0].fromRow, jumpMoves[0].fromCol, jumpMoves[0].toRow, jumpMoves[0].toCol);
	            data.makeMove(jumpMoves[0]);
	            System.out.println(data.boardState(player));
	         }
	      }
	}
	
	//TODO: Arrumar o negócio de comer duas vezes em uma mesma jogada. Criar uma função pra isso;

}
