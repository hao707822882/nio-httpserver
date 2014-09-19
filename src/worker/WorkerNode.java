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
		// ����Զ��������IP�Ͷ˿�
		sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);
		// ����һ�����߳�����ȡ�ӷ������˵�����
		new Thread(new ClientThread()).start();
	}

	private class ClientThread implements Runnable {
		public void run() {
			try {
				while (true) {
					int readyChannels = selector.select();
					if (readyChannels == 0)
						continue;
					Set selectedKeys = selector.selectedKeys(); // ����ͨ�����������֪������ͨ���ļ���
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
				// ʹ�� NIO ��ȡ Channel�е����ݣ������ȫ�ֱ���sc��һ���ģ���Ϊֻע����һ��SocketChannel
				// sc����дҲ�ܶ�������Ƕ�
				SocketChannel sc = (SocketChannel) sk.channel();
				ByteBuffer buff = ByteBuffer.allocate(1024);
				String content = "";
				while (sc.read(buff) > 0) {
					buff.flip();
					content += charset.decode(buff);
				}
				// contentΪhttp������ת����������Ϣ����Ҫ���д�����д���ͻ���
				if (content != "" & content != null) {
					HttpDecoder HttpDecoder = new HttpDecoder();
					HttpBean hb = (HttpBean) HttpDecoder.decode(content);
					System.out.println("url��******" + hb.getUrl());
					Dispatcher dis = new Dispatcher(hb, new NonPermission());
					String response = dis.dispatcher();
					System.out.println("response��"+response);
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