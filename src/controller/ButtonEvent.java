import javax.swing.*;   // JFrame, JButton, JLabel 등 Swing 컴포넌트
import java.awt.*;      // Color, Dimension, Component 등 AWT 클래스
import java.awt.event.*; // ActionListener, ActionEvent 등 이벤트 관련 클래스

public class ButtonEvent extends JFrame {

    private int clickCount = 0; // 버튼 클릭 횟수를 저장하는 변수
    private JLabel label;       // 클릭 횟수를 표시할 레이블

    public ButtonEvent() {
        setTitle("Button Event Practice");              // 창 제목 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫기 버튼 클릭 시 프로그램 종료
        setSize(300, 200);                              // 창 크기 설정 (너비 300, 높이 200)
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // 컴포넌트를 세로로 배치

        label = new JLabel("클릭 횟수: 0");            // 초기 텍스트 설정
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // 레이블 가로 중앙 정렬

        JButton btn = new JButton("Click");             // 버튼 생성 및 텍스트 설정
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);  // 버튼 가로 중앙 정렬

        // 버튼에 클릭 이벤트 리스너 등록
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { // 버튼 클릭 시 호출되는 메소드
                clickCount++;                            // 클릭 횟수 1 증가
                label.setText("클릭 횟수: " + clickCount); // 레이블 텍스트 갱신

                if (clickCount >= 5) {                   // 클릭 횟수가 5 이상이면
                    getContentPane().setBackground(Color.GREEN); // 배경색을 초록으로 변경
                }
            }
        });

        getContentPane().setBackground(Color.YELLOW); // 초기 배경색을 노랑으로 설정

        add(Box.createVerticalGlue());                // 위쪽 여백 (컴포넌트를 수직 중앙으로 밀어줌)
        add(label);                                   // 레이블 추가
        add(Box.createRigidArea(new Dimension(0, 10))); // 레이블과 버튼 사이 10px 간격
        add(btn);                                     // 버튼 추가
        add(Box.createVerticalGlue());                // 아래쪽 여백 (컴포넌트를 수직 중앙으로 밀어줌)

        setVisible(true); // 창을 화면에 표시
    }

    public static void main(String[] args) {
        new ButtonEvent(); // 프로그램 시작 시 ButtonEvent 객체 생성
    }
}
