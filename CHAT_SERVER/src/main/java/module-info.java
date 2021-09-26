module com.example.chat_server {
	requires javafx.controls;
	requires javafx.fxml;

	requires java.sql;
	requires mysql.connector.java;

	requires org.slf4j;

	requires json.simple;

	opens com.example.chat_server to javafx.fxml;
	exports com.example.chat_server;
}