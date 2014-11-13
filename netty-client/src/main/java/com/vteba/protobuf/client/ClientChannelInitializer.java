package com.vteba.protobuf.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.inject.Inject;
import javax.inject.Named;

import com.vteba.netty.protobuf.HeartBeatHandler;
import com.vteba.protobuf.AddressBookProtos.AddressBook;

/**
 * Client Channel初始化器
 * @author yinlei
 * @date 2014-6-22
 */
@Named
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	@Inject
	private ClientHandler clientHandler;
	
	@Inject
	private OfflineReconnectHandler offlineReconnectHandler;
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		/**********ChannelOutboundHandler（发送数据，出去）逆序执行**********/
		// 2、加上头长度
//		pipeline.addLast("lengthPrepender", new LengthFieldPrepender(4));
		pipeline.addLast("protobufVarint32LengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender());
		// 1、将字符转字节数组
//		pipeline.addLast("encoder", new StringEncoder(Char.UTF8));
		pipeline.addLast("encoder", new ProtobufEncoder());
		
		pipeline.addLast("logger", new LoggingHandler(LogLevel.INFO));// 既是Inbound又是Outbound
		pipeline.addLast("ldleStateHandler", new IdleStateHandler(20, 10, 10));//双工的，既是Inbound又是Outbound
		
		/**********ChannelInboundHandler（接受数据，进来）顺序执行*************/
		// 1、获取去掉头长度的字节数组
//		pipeline.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
		pipeline.addLast("protobufVarint32FrameDecoder", new ProtobufVarint32FrameDecoder());
		
//		ExtensionRegistry registry = ExtensionRegistry.newInstance();
		pipeline.addLast("protobufDecoder", new ProtobufDecoder(AddressBook.getDefaultInstance()));
		// 2、将数组编码为字符串
//		pipeline.addLast("decoder", new StringDecoder(Char.UTF8));
		
		pipeline.addLast("heartBeatHandler", new HeartBeatHandler());// 进行心跳检测，如果是心跳消息，直接跳过下面的业务handler
		pipeline.addLast("reconnectHandler", offlineReconnectHandler);
		// 3、业务逻辑处理
		pipeline.addLast("clientHandler", clientHandler);
	}

}
