package view;

import controller.LoginController;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    public LoginView(LoginController loginController, boolean isMember) {
        setTitle(isMember ? "회원" : "비회원");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout()); // 순서대로 가로 배치

        add(new JLabel("전화번호:"));
        JTextField phoneField = new JTextField(12);
        add(phoneField);

        JButton nextBtn = new JButton("로그인");
        JButton registerBtn = new JButton("신규등록");

        nextBtn.addActionListener(e -> {
            if (isMember) loginController.loginAsMember(phoneField.getText());
            else loginController.loginAsGuest(phoneField.getText());
        });

        registerBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "등록 완료! 비회원으로 진행합니다.");
            loginController.loginAsGuest(phoneField.getText());
        });

        add(nextBtn);
        if (isMember) add(registerBtn);
    }
}
