package com.example.chat_client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class ClientController {
	@FXML private Pane		paneSignIn;
	@FXML private TextField txtID;
	@FXML private TextField txtPassword;

	@FXML private Pane		paneSignUp;
	@FXML private TextField txtID_u;
	@FXML private TextField txtPassword_u;
	@FXML private TextField txtNickname_u;

	@FXML private Pane	  	paneMessenger;
	@FXML private TextArea  txtContent;
	@FXML private TextField txtMessage;

	@FXML protected void OnSignUpClick() {
		paneSignIn.setVisible( false );
		paneSignUp.setVisible( true );
	}

	@FXML protected void OnSignInClick() {
		String id = txtID.getText();
		String password = txtPassword.getText();
		ChatClient.getInstance().RequestSignIn( id, password );
	}
	@FXML protected void OnKeyPressed_SignIn( KeyEvent keyEvent ) {
		if( keyEvent.getCode().equals( KeyCode.ENTER ) ) {
			OnSignInClick();
		}
	}

	@FXML protected void OnRequestSignUpClick() {
		String id = txtID_u.getText();
		String password = txtPassword_u.getText();
		String nickname = txtNickname_u.getText();
		ChatClient.getInstance().RequestSignUp( id, password, nickname );
	}

	@FXML public void OnReturnClick() {
		paneSignUp.setVisible( false );
		paneSignIn.setVisible( true );
	}

	@FXML protected void onSendClick() {
		ChatClient.getInstance().SendMessage( txtMessage.getText() );
		txtMessage.setText( "" );
	}
	@FXML protected void OnKeyPressed( KeyEvent keyEvent ) {
		if( keyEvent.getCode().equals( KeyCode.ENTER ) ) {
			onSendClick();
		}
	}

	public void SetMessengerMode() {
		paneSignIn.setVisible( false );
		paneMessenger.setVisible( true );
	}

	public void AppendText( String in_text ) {
		txtContent.appendText( in_text );
	}

	void MessageBox( String in_message )
	{
		Alert alert = new Alert( Alert.AlertType.INFORMATION );
		alert.setTitle( "Information Dialog" );
		alert.setHeaderText( null );
		alert.setContentText( in_message );

		alert.showAndWait();
	}
}