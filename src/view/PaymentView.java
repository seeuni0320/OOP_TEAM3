package view;

import controller.PaymentController;
import model.Ticket;
import model.TimeTicket;
import model.PeriodTicket;

import javax.swing.*;
import java.awt.*;

public class PaymentView extends JFrame {
    public PaymentView(PaymentController paymentController, Ticket ticket) {
        setTitle("결제 단계");
        setSize(350, 180); 
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 1, 10, 10)); // 위아래 2단 구성

        // 1. 안내 문구 만들기 (기본 이름과 가격 추출)
        String detailText = ticket.getTicketName() + " [" + ticket.getPrice() + "원]"; 

        //model파트)여기서 TimeTicket, PeriodTicket을 참조해야하는데, Ticket파일에 둘 다 일반class로 선언되어있어서, 제가 가져오기가 힘들어요..!! 
        //그래서 각자 파일을 따로 만들어서 TimeTicket, PeriodTicket 각자 public으로 선언해주시면 더 좋을 거 같아용 ㅎㅎ 
        if (ticket instanceof TimeTicket) {
            TimeTicket timeTicket = (TimeTicket) ticket;
            detailText += " (+" + timeTicket.getAddHours() + "시간 충전)";
        } else if (ticket instanceof PeriodTicket) {
            PeriodTicket periodTicket = (PeriodTicket) ticket;
            detailText += " (+" + periodTicket.getAddDays() + "일 이용)";
        }

        // 상단에 라벨 배치
        JLabel infoLabel = new JLabel(detailText, SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14)); // 글씨 조금 더 선명하게
        add(infoLabel);

        // 2. 하단: 결제하는 버튼
        JButton payBtn = new JButton(ticket.getPrice() + "원 결제하기");
        payBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        
        payBtn.addActionListener(e -> {
            //  티켓 가격을 컨트롤러에 바로 토스!
            paymentController.pay(ticket, ticket.getPrice()); //여기서 paymentController로 넘어갑니당 
        });
        
        add(payBtn);
    }
}
