package com.ironyard;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;
import java.sql.*;
import java.util.ArrayList;

public class Main {
    public static void createTables(Connection conn) throws SQLException {

        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS messages (id IDENTITY, author VARCHAR, text VARCHAR)");
    }

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.externalStaticFileLocation("public");
        Spark.init();


        Spark.get(
                "/get-messages",
                ((request, response) -> {
                    ArrayList<Message> messages = selectMessages(conn);
                    JsonSerializer serializer = new JsonSerializer();
                    return serializer.serialize(messages);
                })
        );

        Spark.post(
                "/add-message",
                (request, response) -> {
                    String body = request.body();
                    JsonParser parser = new JsonParser();
                    Message msg = parser.parse(body, Message.class);
                    insertMessage(conn, msg.author, msg.text);
                    return "";
                }
        );

    }//end main()

    public static void insertMessage(Connection conn, String author, String text) throws SQLException{
        PreparedStatement statement = conn.prepareStatement("INSERT INTO messages VALUES (NULL, ?, ?)");
        statement.setString(1, author);
        statement.setString(2, text);
        statement.execute();
    }

    public static ArrayList<Message> selectMessages (Connection conn) throws SQLException {
        ArrayList<Message> messages = new ArrayList<>();

        PreparedStatement statement = conn.prepareStatement("SELECT * FROM messages");
        ResultSet results = statement.executeQuery();

        while(results.next()){
            int id = results.getInt("id");
            String author = results.getString("author");
            String text = results.getString("text");
            messages.add(new Message(id, author, text));
        }
        return messages;
    }//end selectMessages()

}//end Main class
