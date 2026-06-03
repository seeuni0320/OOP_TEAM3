package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.Ticket;
import model.User;
import view.ViewNavigator;

/**
 * SeatController
 * - 좌석 현황/선택 화면을 띄운다.
 * - 좌석 클릭 시 빈 좌석인지 검증해 이용 팝업을 띄우거나(신규) 좌석 이동을 처리한다(이미 이용 중).
 *
 * [모델 적응 메모]
 * - Seat에는 시간 개념이 없고 occupy(phone)/release()/isOccupied()만 있다.
 *   남은 시간은 회원(User)에 있으므로, 좌석 이동 시 시간 이전 로직이 필요 없다.
 * - repository.findSeat() / getSeats() 가 모델에 없어 getSeatList() 기반 헬퍼로 대체.
 * - User에 currentSeatNumber가 없어, 사용자의 현재 좌석은 좌석의 assignedUserPhone으로 역추적한다.
 *
 * [동시성] 좌석 데이터는 TimeSchedulerController(별도 스레드)와 공유하므로 seatLock으로 동기화한다.
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
        navigator.showSeatSelection(repository.getSeatList());
    }

    /** 좌석 클릭 처리 */
    public void handleSeatClick(int seatNumber) {
        synchronized (seatLock) {
            Seat seat = findSeat(seatNumber);
            if (seat == null) {
                navigator.showPopup("존재하지 않는 좌석입니다.");
                return;
            }

            User user = session.getUser();
            if (user == null) {
                navigator.showPopup("로그인 정보가 없습니다.");
                return;
            }

            Seat current = findSeatByUserPhone(user.getPhoneNumber());
            if (current == null) {
                // 아직 좌석 미배정 → 신규 이용
                if (seat.isOccupied()) {
                    navigator.showPopup("이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.");
                    return;
                }
                // 이용 팝업 → 사용자가 확인하면 startUse 실행
                navigator.showSeatUsagePopup(seat, () -> startUse(seat));
            } else {
                // 이미 이용 중 → 좌석 이동 요청 처리
                handleMove(user, seat, current);
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
            if (seat.isOccupied()) {
                navigator.showPopup("방금 다른 사용자가 선택한 좌석입니다. 다른 좌석을 선택해 주세요.");
                return;
            }

            User user = session.getUser();
            if (user.getRemainingHours() <= 0 && user.getRemainingDays() <= 0) {
                navigator.showPopup("잔여 이용 시간이 없습니다. 이용권을 먼저 구매해 주세요.");
                return;
            }

            seat.occupy(user.getPhoneNumber());
            repository.saveData();

            navigator.showPopup(seat.getSeatNumber() + "번 좌석 이용을 시작합니다.");
        }

        // 한 손님의 이용 흐름 완료 → 세션 비우고 첫 화면(메인 메뉴)으로 복귀
        session.clear();
        navigator.showMainMenu();
    }

    /** 이용 중인 사용자가 다른 좌석을 클릭한 경우: 좌석만 옮긴다(시간은 회원 계정에 있음). */
    private void handleMove(User user, Seat target, Seat current) {
        if (target.getSeatNumber() == current.getSeatNumber()) {
            navigator.showPopup("현재 이용 중인 좌석입니다.");
            return;
        }
        if (target.isOccupied()) {
            navigator.showPopup("이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.");
            return;
        }

        current.release();
        target.occupy(user.getPhoneNumber());
        repository.saveData();

        navigator.showPopup(target.getSeatNumber() + "번 좌석으로 이동했습니다.");

        // 이동도 좌석 배정 완료이므로 세션 비우고 첫 화면으로 복귀
        session.clear();
        navigator.showMainMenu();
    }

    /** 좌석 번호로 좌석 찾기 (repository.findSeat 대체) */
    private Seat findSeat(int seatNumber) {
        for (Seat s : repository.getSeatList()) {
            if (s.getSeatNumber() == seatNumber) {
                return s;
            }
        }
        return null;
    }

    /** 전화번호로 현재 이용 중인 좌석 역추적 (user.getCurrentSeatNumber 대체) */
    private Seat findSeatByUserPhone(String phone) {
        if (phone == null) {
            return null;
        }
        for (Seat s : repository.getSeatList()) {
            if (s.isOccupied() && phone.equals(s.getAssignedUserPhone())) {
                return s;
            }
        }
        return null;
    }
}