import javax.swing.*;
import java.awt.*;

public class PaymentView extends JFrame {
    private MainMenuView mainView;
    private String selectedTicket;

    public PaymentView(MainMenuView mainView, String selectedTicket) {
        this.mainView = mainView;
        this.selectedTicket = selectedTicket;

        setTitle("가상 결제창");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1, 10, 10));

        JLabel infoLabel = new JLabel("<html><center>선택하신 상품: " + selectedTicket + "<br>결제를 진행하시겠습니까?</center></html>", SwingConstants.CENTER);
        JButton payButton = new JButton("결제 완료 (시뮬레이션)");

        payButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "결제가 완료되었습니다! 좌석 선택 화면으로 이동합니다.");
            setVisible(false);
            // ⑤ 결제가 완려되었으니 다음 단계인 좌석 선택창 띄우기
            new SeatSelectionView(mainView).setVisible(true);
        });

        add(infoLabel);
        add(payButton);
    }
}