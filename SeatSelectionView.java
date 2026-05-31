import javax.swing.*;
import java.awt.*;

public class SeatSelectionView extends JFrame {
    private MainMenuView mainView;

    public SeatSelectionView(MainMenuView mainView) {
        this.mainView = mainView;

        setTitle("스터디카페 좌석 선택");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // 4행 4열 레이아웃
        setLayout(new GridLayout(4, 4, 10, 10)); 

        // 16개 좌석 버튼 만들기
        for (int i = 1; i <= 16; i++) {
            JButton seatBtn = new JButton(i + "번 좌석");
            seatBtn.setBackground(Color.GREEN); // 빈 좌석은 초록색

            int finalI = i;
            seatBtn.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, finalI + "번 좌석에 입실 처리되었습니다.");
                setVisible(false);
                mainView.setVisible(true); // 처음에 숨겨놨던 메인메뉴 다시 켜고 처음으로 돌아가기
            });
            add(seatBtn);
        }
    }
}