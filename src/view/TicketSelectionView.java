package view;

import controller.TicketController;
import model.Ticket;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TicketSelectionView extends JFrame {
    //  isOwnedMode가 true면 [보유 조회창], false면 [신규 구매 메뉴판]으로 갑니다
    public TicketSelectionView(TicketController ticketController, List<Ticket> tickets, boolean isOwnedMode) {
        setTitle(isOwnedMode ? "보유 이용권 조회" : "신규 이용권 구매");
        setSize(300, 350);
        setLocationRelativeTo(null);
        
        // 버튼 개수(+ 조회 모드일 땐 추가 구매 버튼용 1칸)만큼 세로 레이아웃 설정
        setLayout(new GridLayout(tickets.size() + (isOwnedMode ? 1 : 0), 1, 5, 5)); 

        // 넘겨받은 티켓 목록을 돌면서 버튼을 자동으로 생성합니다.
        for (Ticket ticket : tickets) {
            JButton btn = new JButton(ticket.getTicketName() + " (" + ticket.getPrice() + "원)");
            
            btn.addActionListener(e -> {
                if (isOwnedMode) {
                    // 이미 가진 티켓을 선택함 -> 좌석 선택하러 이동하라고 컨트롤러에 전달! ticketController
                    ticketController.selectOwnedTicket(ticket);
                } else {
                    // 새로 살 티켓을 선택함 -> 결제창(PaymentView) 띄우라고 컨트롤러에 전달!
                    ticketController.selectTicketToPurchase(ticket);
                }
            });
            add(btn);
        }

        // 보유 이용권 조회 모드일 때만 맨 밑에 '추가 구매' (회원이긴한데 이용권이 없을 경우)
        if (isOwnedMode) {
            JButton moreBtn = new JButton("이용권 추가 구매");
            moreBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            moreBtn.addActionListener(e -> { 
                setVisible(false); 
                ticketController.showPurchase(); // 구매 메뉴판 켜달라고 컨트롤러에 요청
            });
            add(moreBtn);
        }
    }
}
