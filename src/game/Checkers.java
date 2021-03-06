package game;
import java.awt.*;
import java.applet.*;

@SuppressWarnings("serial")
public class Checkers extends Applet{
	/* The main applet class only lays out the applet.  The work of
    the game is all done in the CheckersCanvas object.   Note that
    the Buttons and Label used in the applet are defined as 
    instance variables in the CheckersCanvas class.  The applet
    class gives them their visual appearance and sets their
    size and positions.*/

 public void init() {
	 
	this.setSize(new Dimension(350, 250));
 
    setLayout(null);  // I will do the layout myself.
 
    setBackground(new Color(0,150,0));  // Dark green background.
    
    /* Create the components and add them to the applet. */

    CheckersCanvas board = new CheckersCanvas();
        // Note: The constructor creates the buttons board.resignButton
        // and board.startButton and the Label board.message.
    add(board);

    board.startButton.setBackground(Color.lightGray);
    add(board.startButton);

    board.resignButton.setBackground(Color.lightGray);
    add(board.resignButton);
    
    board.check.setVisible(true);
    add(board.check);
    add(board.checkAGH);
    add(board.checkMMAG);
    add(board.checkMMH);

    board.message.setForeground(Color.green);
    board.message.setFont(new Font("Serif", Font.BOLD, 14));
    add(board.message);
    
    /* Set the position and size of each component by calling
       its setBounds() method. */

    board.setBounds(20,20,164,164); // Note:  size MUST be 164-by-164 !
    board.startButton.setBounds(210, 20, 100, 30);
    board.resignButton.setBounds(210, 55, 100, 30);
    board.message.setBounds(0, 200, 330, 30);
    board.check.setBounds(50, 220, 200, 30);
    board.checkMMH.setBounds(210, 90, 130, 30);
    board.checkAGH.setBounds(210, 120, 130, 30);
    board.checkMMAG.setBounds(210, 150, 130, 30);
 }
}
