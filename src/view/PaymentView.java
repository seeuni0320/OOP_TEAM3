package view;

import controller.PaymentController;
import model.Ticket;
import model.TimeTicket;
import model.PeriodTicket;

import javax.swing.*;
import java.awt.*;

public class PaymentView extends JFrame {
    // 💡 생성자 매개변수 맨 끝에 SwingNavigator navigator를 추가로 주입받습니다!
    public PaymentView(PaymentController paymentController, Ticket ticket, SwingNavigator navigator) {
        setTitle("결제 단계");
        setSize(380, 180); // 버튼이 가로로 2개 배치되므로 가로 크기를 늘려 공간 확보!
        setLocationRelativeTo(null);
        setLayout(new GridLayout(2, 1, 10, 10)); // 위아래 2단 구성 유지

        // 1. 안내 문구 만들기 (기본 이름과 가격 추출)
        String detailText = ticket.getTicketName() + " [" + ticket.getPrice() + "원]"; 

        if (ticket instanceof TimeTicket) {
            TimeTicket timeTicket = (TimeTicket) ticket;
            detailText += " (+" + timeTicket.getAddHours() + "시간 충전)";
        } else if (ticket instanceof PeriodTicket) {
            PeriodTicket periodTicket = (PeriodTicket) ticket;
            detailText += " (+" + periodTicket.getAddDays() + "일 이용)";
        }

        // 상단에 라벨 배치
        JLabel infoLabel = new JLabel(detailText, SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14)); 
        add(infoLabel);

        // ================================================================
        // 🛠️ [하단 버튼 영역 레이아웃 정렬 공정]
        // ================================================================
        // 하단 2단 영역에 버튼 두 개를 가로로 나란히 배치하기 위해 새로운 Panel(패널) 생성!
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));

        // 2. 하단 왼쪽: 결제하는 버튼
        JButton payBtn = new JButton(ticket.getPrice() + "원 결제하기");
        payBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        payBtn.setBackground(new Color(204, 255, 204)); // 결제 버튼은 눈에 띄게 은은한 연초록색!
        
        payBtn.addActionListener(e -> {
            paymentController.pay(ticket, ticket.getPrice()); 
        });
        
        buttonPanel.add(payBtn); // 주머니에 쏙

        // 3. ✨ 하단 오른쪽: 새로 추가된 메인 화면으로 이동 (결제 취소) 버튼
        JButton mainBtn = new JButton("🏠 결제 취소");
        mainBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        mainBtn.setBackground(new Color(255, 204, 204)); // 취소 버튼은 경고 톤앤매너에 맞춘 연빨강색!
        
        mainBtn.addActionListener(e -> {
            int reply = JOptionPane.showConfirmDialog(this, 
                    "정말 결제를 취소하고 처음 화면으로 돌아가시겠습니까?", 
                    "결제 취소 확인", JOptionPane.YES_NO_OPTION);
                    
            if (reply == JOptionPane.YES_OPTION) {
                // 1. 현재 결제 화면 프레임을 깔끔하게 닫고 메모리 정리
                this.setVisible(false);
                this.dispose();
                
                // 2. ⚡ [먹통 방지 & 꺼짐 방지 완벽 해결]
                // 껍데기 화면을 억지로 새로 파는 게 아니라, 넘겨받은 끈끈한 원본 총사령관 리모컨을 작동시킵니다!
                if (navigator != null) {
                    navigator.showMainMenu(); // 👈 기존 메인 메뉴 화면을 다시 살아 숨쉬게 깨우기!
                } else {
                    // 혹시 모를 단독 테스트용 예외 방어막
                    try {
                        MainMenuView mainFrame = new MainMenuView(null); 
                        mainFrame.setVisible(true);
                    } catch (Exception ex) {}
                }
            }
        });
        
        buttonPanel.add(mainBtn); // 주머니에 쏙

        // 하단 2단 자리에 최종 완성된 버튼 주머니(buttonPanel)를 통째로 얹기!
        add(buttonPanel);
        // ================================================================
    }
}