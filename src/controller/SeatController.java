package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.Ticket;
import model.User;
import view.ViewNavigator;

/**
 * SeatController
 * - 좌석 현황/선택, 좌석 클릭 처리(신규 이용/이동), 전화번호 기준 퇴실.
 * - 잔여 검증은 user.hasUsableBalance()(정기권 유효 또는 분 잔여)로 통일.
 */
public class SeatController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final Object seatLock;

    public SeatController(StudyCafeRepository repository,
                          ViewNavigator navigator,
                          Session session,
                          Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.seatLock = seatLock;
    }

    public void openSeatSelection() {
        navigator.showSeatSelection(repository.getSeatList());
    }

    public void handleSeatClick(int seatNumber) {
        Seat seatToConfirm = null;
        String message = null;
        boolean backToMain = false;

        synchronized (seatLock) {
            Seat seat = findSeat(seatNumber);
            if (seat == null) {
                message = "존재하지 않는 좌석입니다.";
            } else {
                User user = session.getUser();
                if (user == null) {
                    message = "로그인 정보가 없습니다.";
                } else {
                    Seat current = findSeatByUserPhone(user.getPhoneNumber());
                    if (current == null) {
                        if (seat.isOccupied()) {
                            message = "이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.";
                        } else {
                            seatToConfirm = seat;
                        }
                    } else if (seat.getSeatNumber() == current.getSeatNumber()) {
                        message = "현재 이용 중인 좌석입니다.";
                    } else if (seat.isOccupied()) {
                        message = "이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.";
                    } else {
                        current.release();
                        seat.occupy(user.getPhoneNumber());
                        repository.saveData();
                        message = seat.getSeatNumber() + "번 좌석으로 이동했습니다.";
                        backToMain = true;
                    }
                }
            }
        }

        if (seatToConfirm != null) {
            final Seat target = seatToConfirm;
            navigator.showSeatUsagePopup(target, () -> startUse(target));
            return;
        }
        if (message != null) {
            navigator.showPopup(message);
        }
        if (backToMain) {
            session.clear();
            navigator.showMainMenu();
        }
    }

    private void startUse(Seat seat) {
        String message;
        boolean started = false;

        synchronized (seatLock) {
            Ticket ticket = session.getSelectedTicket();
            User user = session.getUser();

            if (ticket == null) {
                message = "선택된 이용권이 없습니다.";
            } else if (user == null) {
                message = "로그인 정보가 없습니다.";
            } else if (seat.isOccupied()) {
                message = "방금 다른 사용자가 선택한 좌석입니다. 다른 좌석을 선택해 주세요.";
            } else if (!user.hasUsableBalance()) {
                message = "잔여 이용권이 없습니다. 이용권을 먼저 구매해 주세요.";
            } else {
                seat.occupy(user.getPhoneNumber());
                repository.saveData();
                message = seat.getSeatNumber() + "번 좌석 이용을 시작합니다.";
                started = true;
            }
        }

        navigator.showPopup(message);
        if (started) {
            session.clear();
            navigator.showMainMenu();
        }
    }

    public void checkoutByPhone(String phone) {
        String message;

        synchronized (seatLock) {
            Seat current = findSeatByUserPhone(phone);
            if (current == null) {
                message = "현재 이용 중인 좌석이 없습니다.";
            } else {
                int seatNumber = current.getSeatNumber();
                current.release();
                repository.saveData();
                message = seatNumber + "번 좌석 퇴실이 완료되었습니다.";
            }
        }

        navigator.refreshSeats(repository.getSeatList());
        navigator.showPopup(message);
        session.clear();
        navigator.showMainMenu();
    }

    private Seat findSeat(int seatNumber) {
        for (Seat s : repository.getSeatList()) {
            if (s.getSeatNumber() == seatNumber) return s;
        }
        return null;
    }

    private Seat findSeatByUserPhone(String phone) {
        if (phone == null) return null;
        for (Seat s : repository.getSeatList()) {
            if (s.isOccupied() && phone.equals(s.getAssignedUserPhone())) {
                return s;
            }
        }
        return null;
    }
}
