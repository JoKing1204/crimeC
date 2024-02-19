import java.awt.*;
import java.util.HashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class LoginPage implements ActionListener{

    JFrame frame = new JFrame();
    JButton loginBut = new JButton("Login");
    JButton registerBut = new JButton("Sign Up");
    JTextField userIDField = new JTextField();
    JPasswordField userPasswordField = new JPasswordField();
    JLabel userIdLabel = new JLabel("userID:");
    JLabel userPasswordLabel = new JLabel("Password:");
    JLabel messageLabel = new JLabel("THIS IS A TEST");
    HashMap<String,String> loginfo = new HashMap<String,String>();
    LoginPage(HashMap<String,String> logInfoOriginal){
        loginfo = logInfoOriginal;

        userIdLabel.setBounds(50,100,75,25);
        userPasswordLabel.setBounds(50,150,75,25);

        messageLabel.setBounds(125,250,250,35);
        messageLabel.setFont(new Font(null,Font.ITALIC,35));

        userIDField.setBounds(125,100,200,25);
        userPasswordField.setBounds(125,150,200,30);

        loginBut.setBounds(125,200,125,50);
        loginBut.addActionListener(this);
        loginBut.setFocusable(false);

        registerBut.setBounds(225,200,125,50);
        registerBut.setFocusable(false);
        registerBut.addActionListener(this);

        frame.add(userIdLabel);
        frame.add(userPasswordLabel);
        frame.add(userIDField);
        frame.add(userPasswordField);
        frame.add(loginBut);
        frame.add(registerBut);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e){

    }
}