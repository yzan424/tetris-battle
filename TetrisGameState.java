package netgame.tetrisgame;

import java.io.Serializable;

public class TetrisGameState implements Serializable {
   
   public boolean playerDisconnected; 
   
   public boolean gameInProgress;
   
   public boolean needToAdd1 = false;
   
   public boolean needToAdd2 = false;
   
   public boolean newGame = false;
   
   public int player1;   
   
   public int player2;   
   
   public int score1;
   
   public int score2;
   
   public int KO1;
   
   public int KO2;
   
   public int addLines1 = 0;
   
   public int addLines2 = 0;
   
   public int winner;
   
   public int time = 120;
   
   public boolean tie;
   
   public void applyMessage(int sender, Object message) {
      if (gameInProgress && message instanceof Integer) {
         int move = (int)message;
         if (sender == player1)
        	 score1 = move;
         else
        	 score2 = move;
      }
      else if (gameInProgress && message.equals("time") && sender == player1) {
    	  time--;
    	  if (winner()) {
          	 if (score1 < score2)
          		 winner = player2;
          	 else
          		 winner = player1;
           }
           else if (tie()) {
         	  tie = true;
           }
      }
      else if (gameInProgress && message instanceof Double) {
    	  double thing = (double)message;
    	  if (sender == player1) {
    		  if (thing == 1)
    			  addLines2 = 1;
    		  if (thing == 2)
    			  addLines2 = 2;
    		  if (thing == 3)
    			  addLines2 = 3;
    		  if (thing == 4)
    			  addLines2 = 4;
    		  needToAdd2 = true;
    	  }
    	  else {
    		  if (thing == 1)
    			  addLines1 = 1;
    		  if (thing == 2)
    			  addLines1 = 2;
    		  if (thing == 3)
    			  addLines1 = 3;
    		  if (thing == 4)
    			  addLines1 = 4;
    		  needToAdd1 = true;
    	  }
      }
      else if (gameInProgress && message.equals("KO")) {
    	  if (sender == player1)
    		  KO2++;
    	  else
    		  KO1++;
      }
      else if (gameInProgress && message.equals("done")) {
    	  if (sender == player1){
    		  needToAdd1 = false;
    		  addLines1 = 0;
    	  }
    	  else {
    		  needToAdd2 = false;
    		  addLines2 = 0;
    	  }
      }
      else if (!gameInProgress && message.equals("newgame")) {
         startGame();
      }
      else if (!gameInProgress && message.equals("did")) {
         newGame = false;
       }
   }
   
   void startFirstGame() {
      startGame();
   }
   
   private void startGame() {
      player1 = 1;
      player2 = 2;
      score1 = 0;
      score2 = 0;
      KO1 = 0;
      KO2 = 0;
      time = 120;
      winner = -1;
      gameInProgress = true;
      tie = false;
      newGame = true;
      needToAdd1 = false;
      needToAdd2 = false;
      addLines1 = 0;
      addLines2 = 0;
   }
   
   private boolean winner() {
      if (time <= 0) {
    	  if (KO1 == KO2) {
    		  if (score1 != score2)
    			  return true;
    		  else
    			  return false;
    	  }
    	  else
    		  return true;
      }
      else 
    	  return false;
   }
   private boolean tie() {
	   if (time <= 0 && KO1 == KO2 && score1 == score2) {
		   return true;
	   }
	   else
		   return false;
   }
   
}