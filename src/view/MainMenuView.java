package view;

//여기는 model,controller랑 상호작용 하는 부분은 없어용 네비게이터가 넘기게 했습니당
import controller.LoginController; // 혹시 나중에 필요할지 모르니 임포트
import javax.swing.*;
import java.awt.*;

public class MainMenuView extends JFrame {
    // 조원들 설계에 맞춰 LoginController 대신 ViewNavigator(리모컨)를 직접 받습니다!
    public MainMenuView(ViewNavigator navigator) { 
        setTitle("메인 메뉴");
        setSize(300, 250); // 버튼이 3개로 늘어났으니 세로 길이를 200 -> 250으로 살짝 키웠어용!
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1, 10, 10)); // 버튼이 3개라 3단 배열로 변경! 간격도 살짝 추가 ㅎㅎ

        JButton memberBtn = new JButton("회원 로그인 (이용권 구매/입실)");
        JButton guestBtn = new JButton("비회원 이용 (이용권 구매)");
        JButton exitBtn = new JButton("🚪 퇴실 하기"); // 새로 추가한 퇴실 버튼!

        // 내비게이터의 showLogin()을 바로 호출합니다!
        // 꿀팁: 퇴실할 때도 본인 확인(전화번호 입력)이 필요하니까 로그인 화면으로 보냅니다.
        // (조원들이 나중에 LoginView나 Controller에서 퇴실 로직으로 분기해주기 편하게 true/false를 활용하거나, 
        // 일단 회원 로그인 창을 공유해서 쓰도록 설계 완료!)
        memberBtn.addActionListener(e -> navigator.showLogin(true));
        guestBtn.addActionListener(e -> navigator.showLogin(false));
        
        // 퇴실 버튼을 누르면 마찬가지로 본인 인증을 위해 로그인 창으로 이동!
        exitBtn.addActionListener(e -> navigator.showLoginForExit()); 

        // 화면에 버튼 3개 순서대로 이쁘게 부착!
        add(memberBtn); 
        add(guestBtn);
        add(exitBtn);
    }
}