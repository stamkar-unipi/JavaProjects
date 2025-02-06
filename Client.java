package gr.unipi.opentriviaapi;

//import libraries for http and api actions
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//import external libraries for json handling and deserialization
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Client {
	
	//the base url for getting the questions data
	private String trivia_uri = "https://opentdb.com/api.php?";
	
	private final HttpClient httpClient;
	private final Gson gson;
	
	//default constructor
	public Client() {
		this.httpClient = HttpClient.newHttpClient();
		this.gson = new Gson();
		this.trivia_uri += "amount=10";
	}
	
	//constructor for getting only amount of questions
	
	public Client(int amount) {
		this.httpClient = HttpClient.newHttpClient();
		this.gson = new Gson();
		this.trivia_uri += "amount=" + amount;
	}
	
	//constructor for getting custom data
	public Client(int amount, int category, String difficulty, String type) {
		this.httpClient = HttpClient.newHttpClient();
		this.gson = new Gson();
		ConstructTriviaUriWithParams(amount, category, difficulty, type);
	}
	
	//helper method to construct the final custom url
	private void ConstructTriviaUriWithParams(int amount, int category, String difficulty, String type) {
		this.trivia_uri += "amount=" + amount;
		this.trivia_uri += "&category=" + category;
		this.trivia_uri += "&difficulty=" + difficulty;
		this.trivia_uri += "&type=" + type;
	}
	
	
	//method to get data from json and deserialize them in a type of DataResponse described below
	public DataResponse fetchData() throws Exception {
		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(this.trivia_uri))
				.GET()
				.build();
		
		HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
	
		if(res.statusCode() != 200) {
			throw new Exception("Could not get data. Status error code: " + res.statusCode());
		}
		
		return gson.fromJson(res.body(), DataResponse.class);
		
	}
	
	//deserialization class for json data
	public static class DataResponse{
		
		@SerializedName("response_code")
		private int responseCode;
		
		private DataQuestion[] results;
		
		public int getResponseCode() {
			return responseCode;
		}
		
		public DataQuestion[] getResults() {
			return results;
		}		
		
	}
	
	public static class DataQuestion {
		
		@SerializedName("category")
		private String category;
		
		@SerializedName("type")
        private String type;
		
		@SerializedName("difficulty")
        private String difficulty;
		
		@SerializedName("question")
        private String question;
        
        @SerializedName("correct_answer")
        private String correctAnswer;
        
        @SerializedName("incorrect_answers")
        private String[] incorrectAnswers;

        public String getCategory() {
            return category;
        }

        public String getType() {
            return type;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public String getQuestion() {
            return question;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public String[] getIncorrectAnswers() {
            return incorrectAnswers;
        } 		
	
	}
	
	
	

}
