import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

public class NioClient
{

  static Selector selector = null;
  private SocketChannel sc = null;
  static String Info = "no";
  static boolean CheckInfo = false;

  public void initServer() {
    try {
      selector = Selector.open();
      sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));
      sc.configureBlocking(false);
      sc.register(selector, SelectionKey.OP_READ);
    } catch (IOException ex) {

    }
  }

  public void reciveInfo(String id, String password) {
    Info = id + "-" + password;
    System.out.println(Info);
  }

  public void startClient() {
    initServer();
    Receive rt = new Receive();
    new Thread(rt).start();
    startWriter();
  }

  public void startWriter() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    try {

      while (true) {

        if (!Info.equals("no")) {

          buffer.clear();
          buffer.put(Info.getBytes());
          buffer.flip();
          sc.write(buffer);

          Info = "no";
        }

      }
    } catch (Exception ex) {

    } finally {
      clearBuffer(buffer);
    }
  }

  static void clearBuffer(ByteBuffer buffer) {
    if (buffer != null) {
      buffer.clear();
      buffer = null;
    }
  }

}

class Receive implements Runnable
{
  private Charset charset = null;
  private CharsetDecoder decoder = null;

  @Override
  public void run() {

    charset = Charset.forName("EUC-KR");
    decoder = charset.newDecoder();

    try {
      while (true) {
        NioClient.selector.select();
        Iterator it = NioClient.selector.selectedKeys().iterator();
        while (it.hasNext()) {
          SelectionKey key = (SelectionKey) it.next();
          if (key.isReadable()) {
            read(key);
          } else {
          }
          it.remove();
        }
      }
    } catch (Exception ex) {
    }

  }

  public void read(SelectionKey key) {
    SocketChannel sc = (SocketChannel) key.channel();
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    int nbyte = 0;

    try {
      nbyte = sc.read(buffer);
      buffer.flip();
      String data = decoder.decode(buffer).toString();

      if (data.equals("yes")) {
        UI.closeui();
        UI.clientUI();
      }

      System.out.println("Receive Messge -" + data);
      NioClient.clearBuffer(buffer);

    } catch (Exception ex) {
      try {
        sc.close();
      } catch (IOException ex1) {

      }
    }

  }

}
