package com.example.chat_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.Executors;


public class Connector {

	// singleton
	private Connector() {}
	public static Connector getInstance() {
		return LazyHolder.INSTANCE;
	}

	private static class LazyHolder {
		private static final Connector INSTANCE = new Connector();
	}

	// controller
	private ServerController controller;
	public void setController( ServerController in_controller ) {
		this.controller = in_controller;
	}

	// connector
	AsynchronousChannelGroup channelGroup;
	AsynchronousServerSocketChannel serverSocketChannel;
	private final HashMap< String, Client > connections = new HashMap<>();

	private final ChatServer chatServer = new ChatServer();

	private boolean bRunning = false;
	public boolean IsRunning() { return bRunning; }

	void startServer() {
		try {
			channelGroup = AsynchronousChannelGroup.withFixedThreadPool( Runtime.getRuntime( ).availableProcessors(),
					Executors.defaultThreadFactory()
			);
			serverSocketChannel = AsynchronousServerSocketChannel.open( channelGroup );
			serverSocketChannel.bind( new InetSocketAddress(5001 ));
		} catch( Exception e ) {
			if( serverSocketChannel.isOpen( )) { stopServer(); }
			return;
		}

		controller.AppendText( "[서버 시작]\n" );
		controller.ChangeButtonText( "Server Stop" );
		controller.ChangeLabelText( "Server is running!" );

		bRunning = true;

		serverSocketChannel.accept( null, new CompletionHandler<AsynchronousSocketChannel, Void>( ) {
			@Override
			public void completed( AsynchronousSocketChannel socketChannel, Void attachment ) {
				try {
					String message = "[연결 수락: " + socketChannel.getRemoteAddress()  + ": " + Thread.currentThread().getName() + "]\n";
					controller.AppendText( message );

					Client client = new Client( socketChannel );
					connections.put( socketChannel.getRemoteAddress().toString(), client );
					controller.AppendText( "[연결 개수: " + connections.size( ) + "]\n" );

				} catch ( IOException e ) {}

				serverSocketChannel.accept( null, this );
			}
			@Override
			public void failed( Throwable exc, Void attachment ) {
				if( serverSocketChannel.isOpen( )) { stopServer(); }
			}
		});
	}

	void stopServer() {
		try {
			connections.clear();
			if( channelGroup!=null && !channelGroup.isShutdown( )) {
				channelGroup.shutdownNow();
			}

			controller.AppendText( "[서버 멈춤]\n" );
			controller.ChangeButtonText( "Server Start" );
			controller.ChangeLabelText( "Server is down!" );
			bRunning = false;

			chatServer.disconnect();

		} catch ( Exception e ) {}
	}

	void send( String in_address, String in_data ) {
		Client client = connections.get( in_address );
		if( null != client ) {
			client.send( in_data );
		}
	}

	void sendAll( String in_data) {
		for( HashMap.Entry< String, Client > entry : connections.entrySet() ) {
			entry.getValue().send(in_data);
		}
	}

	class Client {
		AsynchronousSocketChannel socketChannel;

		Client( AsynchronousSocketChannel socketChannel ) {
			this.socketChannel = socketChannel;
			receive();
		}

		void receive() {
			ByteBuffer byteBuffer = ByteBuffer.allocate( 100 );
			socketChannel.read( byteBuffer, byteBuffer, new CompletionHandler<>( ) {
				@Override
				public void completed( Integer result, ByteBuffer attachment ) {
					try {
						String message = "[요청 처리: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]\n";
						controller.AppendText( message );

						attachment.flip();
						Charset charset = Charset.forName( "utf-8" );
						String data = charset.decode( attachment ).toString();

						// 패킷 처리
						chatServer.processData( socketChannel.getRemoteAddress().toString(), data );

						ByteBuffer byteBuffer = ByteBuffer.allocate( 100 );
						socketChannel.read( byteBuffer, byteBuffer, this );
					} catch( Exception e ) {}
				}
				@Override
				public void failed( Throwable exc, ByteBuffer attachment ) {
					try {
						String message = "[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]\n";
						controller.AppendText( message );

						chatServer.disconnectClient( socketChannel.getRemoteAddress().toString() );
						connections.remove( socketChannel.getRemoteAddress().toString() );
						socketChannel.close();
					} catch ( IOException e ) {}
				}
			});
		}

		void send( String in_data ) {
			Charset charset = Charset.forName( "utf-8" );
			ByteBuffer byteBuffer = charset.encode( in_data );
			socketChannel.write( byteBuffer, null, new CompletionHandler<Integer, Void>( ) {
				@Override
				public void completed( Integer result, Void attachment ) {
				}
				@Override
				public void failed( Throwable exc, Void attachment ) {
					try {
						String message = "[클라이언트 통신 안됨: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]\n";
						controller.AppendText( message );

						chatServer.disconnectClient( socketChannel.getRemoteAddress().toString() );
						connections.remove( socketChannel.getRemoteAddress().toString() );
						socketChannel.close();
					} catch ( IOException e ) {}
				}
			});
		}
	}

}
