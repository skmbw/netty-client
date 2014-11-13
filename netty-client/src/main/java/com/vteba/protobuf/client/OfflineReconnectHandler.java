package com.vteba.protobuf.client;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vteba.protobuf.AddressBookProtos.AddressBook;
import com.vteba.protobuf.AddressBookProtos.MessageType;

/**
 * 掉线重连处理器
 * @author yinlei
 * @since 2014-6-22
 */
@Named
@Sharable// 语义性检查，不保证线程安全。要自己确认该handler是无状态的，可以共享的，无线程并发问题
public class OfflineReconnectHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(OfflineReconnectHandler.class);
	private ScheduledExecutorService scheduler;
	private volatile AtomicBoolean started = new AtomicBoolean(false);// 是否启动定时，假如有多个连接断掉了，可能会开启多个
	
	@Inject
	private Client client;
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (!client.isConnected() && !started.getAndSet(true)) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Client连接Server掉线，进行重连。");
			}
			getScheduler().scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					client.start(true);
				}
			}, 1, 5, TimeUnit.SECONDS);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		started.set(false);// 断线重连成功，设置为不再重连
		
		AddressBook.Builder builder = AddressBook.newBuilder();
		builder.setType(MessageType.PING);
		ctx.write(builder.build());// 发送ping消息给Server
		ctx.fireChannelActive();// 向下传递事件
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		client.setConnected(false);
		LOGGER.error("发生异常信息，client和server连接断开，将重连。", cause.getMessage());
		ctx.close();// 发生异常，关闭链接
	}

	public ScheduledExecutorService getScheduler() {
		if (scheduler == null) {
			scheduler = Executors.newScheduledThreadPool(1);
		}
		return scheduler;
	}

	public void shutdown() {
		if (scheduler != null) {
			scheduler.shutdown();
			scheduler = null;
		}
	}

}
