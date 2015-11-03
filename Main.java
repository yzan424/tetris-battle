package netgame.tetrisgame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import netgame.common.*;

public class Main {

   private static final int DEFAULT_PORT = 45017;
   
   public static void main(String[] args) {
      
      JLabel message = new JLabel("Welcome to Tetris!", JLabel.CENTER);
      message.setFont(new Font("Serif", Font.BOLD, 16));
      
      final JTextField listeningPortInput = new JTextField("" + DEFAULT_PORT, 5);
      final JTextField hostInput = new JTextField(30);
      final JTextField connectPortInput = new JTextField("" + DEFAULT_PORT, 5);
      
      final JRadioButton selectServerMode = new JRadioButton("Start a new game");
      final JRadioButton selectClientMode = new JRadioButton("Connect to existing game");
      
      ButtonGroup group = new ButtonGroup();
      group.add(selectServerMode);
      group.add(selectClientMode);
      ActionListener radioListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (e.getSource() == selectServerMode) {
               listeningPortInput.setEnabled(true);
               hostInput.setEnabled(false);
               connectPortInput.setEnabled(false);
               listeningPortInput.setEditable(true);
               hostInput.setEditable(false);
               connectPortInput.setEditable(false);
            }
            else {
               listeningPortInput.setEnabled(false);
               hostInput.setEnabled(true);
               connectPortInput.setEnabled(true);
               listeningPortInput.setEditable(false);
               hostInput.setEditable(true);
               connectPortInput.setEditable(true);
            }
         }
      };
      selectServerMode.addActionListener(radioListener);
      selectClientMode.addActionListener(radioListener);
      selectServerMode.setSelected(true);
      hostInput.setEnabled(false);
      connectPortInput.setEnabled(false);
      hostInput.setEditable(false);
      connectPortInput.setEditable(false);
      
      
      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new GridLayout(0,1,5,5));
      inputPanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(Color.BLACK, 2),
                 BorderFactory.createEmptyBorder(6,6,6,6) ));
      
      inputPanel.add(message);
      
      JPanel row;
      
      inputPanel.add(selectServerMode);
      
      row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.LEFT));
      row.add(Box.createHorizontalStrut(40));
      row.add(new JLabel("Listen on port: "));
      row.add(listeningPortInput);
      inputPanel.add(row);
      
      inputPanel.add(selectClientMode);
      
      row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.LEFT));      
      row.add(Box.createHorizontalStrut(40));
      row.add(new JLabel("Computer: "));
      row.add(hostInput);
      inputPanel.add(row);

      row = new JPanel();
      row.setLayout(new FlowLayout(FlowLayout.LEFT));
      row.add(Box.createHorizontalStrut(40));
      row.add(new JLabel("Port Number: "));
      row.add(connectPortInput);
      inputPanel.add(row);
      
      while (true) { 
         int action = JOptionPane.showConfirmDialog(null, inputPanel, "Tetris Battle!", 
               JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      
         if (action != JOptionPane.OK_OPTION)
            return;
         
         if (selectServerMode.isSelected()) {
            int port;
            try {
               port = Integer.parseInt(listeningPortInput.getText().trim());
               if (port <= 0)
                  throw new Exception();
            }
            catch (Exception e) {
               message.setText("Illegal port number!");
               listeningPortInput.selectAll();
               listeningPortInput.requestFocus();
               continue;
            }
            Hub hub;
            try {
               hub = new TetrisHub(port);
            }
            catch (Exception e) {
               message.setText("Error: Can't listen on port " + port);
               listeningPortInput.selectAll();
               listeningPortInput.requestFocus();
               continue;
            }
            try {
               new TetrisWindow("localhost", port);
            }
            catch (IOException e) {
               message.setText("Could not connect to server on localhost!!");
               hub.shutDownHub();
               continue;
            }
            break;
         }
         else {
            String host;
            int port;
            host = hostInput.getText().trim();
            if (host.length() == 0) {
               message.setText("You must enter a computer name!");
               hostInput.requestFocus();
               continue;
            }
            try {
               port = Integer.parseInt(connectPortInput.getText().trim());
               if (port <= 0)
                  throw new Exception();
            }
            catch (Exception e) {
               message.setText("Illegal port number!");
               connectPortInput.selectAll();
               connectPortInput.requestFocus();
               continue;
            }
            try {
               new TetrisWindow(host,port);
            }
            catch (IOException e) {
               message.setText("Could not connect to specified host and port.");
               hostInput.selectAll();
               hostInput.requestFocus();
               continue;
            }
            break;
         }
      }
      
   }
}