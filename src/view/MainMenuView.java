package view;
//여기는 model,controller랑 상호작용 하는 부분은 없어용 네비게이터가 넘기게 했습니당
import controller.LoginController; // 혹시 나중에 필요할지 모르니 임포트
import javax.swing.*;
import java.awt.*;

public class MainMenuView extends JFrame {
    // 조원들 설계에 맞춰 LoginController 대신 ViewNavigator(리모컨)를 직접 받습니다!
    public MainMenuView(ViewNavigator navigator) { 
        setTitle("메인 메뉴");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 1)); // 단순 2단 배열

        JButton memberBtn = new JButton("회원 로그인");
        JButton guestBtn = new JButton("비회원 이용");

        // 내비게이터의 showLogin()을 바로 호출합니다!
        memberBtn.addActionListener(e -> navigator.showLogin(true));
        guestBtn.addActionListener(e -> navigator.showLogin(false));

        add(memberBtn); 
        add(guestBtn);
    }
}
