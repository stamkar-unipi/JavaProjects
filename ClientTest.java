package gr.unipi.opentriviaapi;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a local HTTP server on an ephemeral port.
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Test that the default constructor builds the expected URL.
     */
    @Test
    public void testDefaultConstructorUrl() throws Exception {
        Client client = new Client();
        Field uriField = Client.class.getDeclaredField("trivia_uri");
        uriField.setAccessible(true);
        String url = (String) uriField.get(client);
        assertEquals("https://opentdb.com/api.php?amount=10", url);
    }

    /**
     * Test that the constructor taking only the amount builds the expected URL.
     */
    @Test
    public void testAmountConstructorUrl() throws Exception {
        int amount = 5;
        Client client = new Client(amount);
        Field uriField = Client.class.getDeclaredField("trivia_uri");
        uriField.setAccessible(true);
        String url = (String) uriField.get(client);
        assertEquals("https://opentdb.com/api.php?amount=" + amount, url);
    }

    /**
     * Test that the custom constructor builds the expected URL with additional parameters.
     */
    @Test
    public void testCustomConstructorUrl() throws Exception {
        int amount = 5;
        int category = 21;
        String difficulty = "medium";
        String type = "multiple";
        Client client = new Client(amount, category, difficulty, type);
        Field uriField = Client.class.getDeclaredField("trivia_uri");
        uriField.setAccessible(true);
        String url = (String) uriField.get(client);
        String expected = "https://opentdb.com/api.php?amount=" + amount +
                          "&category=" + category +
                          "&difficulty=" + difficulty +
                          "&type=" + type;
        assertEquals(expected, url);
    }

    /**
     * Test that fetchData() correctly parses a successful JSON response.
     */
    @Test
    public void testFetchDataSuccess() throws Exception {
        // Prepare a JSON response that matches the expected format.
        String jsonResponse = "{\"response_code\":0, \"results\": ["
                + "{\"category\":\"Science: Computers\","
                + "\"type\":\"multiple\","
                + "\"difficulty\":\"easy\","
                + "\"question\":\"What is 1+1?\","
                + "\"correct_answer\":\"2\","
                + "\"incorrect_answers\":[\"3\",\"4\",\"5\"]}"
                + "]}";

        // Create a context that returns the JSON response with HTTP 200.
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] responseBytes = jsonResponse.getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        });
        server.start();

        // Create a client instance and override its URL to point to the local server.
        Client client = new Client();
        overrideClientUrl(client, "http://localhost:" + port + "/");

        // Call fetchData and verify the returned data.
        Client.DataResponse dataResponse = client.fetchData();
        assertNotNull(dataResponse);
        assertEquals(0, dataResponse.getResponseCode());
        assertNotNull(dataResponse.getResults());
        assertEquals(1, dataResponse.getResults().length);

        Client.DataQuestion question = dataResponse.getResults()[0];
        assertEquals("Science: Computers", question.getCategory());
        assertEquals("multiple", question.getType());
        assertEquals("easy", question.getDifficulty());
        assertEquals("What is 1+1?", question.getQuestion());
        assertEquals("2", question.getCorrectAnswer());
        assertArrayEquals(new String[]{"3", "4", "5"}, question.getIncorrectAnswers());
    }

    /**
     * Test that fetchData() throws an exception when the HTTP status is not 200.
     */
    @Test
    public void testFetchDataHttpError() throws Exception {
        // Prepare a simple error response.
        String errorResponse = "Not Found";

        // Create a context that returns an error (HTTP 404).
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] responseBytes = errorResponse.getBytes();
                exchange.sendResponseHeaders(404, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        });
        server.start();

        // Create a client instance and override its URL to point to the local server.
        Client client = new Client();
        overrideClientUrl(client, "http://localhost:" + port + "/");

        // Expect an exception because the response code is not 200.
        Exception ex = assertThrows(Exception.class, client::fetchData);
        assertTrue(ex.getMessage().contains("Status error code: 404"));
    }

    /**
     * Helper method that uses reflection to override the private 'trivia_uri' field of Client.
     *
     * @param client the Client instance to modify.
     * @param newUrl the new URL to set.
     * @throws Exception if reflection fails.
     */
    private void overrideClientUrl(Client client, String newUrl) throws Exception {
        Field uriField = Client.class.getDeclaredField("trivia_uri");
        uriField.setAccessible(true);
        uriField.set(client, newUrl);
    }
}
