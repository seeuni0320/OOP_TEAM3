package studycafe.controller;

import studycafe.model.Seat;
import studycafe.model.StudyCafeRepository;
import studycafe.model.Ticket;
import studycafe.model.User;
import studycafe.view.ViewNavigator;

/**
 * SeatController
 * - 좌석 현황/선택 화면을 띄운다.
 * - 사용자가 좌석을 클릭하면 빈 좌석인지 검증해
 *   이용 팝업을 띄우거나(신규 이용) 좌석 이동을 처리한다(이미 이용 중).
 *
 * [동시성 주의]
 * 좌석 데이터는 TimeSchedulerController(별도 스레드)와 공유되므로,
 * 좌석 상태를 읽고 바꾸는 구간은 공유 lock 객체로 동기화한다.
 */
public class SeatController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final Object seatLock; // TimeScheduler와 공유하는 잠금 객체

    public SeatController(StudyCafeRepository repository,
                          ViewNavigator navigator,
                          Session session,
                          Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.seatLock = seatLock;
    }

    /** 좌석 현황 & 선택 화면 열기 */
    public void openSeatSelection() {
        navigator.showSeatSelection(repository.getSeats());
    }

    /** 좌석 클릭 처리 */
    public void handleSeatClick(int seatNumber) {
        synchronized (seatLock) {
            Seat seat = repository.findSeat(seatNumber);
            if (seat == null) {
                navigator.showPopup("존재하지 않는 좌석입니다.");
                return;
            }

            User user = session.getUser();
            if (user == null) {
                navigator.showPopup("로그인 정보가 없습니다.");
                return;
            }

            if (user.getCurrentSeatNumber() == -1) {
                // 아직 좌석 미배정 → 신규 이용
                if (seat.isInUse()) {
                    navigator.showPopup("이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.");
                    return;
                }
                // 이용 팝업 → 사용자가 확인하면 startUse 실행
                navigator.showSeatUsagePopup(seat, () -> startUse(seat));
            } else {
                // 이미 이용 중 → 좌석 이동 요청 처리
                handleMove(user, seat);
            }
        }
    }

    /** 신규 이용 시작 (이용 팝업의 확인 콜백) */
    private void startUse(Seat seat) {
        synchronized (seatLock) {
            Ticket ticket = session.getSelectedTicket();
            if (ticket == null) {
                navigator.showPopup("선택된 이용권이 없습니다.");
                return;
            }
            if (seat.isInUse()) {
                navigator.showPopup("방금 다른 사용자가 선택한 좌석입니다. 다른 좌석을 선택해 주세요.");
                return;
            }

            User user = session.getUser();
            seat.startUse(user.getId(), ticket.getDurationMinutes());
            user.setCurrentSeatNumber(seat.getSeatNumber());
            ticket.markUsed();

            repository.saveSeats();
            repository.saveUsers();

            navigator.showPopup(seat.getSeatNumber() + "번 좌석 이용을 시작합니다. (남은 시간 "
                    + seat.getRemainingMinutes() + "분)");
            navigator.refreshSeats(repository.getSeats());
        }
    }

    /** 이용 중인 사용자가 다른 좌석을 클릭한 경우: 남은 시간을 가지고 이동 */
    private void handleMove(User user, Seat target) {
        if (target.getSeatNumber() == user.getCurrentSeatNumber()) {
            navigator.showPopup("현재 이용 중인 좌석입니다.");
            return;
        }
        if (target.isInUse()) {
            navigator.showPopup("이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.");
            return;
        }

        Seat current = repository.findSeat(user.getCurrentSeatNumber());
        int remaining = (current != null) ? current.getRemainingMinutes() : 0;
        String userId = user.getId();

        if (current != null) {
            current.release();
        }
        target.startUse(userId, remaining);
        user.setCurrentSeatNumber(target.getSeatNumber());

        repository.saveSeats();
        repository.saveUsers();

        navigator.showPopup(target.getSeatNumber() + "번 좌석으로 이동했습니다. (남은 시간 "
                + remaining + "분)");
        navigator.refreshSeats(repository.getSeats());
    }
}
