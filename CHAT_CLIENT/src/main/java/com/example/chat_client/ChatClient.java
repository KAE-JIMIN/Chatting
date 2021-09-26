package com.example.chat_client;

import javafx.application.Platform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class ChatClient {

	// singleton
	private ChatClient() {}
	public static ChatClient getInstance() { return LazyHolder.INSTANCE; }

	private static class LazyHolder {
		private static final ChatClient INSTANCE = new ChatClient();
	}

	// controller
	private ClientController controller;
	public void setController( ClientController in_controller ) {
		this.controller = in_controller;
	}

	private final Connector connector = new Connector();

	void connect() {
		connector.startClient();
	}

	void disconnect() {
		connector.stopClient();
	}

	boolean processData( String in_data ) {

		JSONParser parser = new JSONParser();
		JSONObject obj = null;

		try {
			obj = ( JSONObject )parser.parse( in_data );
		} catch ( ParseException e ) {
			System.out.println( "변환에 실패" );
			e.printStackTrace();
		}

		String type = obj.get( "type" ).toString();
		switch ( type ) {
			case "signUp" -> {
				{
					int result = Integer.parseInt( obj.get("result" ).toString() );
					if ( 0 == result ) {
						controller.OnReturnClick();
					} else if ( -1 == result ) {
						Platform.runLater( () -> controller.MessageBox( "이미 사용 중인 ID입니다." ) );
					} else {
						Platform.runLater( () -> controller.MessageBox( "회원 가입에 실패하였습니다." ) );
					}
				}
				return false;
			}
			case "signIn" -> {
				int result = Integer.parseInt( obj.get("result" ).toString() );
				if ( 0 == result ) {
					Platform.runLater( () -> controller.MessageBox( "로그인되었습니다." ) );
					controller.SetMessengerMode();
					return true;
				} else if ( -1 == result ) {
					Platform.runLater( () -> controller.MessageBox( "회원 가입이 필요합니다." ) );
				} else if ( -2 == result ) {
					Platform.runLater( () -> controller.MessageBox( "비밀번호가 일치하지 않습니다." ) );
				} else {
					Platform.runLater( () -> controller.MessageBox( "로그인에 실패하였습니다." ) );
				}
			}
			case "message" -> {
				{
					String message = obj.get( "message" ).toString() + "\n";
					controller.AppendText( message );
				}
				return true;
			}
		}

		return false;
	}

	void RequestSignUp( String in_id, String in_password, String in_nickname ) {
		if( in_id.isEmpty() || in_password.isEmpty() || in_nickname.isEmpty() )
			return ;

		JSONObject obj = new JSONObject();
		obj.put( "type", "signUp" );
		obj.put( "id", in_id );
		obj.put( "password", in_password );
		obj.put( "nickname", in_nickname );

		connect();
		connector.send( obj.toString() );
	}

	void RequestSignIn( String in_id, String in_password ) {
		if( in_id.isEmpty() || in_password.isEmpty() )
			return ;

		JSONObject obj = new JSONObject();
		obj.put( "type", "signIn" );
		obj.put( "id", in_id );
		obj.put( "password", in_password );

		connect();
		connector.send( obj.toString() );
	}

	void SendMessage( String in_message ) {
		if( in_message.isEmpty() )
			return ;

		JSONObject obj = new JSONObject();
		obj.put( "type", "message" );
		obj.put( "message", in_message );

		connector.send( obj.toString() );
	}
}
