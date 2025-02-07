import java.util.Scanner;

public class TicTacToe {
    // The game board is a 3x3 char array
    private static char[][] board = new char[3][3];
    // The current player ('X' or 'O')
    private static char currentPlayer = 'X';

    public static void main(String[] args) {
        // Initialize the game board with empty spaces
        initializeBoard();
        // Flag to check if the game has ended
        boolean gameEnded = false;
        // Create a Scanner to read user input
        Scanner scanner = new Scanner(System.in);

        // Main game loop
        while (!gameEnded) {
            printBoard();
            System.out.println("Player " + currentPlayer + ", enter your move (row [1-3] and column [1-3]):");

            // Read user input. Subtract 1 to convert to 0-indexed array.
            int row = scanner.nextInt() - 1;
            int col = scanner.nextInt() - 1;

            // Validate the move
            if (row < 0 || row >= 3 || col < 0 || col >= 3) {
                System.out.println("This move is out of bounds. Please try again.");
                continue;
            }
            if (board[row][col] != ' ') {
                System.out.println("This cell is already occupied. Please try again.");
                continue;
            }

            // Make the move
            board[row][col] = currentPlayer;

            // Check for a winner after the move
            if (checkWinner()) {
                printBoard();
                System.out.println("Player " + currentPlayer + " wins!");
                gameEnded = true;
            } 
            // Check for a tie (board is full)
            else if (isBoardFull()) {
                printBoard();
                System.out.println("The game is a tie!");
                gameEnded = true;
            } 
            // Switch players
            else {
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            }
        }
        scanner.close();
    }

    // Initialize the board with spaces
    private static void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    // Print the current game board
    private static void printBoard() {
        System.out.println("-------------");
        for (int i = 0; i < 3; i++) {
            System.out.print("| ");
            for (int j = 0; j < 3; j++) {
                System.out.print(board[i][j] + " | ");
            }
            System.out.println();
            System.out.println("-------------");
        }
    }

    // Check whether the current player has won the game
    private static boolean checkWinner() {
        // Check rows for a win
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' &&
                board[i][0] == board[i][1] &&
                board[i][1] == board[i][2]) {
                return true;
            }
        }

        // Check columns for a win
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != ' ' &&
                board[0][j] == board[1][j] &&
                board[1][j] == board[2][j]) {
                return true;
            }
        }

        // Check the two diagonals
        if (board[0][0] != ' ' &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]) {
            return true;
        }
        if (board[0][2] != ' ' &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]) {
            return true;
        }

        return false;
    }

    // Check if the board is full (tie condition)
    private static boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
}
