package com.example.chat_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class AccountMgr {

	private final Logger logger = LoggerFactory.getLogger( ServerApplication.class );

	Connection con;
	String database = "chat"; // MySQL DATABASE 이름

	public AccountMgr() {
		connect();
	}

	public void connect() {
		Properties jdbcProperties = new Properties();
		jdbcProperties.setProperty( "user", "root" );
		jdbcProperties.setProperty( "password", "1324" );

		String server = "localhost"; // MySQL 서버 주소

		// 1.드라이버 로딩
		try {
			Class.forName( "com.mysql.cj.jdbc.Driver" );
		} catch ( ClassNotFoundException e ) {
			System.err.println( " !! <JDBC 오류> Driver load 오류: " + e.getMessage( ));
			e.printStackTrace();
		}

		// 2.연결
		try {
			con = DriverManager.getConnection( "jdbc:mysql://" + server + "/" + database + "?useSSL=false", jdbcProperties );
			logger.info( "정상적으로 연결되었습니다." );
		} catch( SQLException e ) {
			System.err.println( "con 오류:" + e.getMessage( ));
			e.printStackTrace();
		}
	}

	public void disconnect() {
		// 3.해제
		try {
			if( con != null )
				con.close();
		} catch ( SQLException e ) {}
	}

	public int signUp( String in_id, String in_password, String in_nickname ) {
		int result = -2;	// 0 : 성공
							// -1 : 중복된 ID
							// -2 : DB 오류

		try {
			CallableStatement cstmt = con.prepareCall( "{call " + database + ".signUp(?,?,?,? )}" );

			cstmt.setString( 1, in_id );
			cstmt.setString( 2, in_password );
			cstmt.setString( 3, in_nickname );
			cstmt.registerOutParameter( 4, Types.INTEGER );

			cstmt.execute();
			result = cstmt.getInt( 4 );

		} catch( SQLException e ) {
			System.err.println( "con 오류:" + e.getMessage( ));
			e.printStackTrace();
		}

		return result;
	}

	public int signIn( String in_id, String in_password ) {
		int result = -3;	//  0 : 성공
							// -1 : 없는 ID
							// -2 : 비밀번호 불일치
							// -3 : DB 오류

		try {
			CallableStatement cstmt = con.prepareCall( "{call " + database + ".signIn(?,?,? )}" );

			cstmt.setString( 1, in_id );
			cstmt.setString( 2, in_password );
			cstmt.registerOutParameter( 3, Types.INTEGER );

			cstmt.execute();
			result = cstmt.getInt( 3 );

		} catch( SQLException e ) {
			System.err.println( "con 오류:" + e.getMessage( ));
			e.printStackTrace();
		}

		return result;
	}
	
	public String GetNickName( String in_id ) throws SQLException {
		Statement stmt = con.createStatement();

		String sql = "SELECT nickname FROM membertbl WHERE id = '" + in_id + "'";
		try {
			ResultSet rs = stmt.executeQuery(sql);
			if( rs.next() ) {
				return rs.getString( "nickname" );
			}
		} catch( SQLException e ) {
			System.err.println( "con 오류:" + e.getMessage( ));
			e.printStackTrace();
		}
		return "";
	}
}
