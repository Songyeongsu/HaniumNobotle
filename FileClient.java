import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileClient
{

  private static final String HOST = "localhost";
  private static final int PORT = 9999;

  private static FileHandler fileHandler;
  private static Logger logger = Logger.getLogger("net.daum.javacafe");

  private Selector selector = null;
  private SocketChannel sc = null;

  private Charset charset = null;
  private CharsetDecoder decoder = null;

  public FileClient() {
    charset = Charset.forName("EUC-KR");
    decoder = charset.newDecoder();
  }

  public void initServer() {
    try {
      // �����͸� ����.
      selector = Selector.open();

      // ����ä���� �����Ѵ�.
      sc = SocketChannel.open(new InetSocketAddress(HOST, PORT));

      // �� ���ŷ ���� �����Ѵ�.
      sc.configureBlocking(false);

      // ��������ä���� �����Ϳ� ����Ѵ�.
      sc.register(selector, SelectionKey.OP_READ);
    } catch (IOException e) {
      log(Level.WARNING, "Client.initServer()", e);
    }
  }

  public void startServer() {
    startWriter();
    // startReader();
  }

  private void startWriter() {
    info("Writer is started....");
    Thread t = new MyThread(sc);
    t.start();
  }

  private void startReader() {
    info("Reader is started..");
    try {
      while (true) {
        info("��û�� ��ٸ��� ��..");
        // �������� select() �޼ҵ�� �غ�� �̺�Ʈ�� �ִ��� Ȯ���Ѵ�.
        selector.select();

        // �������� SelectedSet �� ����� �غ��
        // �̺�Ʈ�� (SelectionKey��)�� �ϳ��� ó���Ѵ�.
        Iterator it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
          SelectionKey key = (SelectionKey) it.next();
          if (key.isReadable()) {
            // �̹� ����� Ŭ���̾�Ʈ�� �޼����� ���� ���
            read(key);
          }
          // �̹� ó���� �̺�Ʈ�̹Ƿ� �ݵ�� ����
          it.remove();
        }
      }
    } catch (Exception e) {
      log(Level.WARNING, "Client,startServer()", e);
    }
  }

  private void read(SelectionKey key) {
    // SelectionKey�κ��� ����ä���� ���´�.
    SocketChannel sc = (SocketChannel) key.channel();
    // ByteBuffer�� �����Ѵ�.
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    int read = 0;
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
    }

    buffer.flip();

    String data = "";
    try {
      data = decoder.decode(buffer).toString();
    } catch (CharacterCodingException e) {
      log(Level.WARNING, "Client.read()", e);
    }

    System.out.println("Message -" + data);

    // ���� �޸𸮸� ��ü�Ѵ�.
    clearBuffer(buffer);
  }

  private void clearBuffer(ByteBuffer buffer) {
    if (buffer != null) {
      buffer.clear();
      buffer = null;
    }
  }

  ///////////// Log part //////////////////

  public void initLog() {
    try {
      fileHandler = new FileHandler("Client.log");
    } catch (IOException e) {
    }

    logger.addHandler(fileHandler);
    logger.setLevel(Level.ALL);
  }

  public void log(Level level, String msg, Throwable error) {
    logger.log(level, msg, error);
  }

  public void info(String msg) {
    logger.info(msg);
  }

  class MyThread extends Thread
  {
    private SocketChannel sc = null;

    public MyThread(SocketChannel sc) {
      this.sc = sc;
    }

    public void run() {
      ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
      try {
        while (!Thread.currentThread().isInterrupted()) {
          buffer.clear();
          BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));
          String message = in.readLine();

          if (message.equals("quit") || message.equals("exit")) {
            System.exit(0);
          }

          else if (message.equals("send")) {
            sendFile(sc);
          }
        }
      } catch (Exception e) {
        log(Level.WARNING, "MyThread.run()", e);
      } finally {
        clearBuffer(buffer);
      }
    }

    public void sendFile(SocketChannel socketChannel) {
      RandomAccessFile aFile = null;
      try {
        File file = new File("test.avi");
        aFile = new RandomAccessFile(file, "r");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        int len;
        long curlen = 0, fullen = file.length();
        buffer.putLong(fullen);
        buffer.flip();
        socketChannel.write(buffer);
        buffer.compact();
        while ((len = inChannel.read(buffer)) != -1) {
          buffer.flip();
          socketChannel.write(buffer);
          curlen += len;
          buffer.compact();

          if (curlen == fullen) {
            break;
          }
        }
        System.out.println(fullen + "    " + curlen);
        System.out.println("End of file reached..");
        aFile.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  /////////////// Main ///////////////

  public static void main(String[] args) {
    FileClient client = new FileClient();
    client.initLog();
    client.initServer();
    client.startServer();
  }
}