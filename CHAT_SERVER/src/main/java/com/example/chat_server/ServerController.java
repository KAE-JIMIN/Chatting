package com.example.chat_server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ServerController {
	@FXML private Label		ServerState;
	@FXML private TextArea	txtContent;
	@FXML private Button	btnServer;

	@FXML protected void onServerStartClick() {
		if( Connector.getInstance( ).IsRunning() ) {
			Connector.getInstance().stopServer();
		} else {
			Connector.getInstance().startServer();
		}
	}

	public void AppendText( String in_text ) {
		txtContent.appendText( in_text );
	}
	public void ChangeButtonText( String in_text ) { btnServer.setText( in_text ); }
	public void ChangeLabelText( String in_text ) { ServerState.setText( in_text ); }

}