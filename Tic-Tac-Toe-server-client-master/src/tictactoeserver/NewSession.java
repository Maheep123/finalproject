package tictactoeserver;

import java.io.*;
import java.net.*;

class NewSession implements Runnable {

    // Declaring constants
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;
    public static final int PLAYER1_WON = 1;
    public static final int PLAYER2_WON = 2;
    public static final int DRAW = 3;
    public static final int CONTINUE = 4;

    // Sockets
    private Socket firstPlayer;
    private Socket secondPlayer;

    private char[][] cell = new char[3][3];

    public NewSession(Socket firstPlayer, Socket secondPlayer) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cell[i][j] = ' ';
            }
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream fromPlayer1 = new DataInputStream(firstPlayer.getInputStream());
            DataOutputStream toPlayer1 = new DataOutputStream(firstPlayer.getOutputStream());
            DataInputStream fromPlayer2 = new DataInputStream(secondPlayer.getInputStream());
            DataOutputStream toPlayer2 = new DataOutputStream(secondPlayer.getOutputStream());

            // Notify player1 that player2 has joined
            toPlayer1.writeInt(1);

            // Start the game
            while (true) {
                int row = fromPlayer1.readInt();
                int column = fromPlayer1.readInt();
                cell[row][column] = 'X';

                // Check if first player won by playing his move
                if (isWon('X')) {
                    toPlayer1.writeInt(PLAYER1_WON);
                    toPlayer2.writeInt(PLAYER1_WON);
                    sendMove(toPlayer2, row, column);
                    break;
                } else if (isFull()) {
                    toPlayer1.writeInt(DRAW);
                    toPlayer2.writeInt(DRAW);
                    sendMove(toPlayer2, row, column);
                    break;
                } else {
                    toPlayer2.writeInt(CONTINUE);
                    sendMove(toPlayer2, row, column);
                }

                row = fromPlayer2.readInt();
                column = fromPlayer2.readInt();
                cell[row][column] = 'O';

                // Check if second player won
                if (isWon('O')) {
                    toPlayer1.writeInt(PLAYER2_WON);
                    toPlayer2.writeInt(PLAYER2_WON);
                    sendMove(toPlayer1, row, column);
                    break;
                } else if (isFull()) {
                    toPlayer1.writeInt(DRAW);
                    toPlayer2.writeInt(DRAW);
                    sendMove(toPlayer1, row, column);
                    break;
                } else {
                    toPlayer1.writeInt(CONTINUE);
                    sendMove(toPlayer1, row, column);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void sendMove(DataOutputStream out, int row, int column) throws IOException {
        out.writeInt(row);
        out.writeInt(column);
    }

    private boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cell[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isWon(char token) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if ((cell[i][0] == token) && (cell[i][1] == token) && (cell[i][2] == token)) {
                return true;
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if ((cell[0][j] == token) && (cell[1][j] == token) && (cell[2][j] == token)) {
                return true;
            }
        }

        // Check diagonals
        if ((cell[0][0] == token) && (cell[1][1] == token) && (cell[2][2] == token)) {
            return true;
        }

        if ((cell[0][2] == token) && (cell[1][1] == token) && (cell[2][0] == token)) {
            return true;
        }

        return false;
    }
}

