import javax.swing.*;
import java.awt.*;

public class TicketSelectionView extends JFrame {
    private MainMenuView mainView;
    private String phoneNumber;
    private boolean isMember;

    public TicketSelectionView(MainMenuView mainView, String phoneNumber, boolean isMember) {
        this.mainView = mainView;
        this.phoneNumber = phoneNumber;
        this.isMember = isMember;

        setTitle("이용권 조회 및 선택");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel infoLabel = new JLabel("<html><center>입력된 번호: " + phoneNumber + "<br>" + 
                (isMember ? "[회원 모드] 보유 이용권을 조회합니다." : "[비회원 모드] 이용권을 구매해야 합니다.") + "</center></html>", SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        // 이용권 선택 버튼들 생성
        JPanel ticketPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton ticket1 = new JButton("당일권 2시간 (3,000원)");
        JButton ticket2 = new JButton("당일권 4시간 (5,000원)");
        JButton homeButton = new JButton("처음으로 돌아가기");
        homeButton.setBackground(Color.LIGHT_GRAY);

        // 처음으로 돌아가기 누르면 모든 창 닫고 메인메뉴 다시 켜기
        homeButton.addActionListener(e -> {
            setVisible(false);
            mainView.setVisible(true);
        });

        // 이용권 버튼 누르면 다음 창으로 가야하지만 일단 안내 메시지만 뜨게 처리
        ticket1.addActionListener(e -> {
    setVisible(false); // 이용권 선택창 숨기고
    new PaymentView(mainView, "당일권 2시간").setVisible(true); // ④ 결제창 열기!
});
        ticket2.addActionListener(e -> JOptionPane.showMessageDialog(this, "결제 및 좌석 선택창으로 넘어가는 로직이 필요합니다."));

        ticketPanel.add(ticket1);
        ticketPanel.add(ticket2);
        ticketPanel.add(homeButton);

        add(ticketPanel, BorderLayout.CENTER);
    }
}