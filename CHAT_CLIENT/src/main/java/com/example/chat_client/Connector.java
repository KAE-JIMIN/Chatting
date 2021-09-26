package com.example.chat_client;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import java.util.concurrent.Executors;
import java.net.InetSocketAddress;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Connector {

	private final Logger logger = LoggerFactory.getLogger( Connector.class );

	// connector
	AsynchronousChannelGroup channelGroup;
	AsynchronousSocketChannel socketChannel;

	void startClient() {
		try {
			channelGroup = AsynchronousChannelGroup.withFixedThreadPool( Runtime.getRuntime().availableProcessors(),
					Executors.defaultThreadFactory()
			);
			socketChannel = AsynchronousSocketChannel.open( channelGroup );
			socketChannel.connect( new InetSocketAddress("localhost", 5001 ), null, new CompletionHandler<Void, Void>() {
				@Override
				public void completed( Void result, Void attachment ) {
					try {
						String message = "[연결 완료: "  + socketChannel.getRemoteAddress() + "]";
						logger.info( message );
						receive();
					} catch ( IOException e ) {}
				}
				@Override
				public void failed( Throwable e, Void attachment ) {
					logger.info( "[서버 통신 안됨]" );
					if( socketChannel.isOpen() ) {
						stopClient();
					}
				}
			});
		} catch ( IOException e ) {}
	}

	void stopClient() {
		try {
			logger.info( "[연결 끊음]" );
			if( channelGroup!=null && !channelGroup.isShutdown() ) {
				channelGroup.shutdownNow();
			}
		} catch ( IOException e ) {}
	}

	void receive() {
		ByteBuffer byteBuffer = ByteBuffer.allocate( 100 );
		socketChannel.read( byteBuffer, byteBuffer, new CompletionHandler<>() {
			@Override
			public void completed( Integer result, ByteBuffer attachment ) {
				try {
					attachment.flip();
					Charset charset = Charset.forName( "utf-8" );
					String data = charset.decode( attachment ).toString();
					logger.info( "[받기 완료] "  + data );

					boolean bConnect = ChatClient.getInstance().processData( data );
					if( !bConnect )
						stopClient();

					ByteBuffer byteBuffer = ByteBuffer.allocate( 100 );
					socketChannel.read( byteBuffer, byteBuffer, this );
				} catch( Exception e ) {}
			}
			@Override
			public void failed( Throwable exc, ByteBuffer attachment ) {
				logger.info( "[서버 통신 안됨]" );
				stopClient();
			}
		});
	}

	void send( String in_data ) {
		Charset charset = Charset.forName( "utf-8" );
		ByteBuffer byteBuffer = charset.encode( in_data );
		socketChannel.write( byteBuffer, null, new CompletionHandler< Integer, Void >() {
			@Override
			public void completed( Integer result, Void attachment ) {
				logger.info( "[보내기 완료]" );
			}
			@Override
			public void failed( Throwable exc, Void attachment ) {
				logger.info( "[서버 통신 안됨]" );
				stopClient();
			}
		});
	}
}
