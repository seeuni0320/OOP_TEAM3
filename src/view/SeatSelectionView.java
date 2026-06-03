public class SeatSelectionView extends JFrame {
    private final SeatController seatController;
    private final JButton[] seatButtons = new JButton[16];

    public SeatSelectionView(SeatController seatController, Collection<Seat> seats) {
        this.seatController = seatController;
        setTitle("좌석");
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 4)); // 4x4 바둑판

        for (int i = 0; i < 16; i++) {
            seatButtons[i] = new JButton();
            int seatNum = i + 1;
            seatButtons[i].addActionListener(e -> seatController.handleSeatClick(seatNum)); //손님이 좌석 버튼을 누르면, seatController로 넘어갑니당
            add(seatButtons[i]);
        }
        updateSeatButtons(seats); 
    }
    //model의 좌석 데이터에 맞춰서, 사용중이면 빨간불, 사용하지 않고 있으면 초록불 들어오는 코드예요.
    public void updateSeatButtons(Collection<Seat> seats) {
        for (Seat seat : seats) {
            int idx = seat.getSeatNumber() - 1;
            if (idx < 0 || idx >= 16) continue;

            if (seat.isOccupied()) { //model파트의 Seat.java 파일에 있는 함수 사용한 겁니다.(isOccupied())
                seatButtons[idx].setBackground(Color.RED);
                seatButtons[idx].setText(seat.getSeatNumber() + "번 (사용중)");
            } else {
                seatButtons[idx].setBackground(Color.GREEN);
                seatButtons[idx].setText(seat.getSeatNumber() + "번 (빈자리)");
            }
        }
    }
}
