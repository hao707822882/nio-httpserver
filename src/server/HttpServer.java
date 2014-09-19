package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import bean.Constent;

public class HttpServer {

	public static ServerSocketChannel waiserverSocketChannel;
	public static ServerSocketChannel neiserverSocketChannel;

	public static Selector selector;
	public static Selector workerselector;

	public static Thread acceptThread;
	public static Thread workThread;

	private boolean isrun;

	public static Map clientmap = new HashMap<String, SocketChannel>();
	public static Map worker = new HashMap<String, SocketChannel>();


	public void init() throws IOException {

		workerselector = Selector.open();
		neiserverSocketChannel = ServerSocketChannel.open();
		neiserverSocketChannel.socket().setReuseAddress(true);
		neiserverSocketChannel.configureBlocking(false);
		neiserverSocketChannel.socket().bind(
				new InetSocketAddress(Constent.neiport));

		selector = Selector.open();
		waiserverSocketChannel = ServerSocketChannel.open();
		waiserverSocketChannel.socket().setReuseAddress(true);
		waiserverSocketChannel.configureBlocking(false);
		waiserverSocketChannel.socket().bind(
				new InetSocketAddress(Constent.port));
		System.out.println("服务器启动");

	}

	public static SocketChannel getWorker(String name) {
		synchronized (worker) {
			return (SocketChannel) worker.get(name);
		}
	}

	public static SocketChannel getClient(String name) {
		synchronized (clientmap) {
			return (SocketChannel) clientmap.get(name);
		}
	}

	/****
	 * 
	 * WEB 接受客户的请求
	 * 
	 * *****/
	public void startServer() throws ClosedChannelException {
		isrun = true;
		waiserverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		acceptThread = new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				while (true) {
					int clientnum = 0;
					try {
						clientnum = selector.select();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					if (clientnum == 0)
						continue;
					Set linpai = selector.selectedKeys();
					Iterator it = linpai.iterator();
					SelectionKey key = null;
					while (it.hasNext()) {
						try {
							key = (SelectionKey) it.next();
							it.remove();
							if (key.isAcceptable()) {
								SocketChannel clientchannel = waiserverSocketChannel
										.accept();

								clientchannel.configureBlocking(false);
								// 注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到selector上，之后这个连接的数据，就由这个SocketChannel处理
								clientchannel.register(selector,
										SelectionKey.OP_READ);
								// 将连接的客户端放入map中，以便返回数据时取出socketchannel
								// 为了测试简单编号为“1”
								HttpServer.clientmap.put("1", clientchannel);
								// 将此对应的channel设置为准备接受其他客户端请求
								key.interestOps(SelectionKey.OP_ACCEPT);
								System.out.println("有人连接");
							}
						} catch (Exception e) {
							try {
								key.channel().close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							e.printStackTrace();
						}
						if (key.isReadable()) {// 如果这是可以读取数据
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							StringBuilder content = new StringBuilder();
							try {
								while (sc.read(buff) > 0) {// 大于0说明有的读，=0读完
															// <0错误
									buff.flip();
									content.append(Constent.charest
											.decode(buff));
								}
								String data = content.toString();
								System.out.println("发送的数据" + data);

								SocketChannel workchannel = HttpServer
										.getWorker("1");
								
								ByteBuffer bdata = ByteBuffer
										.wrap(("127.0.0.client" + data)
												.getBytes());
								workchannel.write(bdata);
								// 将此对应的channel设置为准备下一次接受数据
								key.interestOps(SelectionKey.OP_READ);
							} catch (IOException io) {
								key.cancel();
								if (key.channel() != null) {
									try {
										key.channel().close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}

				}
			}
		});
		acceptThread.start();
	}

	/***
	 * 
	 * work接入口
	 * 
	 * ***/
	public void startdispServer() throws ClosedChannelException {

		isrun = true;
		neiserverSocketChannel.register(workerselector, SelectionKey.OP_ACCEPT);
		acceptThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					int clientnum = 0;
					try {
						clientnum = workerselector.select();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					if (clientnum == 0)
						continue;
					Set linpai = workerselector.selectedKeys();
					Iterator it = linpai.iterator();
					SelectionKey key = null;
					while (it.hasNext()) {
						try {
							key = (SelectionKey) it.next();
							it.remove();
							if (key.isAcceptable()) {
								SocketChannel clientchannel = neiserverSocketChannel
										.accept();

								clientchannel.configureBlocking(false);
								// 注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到selector上，之后这个连接的数据，就由这个SocketChannel处理
								clientchannel.register(workerselector,
										SelectionKey.OP_READ);
								// 将连接的客户端放入map中，以便返回数据时取出socketchannel
								// 测试方便使用”1“
								HttpServer.worker.put("1", clientchannel);
								// 将此对应的channel设置为准备接受其他客户端请求
								key.interestOps(SelectionKey.OP_ACCEPT);
								System.out.println("worker节点加入");
							}
						} catch (Exception e) {
							try {
								key.channel().close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							e.printStackTrace();
						}
						if (key.isReadable()) {// 如果这是可以读取数据
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							StringBuilder content = new StringBuilder();
							try {
								while (sc.read(buff) > 0) {// 大于0说明有的读，=0读完
															// <0错误
									buff.flip();
									content.append(Constent.charest
											.decode(buff));
								}
								String data = content.toString();
								System.out.println("work返回的数据" + data);
								SocketChannel cc=HttpServer.getClient("1");
								ByteBuffer dd=ByteBuffer.wrap(data.getBytes());
								cc.write(dd);
								key.interestOps(SelectionKey.OP_READ);
							} catch (IOException io) {
								key.cancel();
								if (key.channel() != null) {
									try {
										key.channel().close();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}

				}
			}
		});
		acceptThread.start();
	}

	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		try {
			server.init();
			server.startServer();
			server.startdispServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
