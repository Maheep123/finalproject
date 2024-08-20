package tictactoeserver;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 *
 * @author aid
 */
public class Client extends JFrame implements Runnable {

    // Declaring constants
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static final int PLAYER1_WON = 1;
    public static final int PLAYER2_WON = 2;
    public static final int DRAW = 3;
    public static final int CONTINUE = 4;

    Socket socket;
    private boolean myTurn = false;
    private char myToken = ' ', otherToken = ' ';
    private Cell[][] cell = new Cell[3][3];
    private JLabel titleLabel = new JLabel();
    private JLabel statusLabel = new JLabel();

    private int rowSelected;
    private int columnSelected;

    private DataInputStream fromServer;
    private DataOutputStream toServer;

    private boolean continueToPlay = true;
    private boolean waiting = true;
    private boolean isStandAlone = false;

    // Constructor to set up the UI and initialize the connection
    public Client() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(3, 3, 0, 0));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                p.add(cell[i][j] = new Cell(i, j));
            }
        }

        p.setBorder(new LineBorder(Color.black, 1));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(new LineBorder(Color.black, 1));
        statusLabel.setBorder(new LineBorder(Color.black, 1));

        add(titleLabel, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        connectToServer();

        // Set up JFrame properties
        setTitle("Tic Tac Toe Client");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Method for connecting to server
    private void connectToServer() {
        try {
            // If it is standalone connect to the localhost
            if (isStandAlone)
                socket = new Socket("localhost", 8000);
            else
                socket = new Socket("localhost", 8000);  // Updated to localhost for standalone app

            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.err.println(ex);
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            // Read which player
            int player = fromServer.readInt();

            // If first player, set the token to X and wait for second player to join
            if (player == PLAYER1) {
                myToken = 'X';
                otherToken = 'O';
                titleLabel.setText("Player 1 with token 'X'");
                statusLabel.setText("Waiting for player 2 to join");

                // Notification that player 2 joined
                fromServer.readInt();

                statusLabel.setText("Player 2 has joined. I start first");

                myTurn = true;
            }
            // If second player, then the game can start
            else if (player == PLAYER2) {
                myToken = 'O';
                otherToken = 'X';
                titleLabel.setText("Player 2 with token 'O'");
                statusLabel.setText("Waiting for player 1 to move");
            }

            while (continueToPlay) {
                if (player == PLAYER1) {
                    waitForPlayerAction();
                    sendMove();
                    receiveInfoFromServer();
                } else if (player == PLAYER2) {
                    receiveInfoFromServer();
                    waitForPlayerAction();
                    sendMove();
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (InterruptedException ex) {
        }
    }

    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }
        waiting = true;
    }

    private void sendMove() throws IOException {
        toServer.writeInt(rowSelected);
        toServer.writeInt(columnSelected);
    }

    private void receiveInfoFromServer() throws IOException {
        int status = fromServer.readInt();
        if (status == PLAYER1_WON) {
            continueToPlay = false;
            if (myToken == 'X') {
                statusLabel.setText("I Won! (X)");
            } else if (myToken == 'O') {
                statusLabel.setText("Player 1 (X) has won!");
                receiveMove();
            }
        } else if (status == PLAYER2_WON) {
            continueToPlay = false;
            if (myToken == 'O') {
                statusLabel.setText("I Won! (O)");
            } else if (myToken == 'X') {
                statusLabel.setText("Player 2 (O) has won!");
                receiveMove();
            }
        } else if (status == DRAW) {
            continueToPlay = false;
            statusLabel.setText("Game is over, no winner!");

            if (myToken == 'O') {
                receiveMove();
            }
        } else {
            receiveMove();
            statusLabel.setText("My turn");
            myTurn = true;
        }
    }

    private void receiveMove() throws IOException {
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        cell[row][column].setToken(otherToken);
    }

    public class Cell extends JPanel {
        private int row, column;
        private char token = ' ';

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            setBorder(new LineBorder(Color.black, 1));
            addMouseListener(new ClickListener());
        }

        public char getToken() {
            return token;
        }

        public void setToken(char c) {
            token = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (token == 'X') {
                g.drawLine(10, 10, getWidth() - 10, getHeight() - 10);
                g.drawLine(getWidth() - 10, 10, 10, getHeight() - 10);
            } else if (token == 'O') {
                g.drawOval(10, 10, getWidth() - 20, getHeight() - 20);
            }
        }

        private class ClickListener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((token == ' ') && myTurn) {
                    setToken(myToken);
                    myTurn = false;
                    rowSelected = row;
                    columnSelected = column;
                    statusLabel.setText("Waiting for the other player to move");
                    waiting = false;
                }
            }
        }
    }

    // Main method to run the client
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client();
            }
        });
    }
}
