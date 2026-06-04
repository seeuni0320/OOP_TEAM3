package view;

import controller.SeatController;
import model.Seat;
import model.User;
import model.StudyCafeRepository; 
import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class SeatSelectionView extends JFrame {
    private final SeatController seatController;
    private final StudyCafeRepository repository; 
    private final JButton[] seatButtons = new JButton[16];

    public SeatSelectionView(SeatController seatController, StudyCafeRepository repository, Collection<Seat> seats, SwingNavigator navigator) {
        this.seatController = seatController;
        this.repository = repository; 
        
        setTitle("좌석 배치도");
        setSize(450, 520); 
        setLocationRelativeTo(null);
        
        // 전체 창을 BorderLayout으로 세팅!
        setLayout(new BorderLayout(10, 10)); 

        // 1.좌석 16개를 묶어줄 바둑판 전용 주머니(Panel) 생성
        JPanel seatPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        
        for (int i = 0; i < 16; i++) {
            seatButtons[i] = new JButton();
            int seatNum = i + 1;
            seatButtons[i].addActionListener(e -> seatController.handleSeatClick(seatNum));
            seatPanel.add(seatButtons[i]); // 전체 창이 아니라 바둑판 주머니에 버튼을 넣음
        }
        
        // 완성된 바둑판 주머니를 창의 '정중앙'에 배치!
        add(seatPanel, BorderLayout.CENTER); 

        // ================================================================
        //  [메인 화면으로 이동 버튼 구역]
        // ================================================================
        JButton mainBtn = new JButton("처음으로 (메인화면)");
        mainBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        mainBtn.setBackground(new Color(230, 242, 255)); // 하늘색 톤!
        mainBtn.setPreferredSize(new Dimension(450, 50)); 
        
        mainBtn.addActionListener(e -> {
            // 현재 열려있는 좌석 화면을 보이지 않게 닫고 메모리 해제
            this.setVisible(false);
            this.dispose();
            
            if (navigator != null) {
                navigator.showMainMenu(); // 기존 메인 메뉴 화면을 다시 살아 숨쉬게 깨우기!
            } else {
                // 혹시 모를 단독 테스트용 예외 방어막
                try {
                    MainMenuView mainFrame = new MainMenuView(null); 
                    mainFrame.setVisible(true);
                } catch (Exception ex) {}
            }
        });
        
        // 완성된 탈출 버튼을 창의 '맨 아래'에 정렬!
        add(mainBtn, BorderLayout.SOUTH);
        // ================================================================

        updateSeatButtons(seats); 
    }

    public void updateSeatButtons(Collection<Seat> seats) {
        for (Seat seat : seats) {
            int idx = seat.getSeatNumber() - 1;
            if (idx < 0 || idx >= 16) continue;

            if (seat.isOccupied()) { 
                seatButtons[idx].setBackground(new Color(255, 102, 102)); // 약간 부드러운 빨간색
                
                String phone = seat.getAssignedUserPhone();
                String timeText = "";
                
                if (phone != null && !phone.isEmpty() && repository != null) {
                    User user = repository.findUser(phone);
                    if (user != null) {
                        int minutes = user.getRemainingMinutes();
                        int days = user.getRemainingDays();
                        
                        if (days > 0) {
                            timeText = days + "일 남음";
                        } else {
                            timeText = minutes + "시간 남음";
                        }
                    } else {
                        timeText = "이용 중"; 
                    }
                } else {
                    timeText = "시간 정보 없음";
                }

                seatButtons[idx].setText("<html><center><font color='white'><b>" + seat.getSeatNumber() + "번</b><br>[" + timeText + "]</font></center></html>");
                seatButtons[idx].setEnabled(false); 
            } else {
                seatButtons[idx].setBackground(new Color(102, 255, 102)); // 약간 화사한 초록색
                seatButtons[idx].setText("<html><center><b>" + seat.getSeatNumber() + "번</b><br><font color='gray' size='3'>(빈자리)</font></center></html>");
                seatButtons[idx].setEnabled(true);
            }
        }
    }
}