package com.example.chat_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {

	@Override
	public void start( Stage stage ) throws IOException {

		FXMLLoader fxmlLoader = new FXMLLoader( ClientApplication.class.getResource("client-view.fxml" ) );
		Scene scene = new Scene( fxmlLoader.load(), 400, 360);
		stage.setTitle( "Chat Client" );
		stage.setScene( scene );

		ClientController controller = fxmlLoader.getController();
		ChatClient.getInstance().setController( controller );

		stage.setOnCloseRequest( event->ChatClient.getInstance().disconnect() );
		stage.show();
	}

	public static void main( String[] args ) {
		launch();
	}
}