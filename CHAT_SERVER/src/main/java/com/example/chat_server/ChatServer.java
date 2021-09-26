package com.example.chat_server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.SQLException;
import java.util.HashMap;


public class ChatServer {

	private final AccountMgr accountMgr = new AccountMgr();
	private final HashMap< String, String > nicknameMap = new HashMap<>();

	protected void disconnect() {
		accountMgr.disconnect();
	}

	void processData( String in_address, String in_data ) throws SQLException {

		JSONParser parser = new JSONParser();
		JSONObject obj = null;

		try {
			obj = ( JSONObject )parser.parse( in_data );
		} catch ( ParseException e ) {
			System.out.println( "변환에 실패" );
			e.printStackTrace();
		}

		String type = obj.get( "type" ).toString();
		switch( type ) {
			case "signUp": {
				String id = obj.get( "id" ).toString();
				String password = obj.get( "password" ).toString();
				String nickname = obj.get( "nickname" ).toString();

				int result = accountMgr.signUp( id, password, nickname );

				JSONObject returnObj = new JSONObject();
				returnObj.put( "type", "signUp" );
				returnObj.put( "result", result );

				Connector.getInstance().send( in_address, returnObj.toString() );
			}
			break;

			case "signIn": {
				String id = obj.get( "id" ).toString();
				String password = obj.get( "password" ).toString();

				int result = accountMgr.signIn( id, password );

				if( 0 == result )
				{
					String nickname = accountMgr.GetNickName( id );
					nicknameMap.put( in_address, nickname );
				}

				JSONObject returnObj = new JSONObject();
				returnObj.put( "type", "signIn" );
				returnObj.put( "result", result );

				Connector.getInstance().send( in_address, returnObj.toString() );
			}
			break;

			case "message": {
				String message = obj.get( "message" ).toString();
				String nickname = nicknameMap.get( in_address );
				String data = "[" + nickname + "] : " + message;

				JSONObject returnObj = new JSONObject();
				returnObj.put( "type", "message" );
				returnObj.put( "message", data );

				Connector.getInstance().sendAll( returnObj.toString() );
			}
			break;
		}
	}

	void disconnectClient( String in_address ) {
		nicknameMap.remove( in_address );
	}
}
