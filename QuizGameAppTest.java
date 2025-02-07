package gr.unipi.quizgame;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Insets;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JSpinner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

public class QuizGameAppTest {

    private QuizGameApp app;

    @BeforeEach
    public void setUp() {
        // Create an instance of the application.
        // (This will launch the UI, so in headless or CI environments you may need to set the system property "java.awt.headless" to "true".)
        app = new QuizGameApp();
    }

    @AfterEach
    public void tearDown() {
        // Optionally, hide or dispose the frame after tests.
        try {
            Field frameField = QuizGameApp.class.getDeclaredField("frame");
            frameField.setAccessible(true);
            JFrame frame = (JFrame) frameField.get(app);
            if (frame != null) {
                frame.dispose();
            }
        } catch (Exception e) {
            // Ignore exceptions during tearDown.
        }
    }

    @Test
    @DisplayName("Test that the htmlDecode method correctly replaces HTML entities")
    public void testHtmlDecode() throws Exception {
        // Access the private htmlDecode(String) method via reflection.
        Method htmlDecodeMethod = QuizGameApp.class.getDeclaredMethod("htmlDecode", String.class);
        htmlDecodeMethod.setAccessible(true);
        
        String input = "Test &quot;hello&quot; &amp; world &#039;example&#039; &lt;tag&gt;";
        String expected = "Test \"hello\" & world 'example' <tag>";
        
        String output = (String) htmlDecodeMethod.invoke(app, input);
        assertEquals(expected, output, "The htmlDecode method did not return the expected result.");
    }

    @Test
    @DisplayName("Test that GameOptions.equals() works correctly")
    public void testGameOptionsEquals() throws Exception {
        // Find the private static inner class GameOptions.
        Class<?>[] declaredClasses = QuizGameApp.class.getDeclaredClasses();
        Class<?> gameOptionsClass = null;
        for (Class<?> c : declaredClasses) {
            if (c.getSimpleName().equals("GameOptions")) {
                gameOptionsClass = c;
                break;
            }
        }
        assertNotNull(gameOptionsClass, "GameOptions inner class was not found.");
        
        // Obtain the constructor: GameOptions(String category, String categoryCode, String difficulty, String type, int number)
        Constructor<?> ctor = gameOptionsClass.getDeclaredConstructor(String.class, String.class, String.class, String.class, int.class);
        ctor.setAccessible(true);
        
        // Create two GameOptions with the same values.
        Object options1 = ctor.newInstance("Any", "0", "Any", "Any", 10);
        Object options2 = ctor.newInstance("Any", "0", "Any", "Any", 10);
        // And one with different parameters.
        Object options3 = ctor.newInstance("General knowledge", "9", "Easy", "Multiple", 15);
        
        // Use the equals() method (which is public) to compare.
        Method equalsMethod = gameOptionsClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);
        boolean eq1 = (Boolean) equalsMethod.invoke(options1, options2);
        boolean eq2 = (Boolean) equalsMethod.invoke(options1, options3);
        
        assertTrue(eq1, "Two GameOptions instances with the same parameters should be equal.");
        assertFalse(eq2, "GameOptions instances with different parameters should not be equal.");
    }

    @Test
    @DisplayName("Test that the main frame is created with the correct title")
    public void testFrameTitle() throws Exception {
        // Access the private frame field.
        Field frameField = QuizGameApp.class.getDeclaredField("frame");
        frameField.setAccessible(true);
        JFrame frame = (JFrame) frameField.get(app);
        assertNotNull(frame, "The frame should not be null.");
        assertEquals("Java Quiz App", frame.getTitle(), "The frame title is not as expected.");
    }

    @Test
    @DisplayName("Test SettingsPanel components are initialized correctly")
    public void testSettingsPanelComponents() throws Exception {
        // Get the private settingsPanel field.
        Field settingsPanelField = QuizGameApp.class.getDeclaredField("settingsPanel");
        settingsPanelField.setAccessible(true);
        Object settingsPanel = settingsPanelField.get(app);
        assertNotNull(settingsPanel, "SettingsPanel should not be null.");
        
        // Test the category combo box.
        Field categoryComboField = settingsPanel.getClass().getDeclaredField("categoryCombo");
        categoryComboField.setAccessible(true);
        @SuppressWarnings("unchecked")
        JComboBox<String> categoryCombo = (JComboBox<String>) categoryComboField.get(settingsPanel);
        assertNotNull(categoryCombo, "Category combo box should not be null.");
        assertEquals("Any", categoryCombo.getItemAt(0), "The first item in the category combo should be 'Any'.");
        
        // Test the difficulty combo box.
        Field difficultyComboField = settingsPanel.getClass().getDeclaredField("difficultyCombo");
        difficultyComboField.setAccessible(true);
        @SuppressWarnings("unchecked")
        JComboBox<String> difficultyCombo = (JComboBox<String>) difficultyComboField.get(settingsPanel);
        assertNotNull(difficultyCombo, "Difficulty combo box should not be null.");
        assertEquals("Any", difficultyCombo.getItemAt(0), "The first item in the difficulty combo should be 'Any'.");
        
        // Test the type combo box.
        Field typeComboField = settingsPanel.getClass().getDeclaredField("typeCombo");
        typeComboField.setAccessible(true);
        @SuppressWarnings("unchecked")
        JComboBox<String> typeCombo = (JComboBox<String>) typeComboField.get(settingsPanel);
        assertNotNull(typeCombo, "Type combo box should not be null.");
        assertEquals("Any", typeCombo.getItemAt(0), "The first item in the type combo should be 'Any'.");
        
        // Test the number spinner.
        Field numberSpinnerField = settingsPanel.getClass().getDeclaredField("numberSpinner");
        numberSpinnerField.setAccessible(true);
        JSpinner numberSpinner = (JSpinner) numberSpinnerField.get(settingsPanel);
        assertNotNull(numberSpinner, "Number spinner should not be null.");
        assertEquals(10, numberSpinner.getValue(), "The default value of the number spinner should be 10.");
    }

    @Test
    @DisplayName("Test GamePanel submit button text")
    public void testGamePanelSubmitButtonText() throws Exception {
        // Get the private gamePanel field.
        Field gamePanelField = QuizGameApp.class.getDeclaredField("gamePanel");
        gamePanelField.setAccessible(true);
        Object gamePanel = gamePanelField.get(app);
        assertNotNull(gamePanel, "GamePanel should not be null.");
        
        // Access the submitButton field inside GamePanel.
        Field submitButtonField = gamePanel.getClass().getDeclaredField("submitButton");
        submitButtonField.setAccessible(true);
        JButton submitButton = (JButton) submitButtonField.get(gamePanel);
        assertNotNull(submitButton, "Submit button should not be null.");
        assertEquals("Submit Answer", submitButton.getText(), "The submit button text is not as expected.");
    }
}
