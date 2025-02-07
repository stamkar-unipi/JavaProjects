# Use the official OpenJDK image as the base image
FROM openjdk:11

RUN apt-get update && apt-get install -y libxext6 libxrender1 libxtst6

# Set the working directory inside the container
WORKDIR /app

# Copy the Java source code into the container
COPY TicTacToe.java .

# Compile the Java source code
RUN javac TicTacToe.java

# Specify the command to run the application
CMD ["java", "TicTacToe"]
