package view;

import model.Seat;
import model.User;
import model.StudyCafeRepository;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class MainMenuView extends JFrame {
    private JPanel seatStatusPanel; 
    private StudyCafeRepository repository; 
    
    private JLabel[] seatLabels = new JLabel[16];

    public MainMenuView(SwingNavigator navigator) {
        setTitle("스터디카페 키오스크");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // 실시간 좌석 현황 바둑판 레이아웃 세팅
        seatStatusPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        seatStatusPanel.setBorder(BorderFactory.createTitledBorder("실시간 좌석 현황판"));
        seatStatusPanel.setPreferredSize(new Dimension(380, 400));
        add(seatStatusPanel, BorderLayout.WEST);

        //메인 제어 버튼 레이아웃 세팅
        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));

        JButton memberBtn = new JButton("회원 로그인 / 등록");
        JButton guestBtn = new JButton("비회원 이용권 구매");
        JButton exitBtn = new JButton("퇴실하기 (이용 종료)");

        Font btnFont = new Font("맑은 고딕", Font.BOLD, 15);
        memberBtn.setFont(btnFont); memberBtn.setBackground(new Color(215, 235, 255));
        guestBtn.setFont(btnFont); guestBtn.setBackground(new Color(230, 255, 230));
        exitBtn.setFont(btnFont); exitBtn.setBackground(new Color(255, 220, 220));

        memberBtn.addActionListener(e -> { if(navigator != null) navigator.showLogin(true); });
        guestBtn.addActionListener(e -> { if(navigator != null) navigator.showLogin(false); });
        exitBtn.addActionListener(e -> { if(navigator != null) navigator.showLoginForExit(); });

        menuPanel.add(memberBtn);
        menuPanel.add(guestBtn);
        menuPanel.add(exitBtn);
        add(menuPanel, BorderLayout.CENTER);
        
        //  프로그램이 처음 켜질 때 딱 한 번만 16개의 라벨 주머니를 생성해서 배치합니다.
        createSeatComponents();
    }

    public void setRepository(StudyCafeRepository repository) {
        this.repository = repository;
    }

   
    private void createSeatComponents() {
        seatStatusPanel.removeAll();
        for (int i = 0; i < 16; i++) {
            int seatNum = i + 1;
            JLabel seatLabel = new JLabel("", SwingConstants.CENTER);
            seatLabel.setOpaque(true);
            seatLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            
            // 초기 상태는 일단 전부 초록색 빈자리로 세팅
            seatLabel.setText("<html><center><b>" + seatNum + "번</b><br><font color='gray' size='3'>(빈자리)</font></center></html>");
            seatLabel.setBackground(new Color(153, 255, 153)); 
            seatLabel.setForeground(new Color(0, 102, 0));
            seatLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 153, 51), 1));
            
            seatLabels[i] = seatLabel; // 배열에 보관!
            seatStatusPanel.add(seatLabel);
        }
    }

   
    public void updateUsageStatus(Collection<Seat> seats) {
        // 데이터가 아직 안 들어왔거나 비어있으면 그냥 패스 (기본 빈자리 상태 유지)
        if (seats == null || seats.isEmpty()) {
            return;
        }

        for (Seat seat : seats) {
            int idx = seat.getSeatNumber() - 1;
            if (idx < 0 || idx >= 16) continue;
            
            JLabel seatLabel = seatLabels[idx]; // 보관 중인 라벨 리모컨 꺼내기
            if (seatLabel == null) continue;

            String timeText = "";
            
            if (seat.isOccupied()) {
                String phone = seat.getAssignedUserPhone();
                if (phone != null && !phone.isEmpty() && repository != null) {
                    User user = repository.findUser(phone);
                    if (user != null) {
                        if (user.isPeriodActive()) {
                            timeText = user.getRemainingDays() + "일";
                        } else {
                            int totalMinutes = user.getRemainingMinutes();
                            int hours = totalMinutes / 60;
                            int mins = totalMinutes % 60;
                            if (hours > 0) {
                                timeText = hours + "시" + mins + "분";
                            } else {
                                timeText = mins + "분";
                            }
                        }
                    } else {
                        timeText = "이용중";
                    }
                } else {
                    timeText = "이용중";
                }
            }

            // 부서뜨리지 않고, 텍스트와 테두리/배경 컬러만 실시간 스왑(Swap)
            if (seat.isOccupied()) {
                seatLabel.setText("<html><center><font color='white'><b>" + seat.getSeatNumber() + "번</b><br><font size='3'>[" + timeText + "]</font></font></center></html>");
                seatLabel.setBackground(new Color(255, 102, 102)); // 빨간 불
                seatLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            } else {
                seatLabel.setText("<html><center><b>" + seat.getSeatNumber() + "번</b><br><font color='gray' size='3'>(빈자리)</font></center></html>");
                seatLabel.setBackground(new Color(153, 255, 153)); // 초록 불
                seatLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 153, 51), 1));
            }
        }
        
        seatStatusPanel.repaint(); 
    }
}