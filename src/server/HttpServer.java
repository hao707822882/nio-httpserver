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
		System.out.println("����������");

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
	 * WEB ���ܿͻ�������
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
								// ע��ѡ������������Ϊ��ȡģʽ���յ�һ����������Ȼ����һ��SocketChannel����ע�ᵽselector�ϣ�֮��������ӵ����ݣ��������SocketChannel����
								clientchannel.register(selector,
										SelectionKey.OP_READ);
								// �����ӵĿͻ��˷���map�У��Ա㷵������ʱȡ��socketchannel
								// Ϊ�˲��Լ򵥱��Ϊ��1��
								HttpServer.clientmap.put("1", clientchannel);
								// ���˶�Ӧ��channel����Ϊ׼�����������ͻ�������
								key.interestOps(SelectionKey.OP_ACCEPT);
								System.out.println("��������");
							}
						} catch (Exception e) {
							try {
								key.channel().close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							e.printStackTrace();
						}
						if (key.isReadable()) {// ������ǿ��Զ�ȡ����
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							StringBuilder content = new StringBuilder();
							try {
								while (sc.read(buff) > 0) {// ����0˵���еĶ���=0����
															// <0����
									buff.flip();
									content.append(Constent.charest
											.decode(buff));
								}
								String data = content.toString();
								System.out.println("���͵�����" + data);

								SocketChannel workchannel = HttpServer
										.getWorker("1");
								
								ByteBuffer bdata = ByteBuffer
										.wrap(("127.0.0.client" + data)
												.getBytes());
								workchannel.write(bdata);
								// ���˶�Ӧ��channel����Ϊ׼����һ�ν�������
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
	 * work�����
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
								// ע��ѡ������������Ϊ��ȡģʽ���յ�һ����������Ȼ����һ��SocketChannel����ע�ᵽselector�ϣ�֮��������ӵ����ݣ��������SocketChannel����
								clientchannel.register(workerselector,
										SelectionKey.OP_READ);
								// �����ӵĿͻ��˷���map�У��Ա㷵������ʱȡ��socketchannel
								// ���Է���ʹ�á�1��
								HttpServer.worker.put("1", clientchannel);
								// ���˶�Ӧ��channel����Ϊ׼�����������ͻ�������
								key.interestOps(SelectionKey.OP_ACCEPT);
								System.out.println("worker�ڵ����");
							}
						} catch (Exception e) {
							try {
								key.channel().close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							e.printStackTrace();
						}
						if (key.isReadable()) {// ������ǿ��Զ�ȡ����
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							StringBuilder content = new StringBuilder();
							try {
								while (sc.read(buff) > 0) {// ����0˵���еĶ���=0����
															// <0����
									buff.flip();
									content.append(Constent.charest
											.decode(buff));
								}
								String data = content.toString();
								System.out.println("work���ص�����" + data);
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
