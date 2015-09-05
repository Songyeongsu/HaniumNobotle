package client;

public class NioClientMain
{

  public static void main(String[] args) {

    NioClient niclient = new NioClient();
    UI ui = new UI();
    niclient.startClient();
  }

}
