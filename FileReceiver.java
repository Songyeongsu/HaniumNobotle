import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class FileReceiver {

   public static void main(String[] args) {
      FileReceiver nioServer = new FileReceiver();
      SocketChannel socketChannel = nioServer.createServerSocketChannel();
      nioServer.readFileFromSocket(socketChannel);
   }

   public SocketChannel createServerSocketChannel() {

      ServerSocketChannel serverSocketChannel = null;
      SocketChannel socketChannel = null;
      try {
         serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.socket().bind(new InetSocketAddress(9999));
         socketChannel = serverSocketChannel.accept();
         System.out.println("Connection established...." + socketChannel.getRemoteAddress());

      } catch (IOException e) {
         e.printStackTrace();
      }

      return socketChannel;
   }

   /**
    * Reads the bytes from socket and writes to file
    *
    * @param socketChannel
    */
   public void readFileFromSocket(SocketChannel socketChannel) {
      try {
    	  RandomAccessFile aFile = null;
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
         Thread.sleep(10000);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      try {
    	  
    	  System.out.println("re go");
          RandomAccessFile aFile = null;

         File file = new File("Wildlife.wmv");
         aFile = new RandomAccessFile(file, "r");
         FileChannel inChannel = aFile.getChannel();
         ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
         int len;
         long curlen = 0, fullen = file.length();
         buffer.putLong(fullen);
         buffer.flip();
         socketChannel.write(buffer);
         buffer.clear();
         while ((len = inChannel.read(buffer)) != -1) {
            buffer.flip();
            socketChannel.write(buffer);
            curlen += len;
            buffer.clear();
            
            if (curlen == fullen)
            	break;
         }
         System.out.println(fullen + "    " + curlen);
         Thread.sleep(1000);
         System.out.println("End of file reached..");
         aFile.close();
         Thread.sleep(10000);

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

   }
}
