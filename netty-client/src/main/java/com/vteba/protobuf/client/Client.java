package com.vteba.protobuf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vteba.utils.common.PropUtils;

/**
 * 客户端启动器，发起调用
 * @author yinlei
 * @since 2014-6-22
 */
@Named
public class Client {
	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
	private volatile AtomicBoolean connected = new AtomicBoolean(false);
	private Channel channel;// 长连接，持有channel就可以重用已经建立的连接了，使用write发送消息
	private Bootstrap bootstrap;
	
	@Inject
	private OfflineReconnectHandler offlineReconnectHandler;
	
	@Inject
	private ChannelInitializer<SocketChannel> clientChannelInitializer;
	
	/**
	 * 连接netty server
	 * @param offline 是否掉线重启
	 */
	public void start(boolean offline) {
		String ip = PropUtils.get("netty.server.ip");
		int port = PropUtils.getInt("netty.server.port");
		
		LOGGER.info(offline ? "Netty Client和Server连接断开，重新连接。" : "Netty Client 连接 Server开始。");
		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		try {
			final Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(eventLoopGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(clientChannelInitializer);
			
			this.bootstrap = bootstrap;// 引导程序的配置启动器
			// 连接server
			ChannelFuture future = bootstrap.connect(ip, port).sync();
			connected.set(true);
			// 连接成功，关闭定时重连的线程池
			offlineReconnectHandler.shutdown();
			
			this.channel = future.channel();// 如果长连接，持有channel，重用连接
			LOGGER.info("Netty Client" + (offline ? "和Server连接断开，重连" : "连接Server") + "[{}:{}]成功。", ip, port);
			
			// 等待直到链接被关闭
			this.channel.closeFuture().sync();
		} catch (Exception e) {
			LOGGER.error("Netty Client " + (offline ? "和Server重连异常，将再次尝试。" : "连接 Server 异常，将重新连接。"), e.getMessage());
		} finally {
			// 关闭线程池，释放资源
			LOGGER.info("Netty Client 连接关闭，释放资源。");
			eventLoopGroup.shutdownGracefully();
		}
	}
	
	@Deprecated
	public void bootstrap(EventLoopGroup eventLoopGroup, String ip, int port) throws InterruptedException {
		final Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(clientChannelInitializer);
		
		this.bootstrap = bootstrap;// 引导程序的配置启动器
		// 连接server
		ChannelFuture future = bootstrap.connect(ip, port).sync();
		connected.set(true);
		// 连接成功，关闭定时重连的线程池
		offlineReconnectHandler.shutdown();
		
		this.channel = future.channel();// 如果长连接，持有channel，重用连接
		LOGGER.info("Netty Client 连接 Server[{}:{}] 成功。", ip, port);
//		
//		for (int i = 0; i < 2; i++) {
//			TimeUnit.SECONDS.sleep(4);
//			// 持有channel就可以重用已经建立的连接了
//			this.channel.write("尹雷等一段时间在发送的" + i);// 相当于在handler中的channelActive发送消息
//		}
		// 等待直到链接被关闭
		this.channel.closeFuture().sync();
	}

	public Channel getChannel() {
		return channel;
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public boolean isConnected() {
		return connected.get();
	}

	public void setConnected(boolean connected) {
		this.connected.set(connected);
	}
}
