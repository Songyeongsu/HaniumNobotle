import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
// New text

public class Server {
	private static final String HOST = "localhost";
	private static final int PORT = 9999;

	private static FileHandler fileHandler;
	private static Logger logger = Logger.getLogger("net.daum.javacafe");

	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private ServerSocket serverSocket = null;

	private Vector room = new Vector();

	public void initServer() {
		try {
			// 셀렉터를 연다
			selector = Selector.open();

			// 서버소켓채널을 생성한다.
			serverSocketChannel = ServerSocketChannel.open();
			// 비블록킹 모드로 설정한다.
			serverSocketChannel.configureBlocking(false);
			// 서버소켓채널과 연결돤 서버소켓 가져온다.
			serverSocket = serverSocketChannel.socket();

			// 주어진 파라미터에 해당하는 주소다. 포트로 서버소켓을 바인드한다.
			InetSocketAddress isa = new InetSocketAddress(HOST, PORT);
			serverSocket.bind(isa);

			// 서버소켓채널을 셀렉터에 등록한다.
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			log(Level.WARNING, "Server.initServer()", e);
		}
	}

	public void startServer() {
		info("server is started...");
		try {
			while (true) {
				info("요청을 기다리는중");
				;
				// 셀렉터의 select() 메소드로 준비된 이벤트가 있는지 확인한다.
				selector.select();

				// 셀렉터의 SelectedSet에 저장된
				// 준비된 이벤트를 (SelectionKey들)을 하나씩 처리한다.
				Iterator it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = (SelectionKey) it.next();

					if (key.isAcceptable()) {
						// 서버소캣채널에 클라이언트가 접속을 시도한 경우
						accept(key);
					} else if (key.isReadable()) {
						// 이미 연결된 클라이언트가 메세지를 보낸경우
						readFileFromSocket((SocketChannel) key.channel());
					}
					// 이미 처리한 이벤트 이므로 삭제
					it.remove();
				}
			}
		} catch (Exception e) {
			log(Level.WARNING, "Server.startServer()", e);
		}
	}

	private void accept(SelectionKey key) {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel sc;

		try {
			// 서버캣채널의 accept() 메소드로 서버소켓을 생성한다.
			sc = server.accept();
			// 생성된 소켓채널을 비블록킹과 읽기 모드로 셀렉터에 등록한다.
			registerChannel(selector, sc, SelectionKey.OP_READ);
			info(sc.toString() + "클라이언트가 접속 했습니다.");
		} catch (ClosedChannelException e) {
			log(Level.WARNING, "Server.accept()", e);
		} catch (IOException e) {
			log(Level.WARNING, "Server.accept()", e);
		}
	}

	private void registerChannel(Selector selector, SocketChannel sc, int ops)
			throws ClosedChannelException, IOException {
		if (sc == null) {
			info("Invalid Connection");
			return;
		}
		sc.configureBlocking(false);
		sc.register(selector, ops);
		// 채팅방에 추가한다.
		addUser(sc);
	}

	private void read(SelectionKey key) {
		// SelectionKey로 부터 소켓채널을 얻어온다.
		SocketChannel sc = (SocketChannel) key.channel();
		// ByteBuffer 를 생성한다.
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile("Wildlife.wmv", "rw");
			FileChannel fileChannel = aFile.getChannel();

			int len;
			long curlen = 0;
			sc.read(buffer);
			buffer.flip();
			long fullen = buffer.getLong();
			buffer.clear();

			System.out.println(fullen);
			while ((len = sc.read(buffer)) != -1) {
				buffer.flip();
				fileChannel.write(buffer);
				curlen += len;
				buffer.clear();
				System.out.println(curlen + "    " + fullen);

				if (curlen == fullen)
					break;
			}
			System.out.println(curlen + "    " + fullen);
			Thread.sleep(1000);
			fileChannel.close();
			System.out.println("End of file reached..Closing channel");
			Thread.sleep(100000000);
		} catch (IOException | InterruptedException e) {
			try {
				sc.close();
			} catch (IOException e1) {
			}

			removeUser(sc);

			info(sc.toString() + "클라이 언트가 접속을 해제했습니다.");
		}

		// try {
		// // 클라이언트가 보낸 메세지를 채팅방 안에 모든 사용자에게 브로드캐스트 해준다.
		// broadcast(buffer);
		// } catch (IOException e) {
		// log(Level.WARNING, "Server.broadcast()", e);
		// }

		// 버퍼 메모리를 해제해준다.
		clearBuffer(buffer);
	}

	public void readFileFromSocket(SocketChannel socketChannel) {
		RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile("Wildlife.wmv", "rw");
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			FileChannel fileChannel = aFile.getChannel();

			int len;
			long curlen = 0;
			socketChannel.read(buffer);
			buffer.flip();
			long fullen = buffer.getLong();
			buffer.clear();

			System.out.println(fullen);
			while ((len = socketChannel.read(buffer)) != -1) {
				buffer.flip();
				fileChannel.write(buffer);
				curlen += len;
				buffer.clear();
				System.out.println(curlen + "    " + fullen);

				if (curlen == fullen)
					break;
			}
			System.out.println(curlen + "    " + fullen);
			Thread.sleep(1000);
			fileChannel.close();
			System.out.println("End of file reached..Closing channel");
			Thread.sleep(100000000);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void broadcast(ByteBuffer buffer) throws IOException {
		buffer.flip();

		Iterator iter = room.iterator();
		while (iter.hasNext()) {
			SocketChannel sc = (SocketChannel) iter.next();
			if (sc != null) {
				sc.write(buffer);
				buffer.rewind();
			}
		}
	}

	private void clearBuffer(ByteBuffer buffer) {
		if (buffer != null) {
			buffer.clear();
			buffer = null;
		}
	}

	private void addUser(SocketChannel sc) {
		room.add(sc);
	}

	private void removeUser(SocketChannel sc) {
		room.remove(sc);
	}

	/////////////////// Log part ////////////

	public void initLog() {
		try {
			fileHandler = new FileHandler("Server.log");
		} catch (IOException e) {
		}

		logger.addHandler(fileHandler);
		logger.setLevel(Level.ALL);
	}

	public void log(Level lever, String msg, Throwable error) {
		logger.log(lever, msg, error);
	}

	public void info(String msg) {
		logger.info(msg);
	}

	//////////////// Main //////////////

	public static void main(String[] args) {
		Server server = new Server();
		server.initLog();
		server.initServer();
		server.startServer();
	}
}
