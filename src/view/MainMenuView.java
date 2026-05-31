// 디자인 코드는 위쪽 생성자에, 이벤트 처리 코드는 아래쪽에 몰아넣기
//MainmenuView : 하단에 [회원 로그인], [비회원 이용] 버튼 배치
//LoginView : 회원 아이디 비번 입력화면
//SeatSelectionView : 전체 좌석 배치도 화면, 좌석 버튼들로 구성되어 실시간 이용 상태를 표시
//TicketSelectionView : 이용권 선택 
//PatmentView : 결제창

import javax.swing.*; 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuView extends JFrame {
    private JButton memberButton;
    private JButton nonMemberButton;

    public MainMenuView() {
        setTitle("스터디카페 키오스크 - 메인");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 화면 정중앙에 창 띄우기
        setLayout(new BorderLayout());

        // 상단 현황판
        JLabel statusLabel = new JLabel("현재 이용 가능한 좌석: 12개 / 16개", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        add(statusLabel, BorderLayout.CENTER);

        // 하단 버튼들
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        memberButton = new JButton("회원 로그인");
        nonMemberButton = new JButton("비회원 이용");

        // 회원 로그인 버튼 클릭 시
        memberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false); // 메인 숨기기
                new LoginView(MainMenuView.this, true).setVisible(true); // 로그인창 열기
            }
        });

        // 비회원 이용 버튼 클릭 시
        nonMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false); // 메인 숨기기
                new LoginView(MainMenuView.this, false).setVisible(true); // 로그인창 열기
            }
        });

        buttonPanel.add(memberButton);
        buttonPanel.add(nonMemberButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // 프로그램을 실행하는 main 메서드
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenuView().setVisible(true);
        });
    }
}