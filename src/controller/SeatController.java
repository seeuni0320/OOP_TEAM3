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
 * [동시성 — 수정됨]
 * - seatLock 안에서는 '좌석 상태 읽기/변경'만 한다.
 * - 모달 팝업(showSeatUsagePopup, showPopup)은 절대 락을 쥔 채 띄우지 않는다.
 *   (예전 코드는 확인창을 락 안에서 띄워, 사용자가 창을 닫을 때까지 스케줄러가 멈췄다.)
 * - 신규 이용은 "확인창을 락 밖에서 띄우고 → 사용자가 확인하면 startUse에서 락을 다시 잡아 재검증"한다.
 *   확인하는 사이 다른 사람이 그 좌석을 차지했을 수 있으므로 재검증이 반드시 필요하다.
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
        Seat seatToConfirm = null; // 신규 이용: 락 밖에서 띄울 확인창 대상
        String message = null;     // 단순 안내 팝업 내용
        boolean backToMain = false; // 이동 완료 후 메인 메뉴로 복귀할지

        // ── 1) 락 안에서는 상태 판단/변경만 한다 ──
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
                        // 아직 좌석 미배정 → 신규 이용
                        if (seat.isOccupied()) {
                            message = "이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.";
                        } else {
                            seatToConfirm = seat; // 확인창은 락 밖에서 띄운다
                        }
                    } else if (seat.getSeatNumber() == current.getSeatNumber()) {
                        message = "현재 이용 중인 좌석입니다.";
                    } else if (seat.isOccupied()) {
                        message = "이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.";
                    } else {
                        // 좌석 이동: 상태 변경은 락 안에서 끝낸다(시간은 회원 계정에 있어 이전 불필요)
                        current.release();
                        seat.occupy(user.getPhoneNumber());
                        repository.saveData();
                        message = seat.getSeatNumber() + "번 좌석으로 이동했습니다.";
                        backToMain = true;
                    }
                }
            }
        }

        // ── 2) UI는 락 밖에서 ──
        if (seatToConfirm != null) {
            final Seat target = seatToConfirm;
            // 이용 팝업 → 사용자가 확인하면 startUse 실행(거기서 락 재획득 + 재검증)
            navigator.showSeatUsagePopup(target, () -> startUse(target));
            return;
        }
        if (message != null) {
            navigator.showPopup(message);
        }
        if (backToMain) {
            // 이동도 좌석 배정 완료이므로 세션 비우고 첫 화면으로 복귀
            session.clear();
            navigator.showMainMenu();
        }
    }

    /** 신규 이용 시작 (이용 팝업의 확인 콜백) */
    private void startUse(Seat seat) {
        String message;
        boolean started = false;

        // ── 락 안에서 재검증 + 점유 ──
        synchronized (seatLock) {
            Ticket ticket = session.getSelectedTicket();
            User user = session.getUser();

            if (ticket == null) {
                message = "선택된 이용권이 없습니다.";
            } else if (user == null) {
                message = "로그인 정보가 없습니다.";
            } else if (seat.isOccupied()) {
                // 확인하는 사이 누가 먼저 앉은 경우
                message = "방금 다른 사용자가 선택한 좌석입니다. 다른 좌석을 선택해 주세요.";
            } else if (user.getRemainingHours() <= 0 && user.getRemainingDays() <= 0) {
                message = "잔여 이용 시간이 없습니다. 이용권을 먼저 구매해 주세요.";
            } else {
                seat.occupy(user.getPhoneNumber());
                repository.saveData();
                message = seat.getSeatNumber() + "번 좌석 이용을 시작합니다.";
                started = true;
            }
        }

        // ── UI는 락 밖에서 ──
        navigator.showPopup(message);
        if (started) {
            // 한 손님의 이용 흐름 완료 → 세션 비우고 첫 화면(메인 메뉴)으로 복귀
            session.clear();
            navigator.showMainMenu();
        }
    }



    /** 전화번호 기준 퇴실 처리 */
    public void checkoutByPhone(String phone) {
        String message;
        boolean shouldReturnMain = true;

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

        if (shouldReturnMain) {
            session.clear();
            navigator.showMainMenu();
        }
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
