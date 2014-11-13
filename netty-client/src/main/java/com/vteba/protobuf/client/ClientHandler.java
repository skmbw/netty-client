package com.vteba.protobuf.client;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.vteba.protobuf.AddressBookProtos.AddressBook;
import com.vteba.protobuf.AddressBookProtos.Person;
import com.vteba.protobuf.AddressBookProtos.Person.PhoneNumber;
import com.vteba.protobuf.AddressBookProtos.Person.PhoneType;

/**
 * 客户端Handler，业务处理
 * @author yinlei
 * @since 2014-6-22
 */
@Named
@Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.write(proto());
		ctx.flush();
	}
	
	public AddressBook proto() {
		AddressBook.Builder builder = AddressBook.newBuilder();
		
		Person.Builder personBuilder =  Person.newBuilder();
		personBuilder.setEmail("tongku2008@126.com");
		personBuilder.setId(2014);
		personBuilder.setName("尹雷yinlei");
		
		PhoneNumber.Builder phoneNumberBuilder = PhoneNumber.newBuilder();
		phoneNumberBuilder.setNumber("13815712222");
		phoneNumberBuilder.setType(PhoneType.HOME);
		
		personBuilder.addPhone(phoneNumberBuilder);
		
		Person person = personBuilder.build();
		builder.addPerson(person);
		
		AddressBook addressBook = builder.build();
		return addressBook;
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
//		client.setConnected(false);
		LOGGER.error("发生异常信息，", cause.getMessage());
		ctx.close();// 发生异常，关闭链接
	}

}
