package com.vteba.netty.client.bootstrap;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

import com.vteba.netty.client.Client;

public class StartClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
	
	public static void main(String[] args) {
		initLogger();
		
		LOGGER.info(Arrays.toString(args));
		LOGGER.info("开始加载启动Netty Client的Spring配置文件。");
		
		String configLocation = "classpath:application-netty.xml";
		ClassPathXmlApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext(configLocation);
			Client client = context.getBean(Client.class);
			client.start();
		} catch (Exception e) {
			LOGGER.error("启动Netty Client守护线程出错。", e);
		} finally {
			context.close();
		}
	}
	
	/**
	 * 加载log4j的日志
	 */
	public static void initLogger() {
		try {
			Log4jConfigurer.initLogging("classpath:log4j.xml");
		} catch (Exception e) {
			throw new IllegalStateException("没有找到log4j配置文件。", e);
		}
	}
	
	
//	public static volatile boolean connected = false;
//	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//	public static void main(String[] args) {
//		start();
//	}
//	
//	public static void start() {
//		LOGGER.info("Netty Client启动。");
//		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();// 监听线程组，分派任务的主管
//		
//		try {
//			Bootstrap bootstrap = new Bootstrap();
//			bootstrap.group(eventLoopGroup)
//				.channel(NioSocketChannel.class)
//				.option(ChannelOption.TCP_NODELAY, true)
//				.handler(new ChannelInitializer<SocketChannel>() {
//
//					@Override
//					protected void initChannel(SocketChannel ch) throws Exception {
//						ChannelPipeline pipeline = ch.pipeline();
//						/**********ChannelOutboundHandler（发送数据，出去）逆序执行**********/
//						// 2、加上头长度
//						pipeline.addLast("lengthPrepender", new LengthFieldPrepender(4));
//						// 1、将字符转字节数组
//						pipeline.addLast("encoder", new StringEncoder(Char.UTF8));
//						
//						pipeline.addLast("logger", new LoggingHandler(LogLevel.WARN));// 既是Inbound又是Outbound
//						pipeline.addLast("ldleStateHandler", new IdleStateHandler(20, 10, 10));//双工的，既是Inbound又是Outbound
//						
//						/**********ChannelInboundHandler（接受数据，进来）顺序执行*************/
//						// 1、获取去掉头长度的字节数组
//						pipeline.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//						// 2、将数组编码为字符串
//						pipeline.addLast("decoder", new StringDecoder(Char.UTF8));
//						
//						pipeline.addLast("heartBeatHandler", new HeartBeatHandler());// 进行心跳检测，如果是心跳消息，直接跳过下面的业务handler
//						// 3、业务逻辑处理
//						pipeline.addLast("clientHandler", new ClientHandler());
//					}
//				});
//			
//			// 连接server
//			ChannelFuture future = bootstrap.connect("127.0.0.1", 8080).sync();
//			connected = true;
//			for (int i = 0; i < 3; i++) {
//				TimeUnit.SECONDS.sleep(7);
//				// 持有channel就可以重用已经建立的连接了
//				future.channel().write("尹雷等一段时间在发送的。");// 相当于在handler中的channelActive发送消息
//			}
//			future.channel().closeFuture().sync();
//			// 等待直到链接被关闭
//		} catch (Exception e) {
//			connected = false;
//			LOGGER.error("Netty Client启动异常。", e);
//			
//			if (!connected) {
//				scheduler.scheduleAtFixedRate(new Runnable() {
//					
//					@Override
//					public void run() {
//						Client.start();
//					}
//				}, 1, 15, TimeUnit.SECONDS);
//			}
//		} finally {
//			// 关闭线程池，释放资源
//			eventLoopGroup.shutdownGracefully();
//		}
//	}

}
