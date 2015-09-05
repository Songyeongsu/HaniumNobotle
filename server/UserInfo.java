import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class UserInfo
{
  private int userNumber = 0;
  private String id;
  private String name;
  private String password;
  private String ip;
  private SocketChannel sc;

  public UserInfo(String id, String name, String password, String ip) {
    this.id = id;
    this.name = name;
    this.password = password;
    this.ip = ip;
    sc = null;
    userNumber++;
  }

  public void setSocketChanel(SocketChannel sc) {
    this.sc = sc;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getIp() {
    return ip;
  }

  public String getpassword() {
    return password;
  }

  public SocketChannel getSocketChanel() {
    return sc;
  }

}