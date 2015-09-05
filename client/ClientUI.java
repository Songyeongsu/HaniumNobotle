import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UI extends Frame
{
  Label lid, lpwd;
  TextField tfId, tfPwd;
  Button ok;
  NioClient ni = new NioClient();

  public static Frame f = new Frame("Login");

  UI() {

    lid = new Label("ID : ", Label.RIGHT);
    lpwd = new Label("Password :", Label.RIGHT);
    tfId = new TextField(10);
    tfPwd = new TextField(10);
    tfPwd.setEchoChar('*');
    ok = new Button("JOIN");
    tfId.addActionListener(new Login());
    tfPwd.addActionListener(new Login());
    ok.addActionListener(new Login());
    Label Smalllabel = new Label("��  Program login ID is 'test' and Pwd is '1234' ");
    Label Biglabel = new Label("             Hanium Login Service             ");
    Biglabel.setFont(new Font("Helvetica", Font.BOLD, 30));
    Biglabel.setBackground(Color.white);
    Smalllabel.setFont(new Font("Helvetica", Font.BOLD, 10));
    Smalllabel.setBackground(Color.white);
    f.setLayout(new FlowLayout());
    f.add(Biglabel);
    f.add(lid);
    f.add(tfId);
    f.add(lpwd);
    f.add(tfPwd);
    f.add(ok);
    f.add(Smalllabel);
    f.setSize(450, 150);
    Toolkit tk = Toolkit.getDefaultToolkit(); // Frame�� ���ȭ�� ��ġ ����
    Dimension screenSize = tk.getScreenSize(); // Frame�� ���ȭ�� ��ġ ����
    f.setLocation(screenSize.width / 2 - 200, screenSize.height / 2 - 100); // Frame��
    // ���ȭ��
    f.setVisible(true);
    f.addWindowListener(new exit());
  }

  public static void clientUI() {
    JFrame f = new JFrame("Client");
    JButton setupButton = new JButton("Setup");
    JButton playButton = new JButton("Play");
    JButton pauseButton = new JButton("Pause");
    JButton tearButton = new JButton("Exit");
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel iconLabel = new JLabel();
    f.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    // Buttons

    buttonPanel.setLayout(new GridLayout(1, 0));
    buttonPanel.add(setupButton);
    buttonPanel.add(playButton);
    buttonPanel.add(pauseButton);
    buttonPanel.add(tearButton);
    // setupButton.addActionListener(new setupButtonListener());

    // playButton.addActionListener(new playButtonListener());

    // pauseButton.addActionListener(new pauseButtonListener());

    // tearButton.addActionListener(new tearButtonListener());

    // Image display label
    iconLabel.setIcon(null);

    // frame layout
    mainPanel.setLayout(null);
    mainPanel.add(iconLabel);
    mainPanel.add(buttonPanel);
    iconLabel.setBounds(0, 0, 380, 280);
    buttonPanel.setBounds(0, 280, 380, 50);
    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
    f.setSize(new Dimension(390, 370));
    f.setVisible(true);

  }

  public static void closeui() {

    f.setVisible(false);

  }

  class Login implements ActionListener
  {

    public void actionPerformed(ActionEvent e) {
      String id = tfId.getText();
      String password = tfPwd.getText();

      String info = id + "-" + password;
      ni.reciveInfo(id, password);
      // ni.userinfo(info);

    }
  }

  public class exit implements WindowListener
  {
    public void windowClosing(WindowEvent e) {
      e.getWindow().setVisible(false);

    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
  }

  public class newimformation
  {

  }

}
