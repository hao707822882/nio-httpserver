package worker;

import intefaceImp.HttpDecoder;
import intefaceImp.NonPermission;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import bean.Dispatcher;
import bean.HttpBean;

public class WorkerNode {

	private Selector selector = null;
	static final int port = 9999;
	private Charset charset = Charset.forName("UTF-8");
	private SocketChannel sc = null;

	public void init() throws IOException {
		selector = Selector.open();
		// 连接远程主机的IP和端口
		sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);
		// 开辟一个新线程来读取从服务器端的数据
		new Thread(new ClientThread()).start();
	}

	private class ClientThread implements Runnable {
		public void run() {
			try {
				while (true) {
					int readyChannels = selector.select();
					if (readyChannels == 0)
						continue;
					Set selectedKeys = selector.selectedKeys(); // 可以通过这个方法，知道可用通道的集合
					Iterator keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						SelectionKey sk = (SelectionKey) keyIterator.next();
						keyIterator.remove();
						dealWithSelectionKey(sk);
					}
				}
			} catch (IOException io) {
				io.printStackTrace();
			}
		}

		private void dealWithSelectionKey(SelectionKey sk) throws IOException {
			if (sk.isReadable()) {
				// 使用 NIO 读取 Channel中的数据，这个和全局变量sc是一样的，因为只注册了一个SocketChannel
				// sc既能写也能读，这边是读
				SocketChannel sc = (SocketChannel) sk.channel();
				ByteBuffer buff = ByteBuffer.allocate(1024);
				String content = "";
				while (sc.read(buff) > 0) {
					buff.flip();
					content += charset.decode(buff);
				}
				// content为http服务器转发过来的消息，需要进行处理并回写给客户端
				if (content != "" & content != null) {
					HttpDecoder HttpDecoder = new HttpDecoder();
					HttpBean hb = (HttpBean) HttpDecoder.decode(content);
					System.out.println("url是******" + hb.getUrl());
					Dispatcher dis = new Dispatcher(hb, new NonPermission());
					String response = dis.dispatcher();
					System.out.println("response是"+response);
					sc.write(ByteBuffer.wrap("HTTP/1.1 200 OK".getBytes()));
				}
				sk.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new WorkerNode().init();
	}
}