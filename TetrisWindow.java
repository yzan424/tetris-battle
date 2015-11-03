package netgame.tetrisgame;

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.IOException;
import netgame.common.*;

public class TetrisWindow extends JFrame {
   
  
   private static TetrisGameState state;
   
   
   private static Board board;    

   private static JLabel message;  
   
   private static int myID;        
   
   private static TetrisClient connection;  
   
   private class TetrisClient extends Client {

      
      public TetrisClient(String hubHostName,int hubPort) throws IOException {
         super(hubHostName, hubPort);
      }

      
      protected void messageReceived(final Object message) {
         if (message instanceof TetrisGameState) {
            SwingUtilities.invokeLater(new Runnable(){
               public void run() { 
                  newState( (TetrisGameState)message ); 
               }
            });
         }
      }

      protected void serverShutdown(String message) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               JOptionPane.showMessageDialog(TetrisWindow.this, 
                     "Your opponent has disconnected.\nThe game is ended.");
               System.exit(0);
            }
         });
      }
      
   }
   
   
   public static class Board extends JPanel implements 
   			KeyListener, FocusListener, MouseListener, ActionListener {

	   final int FinalWidth = 10;

	   final int FinalHeight = 22;

	   Timer timer;
	   
	   Timer timer1;
	   
	   Timer timer2;

	   int randNum;
	   
	   int turnAmount = 0;
	   
	   boolean finishedFalling = false;

	   boolean started = false;

	   boolean paused = true;
	   
	   boolean startDone = false;

	   int score = 0;

	   JLabel statusbar;

	   int[] currentX;

	   int[] currentY;
	   
	   int[] shadowX;
	   
	   int[] shadowY;
	   
	   ArrayList<Integer> rushX;

	   ArrayList<Integer> rushY;

	   ArrayList<Integer> staticX;

	   ArrayList<Integer> staticY;
	   
	   int[] tempY;


	   public Board() {
		   setFocusable(true);
		   randNum = (int)(Math.random()*7);
		   currentX = new int[4];
		   currentY = new int[4];
		   shadowX = new int[4];
		   shadowY = new int[4];
		   tempY = new int[]{0,0,0,0};
		   staticX = new ArrayList<Integer>();
		   staticY = new ArrayList<Integer>();
		   rushX = new ArrayList<Integer>();
		   rushY = new ArrayList<Integer>();
		   timer = new Timer(1000, this);
		   timer1 = new Timer(1000, this);
		   timer2 = new Timer(1000, this);
		   timer2.start();
		   clearBoard();

		   addKeyListener(this);   
		   addFocusListener(this);
		   addMouseListener(this);
	   } 


	   public void paintComponent(Graphics g) {
		   super.paintComponent(g);
		   if (state == null) {
	            g.drawString("Starting up.", 20, 35);
	            return;
	       }
		   if (!paused) {	    
//		   g.setColor(Color.RED);
		   for (int i = 0; i < staticX.size(); ++i) {
			   if (staticX.get(i) == 0 || staticX.get(i) == 1)	    
				   g.setColor(Color.RED);
			   else if (staticX.get(i) == 2 || staticX.get(i) == 3)
				   g.setColor(Color.ORANGE);
			   else if (staticX.get(i) == 4 || staticX.get(i) == 5)
				   g.setColor(Color.YELLOW);
			   else if (staticX.get(i) == 6 || staticX.get(i) == 7)
				   g.setColor(Color.GREEN);
			   else 
				   g.setColor(Color.BLUE);
			   if (staticX.get(i) >= 0) {
				   g.fillRect(staticX.get(i)*squareWidth(), staticY.get(i)*squareHeight(),squareWidth(),squareHeight());
			   }
		   }
		   g.setColor(Color.GRAY);
		   for (int u = 0; u < 4; ++u) {
			   g.fillRect(shadowX[u]*squareWidth(), shadowY[u]*squareHeight(),squareWidth(),squareHeight());
		   }
		   if (randNum == 0)	    
			   g.setColor(Color.RED);
		   else if (randNum == 1)
			   g.setColor(Color.ORANGE);
		   else if (randNum == 2)
			   g.setColor(Color.YELLOW);
		   else if (randNum == 3)
			   g.setColor(Color.GREEN);
		   else if (randNum == 4)
			   g.setColor(Color.BLUE);
		   else if (randNum == 5)
			   g.setColor(Color.CYAN);
		   else
			   g.setColor(Color.PINK);
		   for (int j = 0; j < 4; ++j) {
			   g.fillRect(currentX[j]*squareWidth(), currentY[j]*squareHeight(),squareWidth(),squareHeight());
		   }
		   
		   g.setColor(Color.BLACK);
		   int tempTime = 0;
		   if (state.time % 60 == 0)
			   g.drawString("Time Left " + state.time/60 + ":" + state.time % 60 + tempTime, 180, 35);
		   else if (state.time % 60 < 10) 
			   g.drawString("Time Left " + state.time/60 + ":" + tempTime + state.time % 60, 180, 35);
		   else
			   g.drawString("Time Left " + state.time/60 + ":" + state.time % 60, 180, 35);

		   }
		   if (state.time <= 0) {
			   if (myID == state.winner) {
				   g.setColor(Color.BLUE);
				   g.drawString("YOU WIN!", getWidth()/2, getHeight()/2);
			   }
			   else if (state.tie) {
				   g.setColor(Color.BLACK);
				   g.drawString("IT'S A TIE!", getWidth()/2, getHeight()/2);
			   }
			   else {
				   g.setColor(Color.RED);
				   g.drawString("YOU LOSE!", getWidth()/2, getHeight()/2);
			   }   
		   }
	   }
	   
	   int squareWidth() { 
		   return (int)(getSize().getWidth() / FinalWidth); 
	   }
	   int squareHeight() { 
		   return (int)(getSize().getHeight() / FinalHeight); 
	   }

	   public void start()
	   {
		   if (paused)
			   return;
		   
		   started = true;
		   finishedFalling = false;
		   turnAmount = 0;
		   score = 0;
		   clearBoard();

		   makeNewPiece();
		   timer.start();
		   timer1.start();
	   }
	   private int[] findXCoords(int random) {
		   if(random == 0) {
			   return new int[] {4,5,4,5};
		   }
		   else if(random == 1) {
			   return new int[] {5,5,5,5};
		   }
		   else if(random == 2) {
			   return new int[] {4,4,5,5};
		   }
		   else if(random == 3) {
			   return new int[] {4,4,3,3};
		   }
		   else if(random == 4) {
			   return new int[] {3,4,4,4};
		   }
		   else if(random == 5) {
			   return new int[] {5,4,4,4};
		   }
		   else {
			   return new int[] {3,4,5,4};
		   }
	   }
	   private int[] findYCoords(int random) {
		   if(random == 0) {
			   return new int[] {0,0,1,1};
		   }
		   else if(random == 1) {
			   return new int[] {0,1,2,3};
		   }
		   else if(random == 2) {
			   return new int[] {0,1,1,2};
		   }
		   else if(random == 3) {
			   return new int[] {0,1,1,2};
		   }	
		   else if(random == 4) {
			   return new int[] {0,0,1,2};
		   }
		   else if(random == 5) {
			   return new int[] {0,0,1,2};
		   }
		   else {
			   return new int[] {0,0,0,1};
		   }
	   }
	   private void findShadow() {
		   for (int h = 0; h < currentX.length; h++) {
			   shadowX[h] = currentX[h];
			   shadowY[h] = currentY[h];
		   }
		   while (true) {
			   boolean it = true;
			   for (int j = 0; j < 4; j++) {
				   if (!(attemptMove(shadowX[j], shadowY[j] + 1)))
					   it = false;
			   }
			   if (!it)
				   break;
			   else for (int i = 0; i < 4; i++) {
				   shadowY[i]++;
			   }
		   }
	   }
	   private void forceDrop() {
		   if(!paused) {
		   while (true) {
			   boolean possible = true;
			   for (int j = 0; j < 4; j++) {
				   if (!(attemptMove(currentX[j], currentY[j] + 1)))
					   possible = false;
			   }
			   if (!possible)
				   break;
			   else for (int i = 0; i < 4; i++) {
				   currentY[i]++;
			   }
		   }
		   fullyDropped();
		   }
	   }

	   private void dropOneLine()
	   {
		   if(!paused) {
		   boolean possibles = true;
		   for (int j = 0; j < 4; j++) {
			   if (!(attemptMove(currentX[j], currentY[j] + 1)))
				   possibles = false;
		   }
		   if (!possibles)
			   fullyDropped();
		   else for (int i = 0; i < 4; i++) {
			   currentY[i]++;
		   }
		   }
	   }


	   private void clearBoard()
	   {
		   staticX.clear();
		   staticY.clear();
	   }

	   private void fullyDropped()
	   {
		   for (int i = 0; i < 4; ++i) {
			   staticX.add(currentX[i]);
			   staticY.add(currentY[i]);
		   }

		   if (!finishedFalling) {
			   for (int i = 0; i < 4; i++) {
				   tempY[i] = currentY[i];
			   }
			   makeNewPiece();
		   }
		   removeLine();
	   }

	   private void makeNewPiece()
	   {
		   if (!paused) {
		   randNum = (int)(Math.random()*7);
		   currentX = findXCoords(randNum);
		   currentY = findYCoords(randNum);
		   turnAmount = 0;
		   findShadow();

		   boolean possibility = true;
		   for (int i = 0; i < 4; i++) {
			   if (!attemptMove(currentX[i], currentY[i])) {
				   possibility = false;
			   }
		   }
		   if(state != null && !possibility) {
			   timer.stop();
			   connection.send("KO");
			   start();
		   }
		   }
	   }

	   private boolean attemptMove(int x, int y)
	   {
		   if (x < 0 || y < 0 || x >= FinalWidth || y >= FinalHeight) {
			   return false;
		   }
		   else for (int i = 0; i < staticX.size(); i++) {
			   if (staticX.get(i) == x && staticY.get(i) == y)
				   return false;
		   }
		   return true;
	   }
	   private void move(int[] moving, int moveAmount) {
		   for (int i = 0; i < moving.length; i++) {
			   moving[i] += moveAmount;
		   }
		   findShadow();
	   }

	   private void removeLine()
	   {
		   int[] allCols = new int[FinalHeight];
		   for (int i = 0; i < FinalHeight; i++)
			   allCols[i] = 0;
		   for (int j = 0; j < staticY.size(); j++) {
			   allCols[staticY.get(j)]++;
		   }
		   ArrayList<Integer> fullRows = new ArrayList<Integer>();
		   for (int c = 0; c < allCols.length; c++) {
			   if (allCols[c] >= FinalWidth && (c == tempY[0] || c == tempY[1] || c == tempY[2] || c == tempY[3])) {
				   fullRows.add(c);
			   }
		   }
		   if (fullRows.size() > 0) {
			   for (int a = 0; a < fullRows.size(); a++) {
				   for (int b = 0; b < staticY.size(); b++) {
					   if (fullRows.get(a) == staticY.get(b)) {
						   staticX.remove(b);
						   staticY.remove(b);
						   b--;
					   }
				   }
			   }
			   for (int k = 0; k < fullRows.size(); k++) {
				   for (int w = 0; w < staticY.size(); w++) {
					   if (staticY.get(w) < fullRows.get(k))
						   staticY.set(w, staticY.get(w) + 1);
				   }
			   }
			   score += fullRows.size();
			   if (state != null) {
				   connection.send(score);
				   connection.send((double)fullRows.size());
			   }
			   findShadow();
			   makeNewPiece();
			   repaint();
		   }
	   }
	   
	   private void addLine(int num) {
		   for (int i = 0; i < staticY.size(); i++)
			   staticY.set(i, staticY.get(i) - num);
		   for (int x = 0; x < FinalWidth; x++) {
			   for (int y = 0; y < num; y++) {
				   staticX.add(x);
				   staticY.add(FinalHeight - 1 - y);
			   }
		   }
		   findShadow();
		   if (state != null) {
			   connection.send("done");
		   }
	   }
	   
	   
	   
	   public void actionPerformed(ActionEvent e) {
		   if (e.getSource() == timer) {
			   if (finishedFalling) {
				   finishedFalling = false;
				   makeNewPiece();
			   } 
			   else {
				   dropOneLine();
			   }
		   }
		   if (e.getSource() == timer1 && state.time > 0 && state != null) {
			   connection.send("time");
			   if (state.time <= 1) {
				   paused = true;
			   }
			   repaint();
		   }
		   if (e.getSource() == timer2) {
			   if (state != null && startDone == false) {
				   paused = false;
				   start();
				   startDone = true;
			   }
			   if (state != null && state.needToAdd1 && myID == state.player1)
				   addLine(state.addLines1);
			   else if (state != null && state.needToAdd2 && myID == state.player2)
				   addLine(state.addLines2);
			   
		   }
	   }

	   public void focusGained(FocusEvent evt) {
	   }


	   public void focusLost(FocusEvent evt) {
	   }

	   public void keyTyped(KeyEvent evt) {
	   }  // end keyTyped()

	   public void keyPressed(KeyEvent evt) { 

		   int key = evt.getKeyCode(); 

		   if (key == KeyEvent.VK_LEFT) { 
			   boolean possibles = true;
			   for (int j = 0; j < 4; j++) {
				   if (!(attemptMove(currentX[j] - 1, currentY[j])))
					   possibles = false;
			   }
			   if (possibles)
				   move(currentX, -1);
			   repaint();
		   }
		   else if (key == KeyEvent.VK_RIGHT) { 
			   boolean possibles1 = true;
			   for (int j = 0; j < 4; j++) {
				   if (!(attemptMove(currentX[j] + 1, currentY[j])))
					   possibles1 = false;
			   }
			   if (possibles1)
				   move(currentX, 1);
			   repaint();
		   }
		   else if (key == KeyEvent.VK_DOWN) {
			   dropOneLine();
			   repaint(); 
		   }
		   else if (key == KeyEvent.VK_SPACE) {
			   forceDrop();
			   repaint();
		   }
		   else if (key == KeyEvent.VK_UP) {
			   switch (randNum) {
			   case 0:
				   break;
			   case 1:
				   if (turnAmount == 0) {
					   if (attemptMove(currentX[0] - 2, currentY[0] + 2) && attemptMove(currentX[1] - 1, currentY[1] + 1) && attemptMove(currentX[3] + 1, currentY[3] - 1)) {
						   currentX[0] += -2;
						   currentY[0] += 2;
						   currentX[1] += -1;
						   currentY[1] += 1;
						   currentX[3] += 1;
						   currentY[3] += -1;
						   turnAmount = 1;
					   }
				   }
				   else {
					   if (attemptMove(currentX[0] + 2, currentY[0] - 2) && attemptMove(currentX[1] + 1, currentY[1] - 1) && attemptMove(currentX[3] - 1, currentY[3] + 1)) {
						   currentX[0] += 2;
						   currentY[0] += -2;
						   currentX[1] += 1;
						   currentY[1] += -1;
						   currentX[3] += -1;
						   currentY[3] += 1;
						   turnAmount = 0;
					   }
				   }
				   break;
			   case 2:
				   if (turnAmount == 0) {
					   if (attemptMove(currentX[0], currentY[0] + 1) && attemptMove(currentX[1] + 1, currentY[1]) && attemptMove(currentX[2], currentY[2] - 1) && attemptMove(currentX[3] + 1, currentY[3] - 2)) {
						   currentY[0] += 1;
						   currentX[1] += 1;
						   currentY[2] += -1;
						   currentX[3] += 1;
						   currentY[3] += -2;
						   turnAmount = 1;
					   }
				   }
				   else {
					   if (attemptMove(currentX[0], currentY[0] - 1) && attemptMove(currentX[1] - 1, currentY[1]) && attemptMove(currentX[2], currentY[2] + 1) && attemptMove(currentX[3] - 1, currentY[3] + 2)) {
						   currentY[0] += -1;
						   currentX[1] += -1;
						   currentY[2] += 1;
						   currentX[3] += -1;
						   currentY[3] += 2;
						   turnAmount = 0;
					   }
				   }
				   break;
			   case 3:
				   if (turnAmount == 0) {
					   if (attemptMove(currentX[0] - 1, currentY[0]) && attemptMove(currentX[1], currentY[1] - 1) && attemptMove(currentX[2] + 1, currentY[2]) && attemptMove(currentX[3] + 2, currentY[3] - 1)) {
						   currentX[0] += -1;
						   currentY[1] += -1;
						   currentX[2] += 1;
						   currentX[3] += 2;
						   currentY[3] += -1;
						   turnAmount = 1;
					   }
				   }
				   else {
					   if (attemptMove(currentX[0] + 1, currentY[0]) && attemptMove(currentX[1], currentY[1] + 1) && attemptMove(currentX[2] - 1, currentY[2]) && attemptMove(currentX[3] - 2, currentY[3] + 1)) {
						   currentX[0] += 1;
						   currentY[1] += 1;
						   currentX[2] += -1;
						   currentX[3] += -2;
						   currentY[3] += 1;
						   turnAmount = 0;
					   }
				   }
				   break;
			   case 4:
				   if (turnAmount == 0) {
					   if (attemptMove(currentX[0] + 2, currentY[0]) && attemptMove(currentX[1] + 1, currentY[1] + 1) && attemptMove(currentX[3] - 1, currentY[3] - 1)) {
						   currentX[0] += 2;
						   currentY[1] += 1;
						   currentX[1] += 1;
						   currentX[3] += -1;
						   currentY[3] += -1;
						   turnAmount = 1;
					   }
				   }
				   else if (turnAmount == 1){
					   if (attemptMove(currentX[0], currentY[0] + 2) && attemptMove(currentX[1] - 1, currentY[1] + 1) && attemptMove(currentX[3] + 1, currentY[3] - 1)) {
						   currentY[0] += 2;
						   currentX[1] += -1;
						   currentY[1] += 1;
						   currentX[3] += 1;
						   currentY[3] += -1;
						   turnAmount = 2;
					   }
				   }
				   else if (turnAmount == 2) {
					   if (attemptMove(currentX[0] - 2, currentY[0]) && attemptMove(currentX[1] - 1, currentY[1] - 1) && attemptMove(currentX[3] + 1, currentY[3] + 1)) {
						   currentX[0] += -2;
						   currentY[1] += -1;
						   currentX[1] += -1;
						   currentX[3] += 1;
						   currentY[3] += 1;
						   turnAmount = 3;
					   }
				   }
				   else {
					   if (attemptMove(currentX[0], currentY[0] - 2) && attemptMove(currentX[1] + 1, currentY[1] - 1) && attemptMove(currentX[3] - 1, currentY[3] + 1)) {
						   currentY[0] += -2;
						   currentX[1] += 1;
						   currentY[1] += -1;
						   currentX[3] += -1;
						   currentY[3] += 1;
						   turnAmount = 0;
					   }
				   }
				   break;
			   case 5:
				   if (turnAmount == 0) {
					   if (attemptMove(currentX[0], currentY[0] + 2) && attemptMove(currentX[1] + 1, currentY[1] + 1) && attemptMove(currentX[3] - 1, currentY[3] - 1)) {
						   currentY[0] += 2;
						   currentY[1] += 1;
						   currentX[1] += 1;
						   currentX[3] += -1;
						   currentY[3] += -1;
						   turnAmount = 1;
					   }
				   }
				   else if (turnAmount == 1){
					   if (attemptMove(currentX[0] - 2, currentY[0]) && attemptMove(currentX[1] - 1, currentY[1] + 1) && attemptMove(currentX[3] + 1, currentY[3] - 1)) {
						   currentX[0] += -2;
						   currentX[1] += -1;
						   currentY[1] += 1;
						   currentX[3] += 1;
						   currentY[3] += -1;
						   turnAmount = 2;
					   }
				   }
				   else if (turnAmount == 2) {
					   if (attemptMove(currentX[0], currentY[0] - 2) && attemptMove(currentX[1] - 1, currentY[1] - 1) && attemptMove(currentX[3] + 1, currentY[3] + 1)) {
						   currentY[0] += -2;
						   currentY[1] += -1;
						   currentX[1] += -1;
						   currentX[3] += 1;
						   currentY[3] += 1;
						   turnAmount = 3;
					   }
				   }
				   else {
					   if (attemptMove(currentX[0] + 2, currentY[0]) && attemptMove(currentX[1] + 1, currentY[1] - 1) && attemptMove(currentX[3] - 1, currentY[3] + 1)) {
						   currentX[0] += 2;
						   currentX[1] += 1;
						   currentY[1] += -1;
						   currentX[3] += -1;
						   currentY[3] += 1;
						   turnAmount = 0;
					   }
				   }
				   break;
			   case 6:
				   if (turnAmount == 0) {
					   if (attemptMove(currentX[2] - 1, currentY[2] - 1)) {
						   currentX[2] += -1;
						   currentY[2] += -1;
						   turnAmount = 1;
					   }
				   }
				   else if (turnAmount == 1){
					   if (attemptMove(currentX[3] + 1, currentY[3] - 1)) {
						   currentX[3] += 1;
						   currentY[3] += -1;
						   turnAmount = 2;
					   }
				   }
				   else if (turnAmount == 2) {
					   if (attemptMove(currentX[0] + 1, currentY[0] + 1)) {
						   currentX[0] += 1;
						   currentY[0] += 1;
						   turnAmount = 3;
					   }
				   }
				   else {
					   if (attemptMove(currentX[2] - 1, currentY[2] + 1)) {
						   currentX[2] += -1;
						   currentY[2] += 1;
						   int temp = currentX[0];
						   currentX[0] = currentX[2];
						   currentX[2] = currentX[3];
						   currentX[3] = temp;
						   int temp2 = currentY[0];
						   currentY[0] = currentY[2];
						   currentY[2] = currentY[3];
						   currentY[3] = temp2;
						   turnAmount = 0;
					   }
				   }
				   break;
			   }
			   findShadow();
			   repaint();
		   }
	   }

	   public void keyReleased(KeyEvent evt) {
		   repaint();
	   }
	   public void mousePressed(MouseEvent evt) {
		   requestFocus();
	   }   


	   public void mouseEntered(MouseEvent evt) { }  
	   public void mouseExited(MouseEvent evt) { }  
	   public void mouseReleased(MouseEvent evt) { } 
	   public void mouseClicked(MouseEvent evt) { }




}   

   public TetrisWindow(String hostName, int serverPortNumber)  throws IOException {
      super("Rainbow Tetris");
      connection = new TetrisClient(hostName, serverPortNumber);
      myID = connection.getID();
      board = new Board();
      message = new JLabel("Waiting for two players to connect.", JLabel.CENTER);
      board.setBackground(Color.WHITE);
      board.setPreferredSize(new Dimension(300,660));
      board.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent evt) {
            doMouseClick();
         }
      });
      message.setBackground(Color.LIGHT_GRAY);
      message.setOpaque(true);
      JPanel content = new JPanel();
      content.setLayout(new BorderLayout(2,2));
      content.setBorder(BorderFactory.createLineBorder(Color.GRAY,2));
      content.setBackground(Color.GRAY);
      content.add(board,BorderLayout.CENTER);
      content.add(message,BorderLayout.SOUTH);
      setContentPane(content);
      pack();
      setResizable(false);
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter(){
         public void windowClosing(WindowEvent evt) {
            dispose();
            connection.disconnect();  
            try {
               Thread.sleep(333); 
            }
            catch (InterruptedException e) {
            }
            System.exit(0);
         }
      });
      setLocation(200,100);
      setVisible(true);
   }
   private void doMouseClick() {
   }
   
   private void newState(TetrisGameState state) {
      if ( state.playerDisconnected ) {
         JOptionPane.showMessageDialog(this, "Your opponent has disconnected.\nThe game is ended.");
         System.exit(0);
      }
      this.state = state;
      board.repaint();
      if (!state.gameInProgress || state == null) {
         return;
      }
      else if (state.winner != -1 || state.tie) {
         setTitle("Game Over");
      }
      else {
         setTitle("Game In Progress");
         if (myID == state.player1)
         	message.setText("You: " + state.score1 + " pts " + state.KO1 + " KOs Opponent: " + state.score2 + " pts " + state.KO2 + " KOs");
         else
        	 message.setText("You: " + state.score2 + " pts " + state.KO2 + " KOs Opponent: " + state.score1 + " pts " + state.KO1 + " KOs");
      }
   }

}
