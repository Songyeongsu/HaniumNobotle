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

public class FileServer
{
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
      // �����͸� ����
      selector = Selector.open();

      // ��������ä���� �����Ѵ�.
      serverSocketChannel = ServerSocketChannel.open();
      // ����ŷ ���� �����Ѵ�.
      serverSocketChannel.configureBlocking(false);
      // ��������ä�ΰ� ����� �������� �����´�.
      serverSocket = serverSocketChannel.socket();

      // �־��� �Ķ���Ϳ� �ش��ϴ� �ּҴ�. ��Ʈ�� ���������� ���ε��Ѵ�.
      InetSocketAddress isa = new InetSocketAddress(HOST, PORT);
      serverSocket.bind(isa);

      // ��������ä���� �����Ϳ� ����Ѵ�.
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    } catch (IOException e) {
      log(Level.WARNING, "Server.initServer()", e);
    }
  }

  public void startServer() {
    info("server is started...");
    try {
      while (true) {
        info("��û�� ��ٸ�����");
        ;
        // �������� select() �޼ҵ�� �غ�� �̺�Ʈ�� �ִ��� Ȯ���Ѵ�.
        selector.select();

        // �������� SelectedSet�� �����
        // �غ�� �̺�Ʈ�� (SelectionKey��)�� �ϳ��� ó���Ѵ�.
        Iterator it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
          SelectionKey key = (SelectionKey) it.next();

          if (key.isAcceptable()) {
            // ������Ĺä�ο� Ŭ���̾�Ʈ�� ������ �õ��� ���
            accept(key);
          } else if (key.isReadable()) {
            // �̹� ����� Ŭ���̾�Ʈ�� �޼����� �������
            readFileFromSocket((SocketChannel) key.channel());
          }
          // �̹� ó���� �̺�Ʈ �̹Ƿ� ����
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
      // ����Ĺä���� accept() �޼ҵ�� ���������� �����Ѵ�.
      sc = server.accept();
      // ������ ����ä���� ����ŷ�� �б� ���� �����Ϳ� ����Ѵ�.
      registerChannel(selector, sc, SelectionKey.OP_READ);
      info(sc.toString() + "Ŭ���̾�Ʈ�� ���� �߽��ϴ�.");
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
    // ä�ù濡 �߰��Ѵ�.
    addUser(sc);
  }

  private void read(SelectionKey key) {
    // SelectionKey�� ���� ����ä���� ���´�.
    SocketChannel sc = (SocketChannel) key.channel();
    // ByteBuffer �� �����Ѵ�.
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

      info(sc.toString() + "Ŭ���� ��Ʈ�� ������ �����߽��ϴ�.");
    }

    // try {
    // // Ŭ���̾�Ʈ�� ���� �޼����� ä�ù� �ȿ� ��� ����ڿ��� ��ε�ĳ��Ʈ ���ش�.
    // broadcast(buffer);
    // } catch (IOException e) {
    // log(Level.WARNING, "Server.broadcast()", e);
    // }

    // ���� �޸𸮸� �������ش�.
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
      int count = socketChannel.read(buffer);
      buffer.flip();
      long fullen = buffer.getLong();
      buffer.clear();

      System.out.println("SocketChannel read count " + count);

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
    FileServer server = new FileServer();
    server.initLog();
    server.initServer();
    server.startServer();
  }
}
