package view;

import controller.LoginController;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    public LoginView(LoginController loginController, boolean isMember, boolean isExitMode, SwingNavigator navigator) {
        
        if (isExitMode) {
            setTitle("퇴실 본인 인증");
        } else {
            setTitle(isMember ? "회원 로그인" : "비회원 로그인");
        }
        
        setSize(300, 180);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); 

        add(new JLabel("전화번호:"));
        JTextField phoneField = new JTextField(12);
        add(phoneField);

        String btnText = isExitMode ? "퇴실하기" : "로그인";
        JButton nextBtn = new JButton(btnText);
        JButton registerBtn = new JButton("신규등록");

        nextBtn.addActionListener(e -> {
            String phoneNumber = phoneField.getText();
            
            if (isExitMode) {
                loginController.exitAsMember(phoneNumber); 
            } else {
                if (isMember) {
                    loginController.loginAsMember(phoneNumber);
                } else {
                    loginController.loginAsGuest(phoneNumber);
                }
            }
            this.setVisible(false);
            this.dispose();
        });

        registerBtn.addActionListener(e -> {
            String phoneNumber = phoneField.getText().trim();
            if (phoneNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this, "등록할 전화번호를 입력해주세요.");
                return;
            }
            
            this.setVisible(false);
            this.dispose();
            
            loginController.registerMember(phoneNumber);
            
            // 비회원으로 튕기지 않고 무조건 정기권 구매 창으로 넘어감
            loginController.loginAsMember(phoneNumber);
        });

        add(nextBtn);
        
        if (isMember && !isExitMode) {
            add(registerBtn);
        }

        add(Box.createHorizontalStrut(250)); 

        JButton mainBtn = new JButton("처음으로 (메인화면)");
        mainBtn.setBackground(new Color(230, 242, 255)); 
        mainBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        
        mainBtn.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (navigator != null) {
                navigator.showMainMenu(); 
            }
        });
        
        add(mainBtn);
    }
}