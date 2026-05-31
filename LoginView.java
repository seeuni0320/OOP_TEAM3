import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private MainMenuView mainView;
    private boolean isMember;

    public LoginView(MainMenuView mainView, boolean isMember) {
        this.mainView = mainView;
        this.isMember = isMember;

        setTitle(isMember ? "회원 로그인" : "비회원 전화번호 입력");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1, 10, 10));

        JLabel titleLabel = new JLabel(isMember ? "회원 전화번호를 입력하세요" : "비회원 전화번호를 입력하세요", SwingConstants.CENTER);
        JTextField phoneField = new JTextField(15);
        JButton nextButton = new JButton("다음 단계");

        nextButton.addActionListener(e -> {
            String phone = phoneField.getText();
            
            if(phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "전화번호를 입력해주세요!");
                return;
            }

            setVisible(false); // 로그인창 숨기기
            // 다음 단계인 이용권 조회/구매 창으로 이동하면서 입력한 폰번호와 회원여부 넘기기
            new TicketSelectionView(mainView, phone, isMember).setVisible(true);
        });

        add(titleLabel);
        
        // 텍스트 필드를 가운데 정렬하기 위한 패널
        JPanel fieldPanel = new JPanel();
        fieldPanel.add(phoneField);
        add(fieldPanel);
        
        add(nextButton);
    }
}