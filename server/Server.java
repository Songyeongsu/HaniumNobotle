import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Server
{

  ArrayList<UserInfo> userInfo = new ArrayList<UserInfo>();
  Selector selector = null;
  ServerSocketChannel serverSocketChannel = null;
  ServerSocket serverSocket = null;
  serverLog log = new serverLog();
  LogRecord record = null;
  WindowHandler handler = WindowHandler.getInstance();

  public void readuser() {
    String filename = "userFile.txt";
    try {
      Scanner in = new Scanner(new File(filename));
      while (in.hasNextLine()) {
        String line = in.nextLine();
        String[] tokens = line.trim().split("-");
        UserInfo user = new UserInfo(tokens[0], tokens[1], tokens[2], tokens[3]);
        userInfo.add(user);
      }
    } catch (FileNotFoundException ex) {
      record = new LogRecord(Level.INFO, "Not Found file");
      handler.publish(record);
    }
    record = new LogRecord(Level.INFO, "UserInfo loading complete");
    handler.publish(record);
  }

  public void initServer() {
    try {
      selector = Selector.open(); // �������� ä�� ����
      serverSocketChannel = ServerSocketChannel.open(); // �� ���ŷ ��� ����
      serverSocketChannel.configureBlocking(false); // �������� ä�ΰ� �����
                                                    // ��������
      // �����´�
      serverSocket = serverSocketChannel.socket(); // ��Ʈ�� �������� ���ε�
      InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 9999);
      serverSocket.bind(isa); // �������� ä���� �����Ϳ� ���
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    record = new LogRecord(Level.INFO, "Server setting is completed");
    handler.publish(record);
  }

  public void startServer() {
    record = new LogRecord(Level.INFO, "Server starts");
    handler.publish(record);
    try {
      while (true) {
        selector.select();
        Iterator it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
          SelectionKey key = (SelectionKey) it.next();
          if (key.isAcceptable()) {
            accept(key);
          } else if (key.isReadable()) {
            read(key);
          }
          it.remove();
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void accept(SelectionKey key) {
    ServerSocketChannel server = (ServerSocketChannel) key.channel();
    try {
      SocketChannel sc = server.accept();
      if (sc == null)
        return;
      sc.configureBlocking(false);
      sc.register(selector, SelectionKey.OP_READ);
      record = new LogRecord(Level.INFO, "Connect to client " + sc.toString());
      handler.publish(record);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void read(SelectionKey key) throws IOException {
    Charset charset = null;
    CharsetDecoder decoder = null;
    SocketChannel sc = (SocketChannel) key.channel();
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    record = new LogRecord(Level.INFO, "loging User " + sc.getLocalAddress().toString());
    handler.publish(record);
    charset = Charset.forName("EUC-KR");
    decoder = charset.newDecoder();
    try {
      int read = sc.read(buffer);
      buffer.flip();
      String str = decoder.decode(buffer).toString();
      String[] tk = str.split("-");
      String id = tk[0];
      String pwd = tk[1];
      for (int i = 0; i < userInfo.size(); i++) {
        System.out.println(userInfo.get(i).getName());
        if (id.equalsIgnoreCase(userInfo.get(i).getId())) {
          if (pwd.equalsIgnoreCase(userInfo.get(i).getpassword())) {
            userInfo.get(i).setSocketChanel(sc);
            returnOk(sc, "yes");
            break;
          }
        }
      }
    } catch (IOException ex) {
      try {
        sc.close();
      } catch (IOException e) {
      }
      ex.printStackTrace();
    }
  }

  private void returnOk(SocketChannel sc, String string) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    try {
      buffer.clear();
      buffer.put(string.getBytes());
      buffer.flip();
      sc.write(buffer);
    } catch (Exception e) {

    }
  }

  class serverLog
  {
    private WindowHandler handler = null;
    private Logger logger = null;

    public serverLog() {
      handler = WindowHandler.getInstance();
      logger = Logger.getLogger("sam.logging.handler");
      logger.addHandler(handler);
    }

    public void logMessage(String st) {
      logger.info(st);
    }
  }
}