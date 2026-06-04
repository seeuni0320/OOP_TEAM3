package view;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class MainMenuView extends JFrame {
    // 💡 실시간으로 불빛을 바꿔치기할 미니 좌석 버튼 16개 주머니!
    private final JButton[] miniSeatButtons = new JButton[16];

    public MainMenuView(ViewNavigator navigator) { 
        setTitle("메인 메뉴");
        // 💡 좌석 배치판과 버튼이 좌우로 나란히 들어가므로 가로 크기를 700으로 시원하게 확장!
        setSize(700, 420); 
        setLocationRelativeTo(null);
        
        // 전체 레이아웃을 좌우로 넓게 배치하기 위해 1행 2열 GridLayout으로 설정합니다.
        setLayout(new GridLayout(1, 2, 20, 0)); 

        // ================================================================
        // 🟩 [왼쪽 구역: 미니 좌석 이용 현황판 (4x4)]
        // ================================================================
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 10));

        // 상단 타이틀
        JLabel titleLabel = new JLabel("📊 실시간 좌석 이용 현황", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        // 4x4 바둑판 주머니 생성
        JPanel miniSeatGrid = new JPanel(new GridLayout(4, 4, 6, 6));
        for (int i = 0; i < 16; i++) {
            miniSeatButtons[i] = new JButton();
            miniSeatButtons[i].setFont(new Font("맑은 고딕", Font.BOLD, 11));
            miniSeatButtons[i].setEnabled(false); // 💡 메인화면 현황판은 클릭용이 아니라 '뷰어'니까 비활성화!
            miniSeatGrid.add(miniSeatButtons[i]);
        }
        leftPanel.add(miniSeatGrid, BorderLayout.CENTER);
        
        add(leftPanel); // 메인 창 왼쪽에 착 장착!

        // ================================================================
        // 🎛️ [오른쪽 구역: 키오스크 오리지널 메뉴 버튼 3개]
        // ================================================================
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 20));

        JLabel menuLabel = new JLabel("원하시는 서비스를 선택하세요", SwingConstants.CENTER);
        menuLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        rightPanel.add(menuLabel, BorderLayout.NORTH);

        // 버튼 3단 쌓기
        JPanel buttonGrid = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton memberBtn = new JButton("회원 로그인 (이용권 구매/입실)");
        JButton guestBtn = new JButton("비회원 이용 (이용권 구매)");
        JButton exitBtn = new JButton("🚪 퇴실 하기"); 

        memberBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        guestBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        exitBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        exitBtn.setBackground(new Color(245, 245, 245));

        memberBtn.addActionListener(e -> navigator.showLogin(true));
        guestBtn.addActionListener(e -> navigator.showLogin(false));
        exitBtn.addActionListener(e -> navigator.showLoginForExit()); 

        buttonGrid.add(memberBtn); 
        buttonGrid.add(guestBtn);
        buttonGrid.add(exitBtn);

        rightPanel.add(buttonGrid, BorderLayout.CENTER);
        
        add(rightPanel); // 메인 창 오른쪽에 착 장착!
    }

    /**
     * ⚡ [핵심 연동 장치] SwingNavigator가 메인 화면을 호출할 때 데이터를 던져주면,
     * 올려준 스크린샷과 100% 똑같은 싱크로율로 미니 바둑판에 색상과 텍스트를 입힙니다!
     */
    public void updateUsageStatus(Collection<model.Seat> seats) {
        if (seats == null || seats.isEmpty()) return;

        for (model.Seat seat : seats) {
            int idx = seat.getSeatNumber() - 1;
            if (idx < 0 || idx >= 16) continue;

            if (seat.isOccupied()) { 
                miniSeatButtons[idx].setBackground(new Color(255, 102, 102)); 
                miniSeatButtons[idx].setText("<html><center><font color='white'><b>" + seat.getSeatNumber() + "번</b><br>[이용 중]</font></center></html>");
            } else {
                miniSeatButtons[idx].setBackground(new Color(102, 255, 102)); 
                miniSeatButtons[idx].setText("<html><center><b>" + seat.getSeatNumber() + "번</b><br><font color='#555555' size='2'>(빈자리)</font></center></html>");
            }
        }
    }
}
