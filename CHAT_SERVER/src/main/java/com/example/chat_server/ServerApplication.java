package com.example.chat_server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApplication extends Application {

	@Override
	public void start( Stage stage ) throws IOException {

		FXMLLoader fxmlLoader = new FXMLLoader( ServerApplication.class.getResource("server-view.fxml" ));
		Scene scene = new Scene( fxmlLoader.load( ), 400, 360);
		stage.setTitle( "Asynchronous Server" );
		stage.setScene( scene );

		ServerController controller = fxmlLoader.getController();
		Connector.getInstance().setController( controller );

		stage.setOnCloseRequest( event->Connector.getInstance().stopServer() );
		stage.show();
	}

	public static void main( String[] args ) {
		launch();
	}
}