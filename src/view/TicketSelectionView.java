package view;

import controller.TicketController;
import model.Ticket;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TicketSelectionView extends JFrame {
    // 💡 생성자 매개변수 맨 끝에 SwingNavigator navigator를 추가로 주입받습니다!
    public TicketSelectionView(TicketController ticketController, List<Ticket> tickets, boolean isOwnedMode, SwingNavigator navigator) {
        setTitle(isOwnedMode ? "보유 이용권 조회" : "신규 이용권 구매");
        setSize(300, 400); // 하단 버튼을 위해 세로 크기를 넉넉하게 확장!
        setLocationRelativeTo(null);
        
        // 메인 화면 버튼이 맨 밑에 무조건 1칸 추가되므로, 
        // 기존 줄 수에다가 무조건 + 1을 해줘서 바둑판 배열이 깨지는 걸 완벽히 방어합니다!
        int rowCount = tickets.size() + (isOwnedMode ? 1 : 0) + 1; 
        setLayout(new GridLayout(rowCount, 1, 8, 8)); // 여백도 8로 늘려서 보기 편하게!

        // 넘겨받은 티켓 목록을 돌면서 버튼을 자동으로 생성합니다.
        for (Ticket ticket : tickets) {
            JButton btn = new JButton(ticket.getTicketName() + " (" + ticket.getPrice() + "원)");
            
            btn.addActionListener(e -> {
                if (isOwnedMode) {
                    // 이미 가진 티켓을 선택함 -> 좌석 선택하러 이동하라고 컨트롤러에 전달!
                    ticketController.selectOwnedTicket(ticket);
                } else {
                    // 새로 살 티켓을 선택함 -> 결제창(PaymentView) 띄우라고 컨트롤러에 전달!
                    ticketController.selectTicketToPurchase(ticket);
                }
            });
            add(btn);
        }

        // 보유 이용권 조회 모드일 때만 맨 밑에 '추가 구매'
        if (isOwnedMode) {
            JButton moreBtn = new JButton("이용권 추가 구매");
            moreBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            moreBtn.addActionListener(e -> { 
                setVisible(false); 
                ticketController.showPurchase(); // 구매 메뉴판 켜달라고 컨트롤러에 요청
            });
            add(moreBtn);
        }

        // ================================================================
        // 🏠 [새로 추가된 메인 화면으로 이동 버튼 구역]
        // ================================================================
        JButton mainBtn = new JButton("🏠 처음으로 (메인화면)");
        mainBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        mainBtn.setBackground(new Color(230, 242, 255)); // 로그인 뷰와 톤앤매너 맞춘 하늘색 톤!
        
        mainBtn.addActionListener(e -> {
            // 1. 현재 켜져 있는 티켓 조회/구매 창을 깔끔하게 숨기고 닫기
            this.setVisible(false);
            this.dispose();
            
            // 2. ⚡ [먹통 방지 & 꺼짐 방지 완벽 해결]
            // 새 창을 파는 게 아니라, 넘겨받은 진짜 원본 총사령관 리모컨을 작동시킵니다!
            if (navigator != null) {
                navigator.showMainMenu(); // 👈 기존 메인 메뉴 화면을 다시 살아 숨쉬게 깨우기!
            } else {
                // 혹시 모를 단독 테스트용 예외 방어막
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