import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Iterator;
import java.util.Vector;

public class FileReceive {
   private Selector selector = null;
   private ServerSocketChannel serverSocketChannel = null;
   private ServerSocket serverSocket = null;

   private Vector room = new Vector();

   public void initServer() {
      try {
         selector = Selector.open();
         // 서버소켓 채널 생성
         serverSocketChannel = ServerSocketChannel.open();
         // 비 블록킹 모드 설정
         serverSocketChannel.configureBlocking(false);
         // 서버소켓 채널과 연결된 서버소켓 가져온다
         serverSocket = serverSocketChannel.socket();
         // 포트로 서버소켓 바인드
         InetSocketAddress isa = new InetSocketAddress(9999);
         serverSocket.bind(isa);
         // 서버소켓 채널을 셀렉터에 등록
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public void startServer() {
      System.out.println("Server is Started...");
      try {
         while (selector.select() > 0) {

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

         room.add(sc);
         System.out.println(sc.toString() + "클라이언트가 접속했습니다.");

      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void read(SelectionKey key) {
      SocketChannel sc = (SocketChannel) key.channel();
      try {
         System.out.println("read");
         File file = new File("Wildlife.wmv");
         FileOutputStream fos = new FileOutputStream(file);
         ByteBuffer buffer = ByteBuffer.allocate(4096);
         FileChannel fileChannel = fos.getChannel();
         while (sc.read(buffer) != -1) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
         }
         Thread.sleep(1000);
         fileChannel.close();
         sc.close();
         System.out.println("End of file reached..Closing channel");

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }


   }

   // try {
   // int read = sc.read(buffer);
   //
   // } catch (IOException ex) {
   // try {
   // sc.close();
   // } catch (IOException e) {
   // }
   // room.remove(sc);
   // ex.printStackTrace();
   // }

   // readFileFromSocket(sc);

   // try {
   // broadcast(buffer);
   //
   // } catch (
   //
   // IOException ex)
   //
   // {
   // ex.printStackTrace();
   // }
   //
   // if (buffer != null)
   //
   // {
   // buffer.clear();
   // buffer = null;
   // }
   // }

   // private void broadcast(ByteBuffer buffer) throws IOException {
   // buffer.flip();
   // Iterator iter = room.iterator();
   //
   // while (iter.hasNext()) {
   // SocketChannel sc = (SocketChannel) iter.next();
   //
   // if (sc != null) {
   // sc.write(buffer);
   // buffer.rewind();
   // }
   // }
   // }

   public void readFileFromSocket(SocketChannel socketChannel) {
      RandomAccessFile aFile = null;
      try {
         aFile = new RandomAccessFile("Wildlife.txt", "rw");
         ByteBuffer buffer = ByteBuffer.allocate(1024);
         FileChannel fileChannel = aFile.getChannel();
         while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
         }
         Thread.sleep(1000);
         fileChannel.close();
         System.out.println("End of file reached..Closing channel");
         socketChannel.close();

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

   }

   public static void main(String[] args) {
      FileReceive server = new FileReceive();
      server.initServer();
      server.startServer();
   }
}