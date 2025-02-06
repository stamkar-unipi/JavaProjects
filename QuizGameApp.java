package gr.unipi.quizgame;

//imports java libraries for GUI
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerNumberModel;

//imports the api developed as library jar
import gr.unipi.opentriviaapi.Client;
import gr.unipi.opentriviaapi.Client.DataResponse;
import gr.unipi.opentriviaapi.Client.DataQuestion;

public class QuizGameApp {

    // Variables to hold maximum score and previous game parameters.
    private static Integer maxScore = null;
    private static GameOptions prevOptions = null;
    
    // Scoring rules
    private static final int CORRECT_SCORE = 10;
    private static final int WRONG_SCORE = -5;

    // Main frame and card layout for swapping between panels.
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Panels for the settings and game.
    private SettingsPanel settingsPanel;
    private GamePanel gamePanel;
    
    public QuizGameApp() {
        frame = new JFrame("Java Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        settingsPanel = new SettingsPanel();
        gamePanel = new GamePanel();
        
        mainPanel.add(settingsPanel, "SETTINGS");
        mainPanel.add(gamePanel, "GAME");
        
        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    // Start the game with the given options.
    private void startGame(GameOptions options) {
        // If game options are different from the previous game, then reset maximum score.
        if (prevOptions != null && !prevOptions.equals(options)) {
            maxScore = null;
        }
        prevOptions = options;
        
        // Retrieve questions from the middle library
        List<Question> questions = fetchQuestions(options);
        if (questions == null || questions.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Could not fetch questions from API.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        gamePanel.setQuestions(questions, options);
        cardLayout.show(mainPanel, "GAME");
    }
    
   //Fetch data from the library jar and populate the questions in a list
    private List<Question> fetchQuestions(GameOptions options) {
        List<Question> questionsList = new ArrayList<>();
        try {
            Client client;
            if (options.category.equals("Any") 
            	    && options.difficulty.equals("Any") 
            	    && options.type.equals("Any")) {
            	    client = new Client(options.number);
            	} else {
            	    int categoryInt = options.category.equals("Any") ? 0 : Integer.parseInt(options.categoryCode);
            	    String difficultyParam = options.difficulty.equals("Any") ? "" : options.difficulty.toLowerCase();
            	    String typeParam = options.type.equals("Any") ? "" : options.type.toLowerCase();
            	    client = new Client(options.number, categoryInt, difficultyParam, typeParam);
            	}
            
            DataResponse response = client.fetchData();
            // Check if API response code indicates success (0 means success)
            //otherwise displays error message
            if (response.getResponseCode() != 0) {
            	return null;
            }
            DataQuestion[] dataQuestions = response.getResults();
            for (DataQuestion dq : dataQuestions) {
                String questionText = htmlDecode(dq.getQuestion());
                String correctAnswer = htmlDecode(dq.getCorrectAnswer());
                List<String> incorrectAnswers = new ArrayList<>();
                for (String ia : dq.getIncorrectAnswers()) {
                    incorrectAnswers.add(htmlDecode(ia));
                }
                Question q = new Question(questionText, correctAnswer, incorrectAnswers, dq.getType());
                questionsList.add(q);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return questionsList;
    }
    
    // remove html special chars for displaying the data more clearly
    private String htmlDecode(String s) {
        return s.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }
    
    // Inner class representing game options.
    //It's a middle class to represent the state before calling the jar
    private static class GameOptions {
        String category; // e.g., "Any" or "General Knowledge"
        String categoryCode; // For API (e.g., "9" for General Knowledge)
        String difficulty; // "Any", "Easy", "Medium", "Hard"
        String type;       // "Any", "Multiple", "Boolean"
        int number;
        
        public GameOptions(String category, String categoryCode, String difficulty, String type, int number) {
            this.category = category;
            this.categoryCode = categoryCode;
            this.difficulty = difficulty;
            this.type = type;
            this.number = number;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof GameOptions)) return false;
            GameOptions other = (GameOptions) o;
            return category.equals(other.category) &&
                   difficulty.equals(other.difficulty) &&
                   type.equals(other.type) &&
                   number == other.number;
        }
    }
    
    // Class representing a quiz question.
    private static class Question {
        String questionText;
        String correctAnswer;
        List<String> incorrectAnswers;
        String type; // "multiple" or "boolean"
        
        public Question(String questionText, String correctAnswer, List<String> incorrectAnswers, String type) {
            this.questionText = questionText;
            this.correctAnswer = correctAnswer;
            this.incorrectAnswers = incorrectAnswers;
            this.type = type;
        }
    }
    
    // Panel to select game settings.
    private class SettingsPanel extends JPanel {
        private JComboBox<String> categoryCombo;
        private JComboBox<String> difficultyCombo;
        private JComboBox<String> typeCombo;
        private JSpinner numberSpinner;
        private JButton defaultGameButton;
        private JButton customGameButton;
        
        // Example categories – you can add more or map them to API category codes.
        // The second element in each array is the category code expected by the Client class from jar file.
        private String[][] categories = {
        		{"Any", "0"},
        		{"General knowledge", "9"},
        		{"Books", "10"},
        		{"Film", "11"},
        		{"Music", "12"},
        		{"Musical and Theaters", "13"},
        		{"Television", "14"},
        		{"Video Games", "15"},
        		{"Board Games", "16"},
        		{"Science and Nature", "17"},
        		{"Computers", "18"},
        		{"Mathematics", "19"},
        		{"Mythology", "20"},
        		{"Sports", "21"},
        		{"Geography", "22"},
        		{"History", "23"},
        		{"Politics", "24"},
        		{"Art", "25"},
        		{"Celebrities", "26"},
        		{"Animals", "27"},
        		{"Vehicles", "28"},
        		{"Comics", "29"},
        		{"Gadgets", "30"},
        		{"Japanese Anime and Manga", "31"},
        		{"Cartoon and Animations", "32"}
        };
        
        public SettingsPanel() {
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,5,5,5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Category
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel("Category:"), gbc);
            
            gbc.gridx = 1;
            String[] categoryNames = new String[categories.length];
            for (int i = 0; i < categories.length; i++) {
                categoryNames[i] = categories[i][0];
            }
            categoryCombo = new JComboBox<>(categoryNames);
            add(categoryCombo, gbc);
            
            // Difficulty
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(new JLabel("Difficulty:"), gbc);
            gbc.gridx = 1;
            String[] difficulties = {"Any", "Easy", "Medium", "Hard"};
            difficultyCombo = new JComboBox<>(difficulties);
            add(difficultyCombo, gbc);
            
            // Type
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(new JLabel("Type:"), gbc);
            gbc.gridx = 1;
            String[] types = {"Any", "Multiple", "Boolean"};
            typeCombo = new JComboBox<>(types);
            add(typeCombo, gbc);
            
            // Number of questions
            gbc.gridx = 0;
            gbc.gridy = 3;
            add(new JLabel("Number:"), gbc);
            gbc.gridx = 1;
            numberSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
            add(numberSpinner, gbc);
            
            // Buttons panel
            JPanel buttonPanel = new JPanel();
            defaultGameButton = new JButton("Start New Game (with default settings)");
            customGameButton = new JButton("Start New Game (with your settings)");
            buttonPanel.add(defaultGameButton);
            buttonPanel.add(customGameButton);
            
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            add(buttonPanel, gbc);
            
            // Button listeners
            defaultGameButton.addActionListener(e -> {
                // Default settings – for example, Any category, Any difficulty, Any type, 10 questions.
                GameOptions options = new GameOptions("Any", "0", "Any", "Any", 10);
                startGame(options);
            });
            
            customGameButton.addActionListener(e -> {
                String category = (String) categoryCombo.getSelectedItem();
                // Find category code:
                String categoryCode = "";
                for (String[] cat : categories) {
                    if (cat[0].equals(category)) {
                        categoryCode = cat[1];
                        break;
                    }
                }
                String difficulty = (String) difficultyCombo.getSelectedItem();
                String type = (String) typeCombo.getSelectedItem();
                int number = (Integer) numberSpinner.getValue();
                GameOptions options = new GameOptions(category, categoryCode, difficulty, type, number);
                startGame(options);
            });
        }
    }
    
    // Panel for playing the game.
  // Panel for playing the game.
private class GamePanel extends JPanel {
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctCount = 0; // New variable to count correct answers
    private GameOptions options;
    
    // UI components for the question.
    private JLabel questionLabel;
    private JPanel answersPanel;
    private JButton submitButton;
    
    // To hold answer choices (radio buttons).
    private ButtonGroup answerGroup;
    
    public GamePanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        
        questionLabel = new JLabel("Question");
        add(questionLabel, BorderLayout.NORTH);
        
        answersPanel = new JPanel();
        add(answersPanel, BorderLayout.CENTER);
        
        submitButton = new JButton("Submit Answer");
        add(submitButton, BorderLayout.SOUTH);
        
        submitButton.addActionListener(e -> handleAnswer());
    }
    
    public void setQuestions(List<Question> questions, GameOptions options) {
        this.questions = questions;
        this.options = options;
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.correctCount = 0; // Reset the correct answer count for a new game
        showQuestion();
    }
    
    private void showQuestion() {
        answersPanel.removeAll();
        answerGroup = new ButtonGroup();

        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }
        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText((currentQuestionIndex+1) + ". " + q.questionText);

        List<String> choices = new ArrayList<>();
        // If the question is boolean, manually add both options.
        if (q.type.equalsIgnoreCase("boolean")) {
            choices.add("True");
            choices.add("False");
        } else {
            choices.add(q.correctAnswer);           
            choices.addAll(q.incorrectAnswers);
        }

        // Shuffle the choices so the correct answer isn't always in the same place.
        Collections.shuffle(choices);         

        answersPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;  // make sure to start at row 0

        for (String choice : choices) {
            JRadioButton rb = new JRadioButton(choice);
            // Set the button's action command to its text.
            rb.setActionCommand(choice);
            answerGroup.add(rb);
            answersPanel.add(rb, gbc);
            gbc.gridy++;
        }

        answersPanel.revalidate();
        answersPanel.repaint();
    }

    private void handleAnswer() {
        // Get selected answer
        if (answerGroup.getSelection() == null) {
            JOptionPane.showMessageDialog(frame, "Please select an answer!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selected = answerGroup.getSelection().getActionCommand();
        
        Question q = questions.get(currentQuestionIndex);
        boolean correct = selected.equals(q.correctAnswer);
        if (correct) {
            score += CORRECT_SCORE;
            correctCount++;  // Increment the correct answer count
            JOptionPane.showMessageDialog(frame, "Correct!", "Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            score += WRONG_SCORE;
            JOptionPane.showMessageDialog(frame, "Incorrect!\nThe correct answer was: " + q.correctAnswer, "Result", JOptionPane.INFORMATION_MESSAGE);
        }
        
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            showQuestion();
        } else {
            endGame();
        }
    }
    
    private void endGame() {
        // Calculate success rate as percentage of correct answers.
        int successRate = (int) (((double) correctCount / questions.size()) * 100);
        String message = "Game over!\nYour score: " + score + "\nSuccess rate: " + successRate + "%";
        if (maxScore == null || score > maxScore) {
            if (maxScore != null) {
                message += "\nCongratulations! You beat your previous maximum score of " + maxScore + "!";
            } else {
                message += "\nThis is your first game!";
            }
            maxScore = score;
        } else {
            message += "\nYour maximum score remains: " + maxScore;
        }
        int choice = JOptionPane.showConfirmDialog(frame, message + "\nDo you want to start a new game?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            cardLayout.show(mainPanel, "SETTINGS");
        } else {
            System.exit(0);
        }
    }
}

//we use main method to convert the class into executable app
    public static void main(String[] args) {
    	
        // Run Swing in the Event Dispatch Thread for better UI experience.
        SwingUtilities.invokeLater(() -> {
            new QuizGameApp();
        });
    }
}
