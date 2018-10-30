package players;
import game.CheckersCanvas;
import game.CheckersMove;

public class GAPlayer {
	
	public  void play(CheckersMove[] moves, CheckersCanvas canvas){
		if(moves == null) return;
		canvas.doMakeMove(moves[0]);
	}

}
