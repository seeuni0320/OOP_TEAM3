package view;

import controller.LoginController;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    // 💡 생성자 매개변수 맨 끝에 SwingNavigator navigator를 추가로 주입받습니다!
    public LoginView(LoginController loginController, boolean isMember, boolean isExitMode, SwingNavigator navigator) {
        
        // 1. 퇴실 모드 분기 처리
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
            if (isExitMode) {
                loginController.loginAsMember(phoneField.getText());
            } else {
                if (isMember) loginController.loginAsMember(phoneField.getText());
                else loginController.loginAsGuest(phoneField.getText());
            }
        });

        registerBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "등록 완료! 비회원으로 진행합니다.");
            loginController.loginAsGuest(phoneField.getText());
        });

        add(nextBtn);
        
        if (isMember && !isExitMode) {
            add(registerBtn);
        }

        // ================================================================
        // 🏠 [새로 추가된 메인 메뉴로 가기 버튼 구역]
        // ================================================================
        add(Box.createHorizontalStrut(250)); 

        JButton mainBtn = new JButton("🏠 처음으로 (메인화면)");
        mainBtn.setBackground(new Color(230, 242, 255)); 
        mainBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        
        mainBtn.addActionListener(e -> {
            // 1. 현재 열려있는 로그인 창을 메모리에서 깔끔하게 해제
            this.setVisible(false);
            this.dispose();
            
            // 2. ⚡ [먹통 방지 & 꺼짐 방지 완벽 해결]
            // 복제본을 새로 만드는 게 아니라, 넘겨받은 원본 총사령관 리모컨을 그대로 사용합니다!
            if (navigator != null) {
                navigator.showMainMenu(); // 👈 총사령관에게 메인 메뉴를 다시 켜라고 명령!
            } else {
                // 혹시 모를 단독 테스트용 방어 코드 (navigator가 null로 들어왔을 때만 실행됨)
                try {
                    MainMenuView mainFrame = new MainMenuView(null);
                    mainFrame.setVisible(true);
                } catch (Exception ex) {}
            }
        });
        
        add(mainBtn);
        // ================================================================
    }
}