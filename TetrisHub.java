package netgame.tetrisgame;

import java.io.IOException;

import netgame.common.Hub;

public class TetrisHub extends Hub {
   
   private TetrisGameState state; 
   
   public TetrisHub(int port) throws IOException {
      super(port);
      state = new TetrisGameState();
      setAutoreset(true);
   }

   protected void messageReceived(int playerID, Object message) {
      state.applyMessage(playerID, message);
      sendToAll(state);
   }

   protected void playerConnected(int playerID) {
      if (getPlayerList().length == 2) {
         shutdownServerSocket();
         state.startFirstGame();
         sendToAll(state);
      }
   }

   protected void playerDisconnected(int playerID) {
      state.playerDisconnected = true;
      sendToAll(state);
   }
}