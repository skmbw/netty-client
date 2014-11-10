package com.vteba.netty.client;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 客户端Handler，业务处理
 * @author yinlei
 * @since 2014-6-22
 */
@Named
@Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
	
	@Inject
	private Client client;
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (!client.isConnected()) {
			Client.scheduler.scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					client.start();
				}
			}, 1, 15, TimeUnit.SECONDS);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		client.setConnected(true);
		String msg = "{\"name\":\"yinlei尹雷\"}";
		ctx.write(msg);
		//ctx.write(readFile());
		ctx.write(msg);
		ctx.flush();
	}
	
	protected String readFile() {
		File file = new File("c:\\g.txt");
		String result = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			result = IOUtils.toString(fileInputStream);
		} catch (IOException e) {
			
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
		JSON.parse(result);
		return result;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		LOGGER.info("从服务器接受的消息是=[{}].", msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// 如果要建立长连接这里不能关闭连接
		//ctx.close();// 接受服务端返回的消息完毕，关闭链接
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		client.setConnected(false);
		LOGGER.error("发生异常信息，", cause.getMessage());
		ctx.close();// 发生异常，关闭链接
	}

}
