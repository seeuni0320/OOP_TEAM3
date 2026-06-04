package controller;

import model.PeriodTicket;
import model.StudyCafeRepository;
import model.Ticket;
import model.TimeTicket;
import model.User;
import view.ViewNavigator;

/**
 * PaymentController
 * - 결제 금액을 검증하고, 구매한 이용권의 시간을 회원 계정에 적립한다.
 * - 결제가 끝나면 좌석 선택 단계로 보낸다.
 *
 * [모델 적응 메모]
 * - 시간은 좌석이 아니라 회원(User)에 누적되는 구조 → TimeTicket이면 addRemainingHours,
 *   PeriodTicket이면 addRemainingDays 로 적립.
 * - 저장은 모델의 통합 메서드 saveData() 사용.
 * - (모델 User에 '분' 단위 잔여가 없어 TimeTicket의 addMinutes는 현재 반영되지 않음.)
 *
 * [동시성 — 수정됨]
 * - 회원 잔여 적립 + saveData()를 seatLock 안에서 처리한다.
 *   (예전 코드는 락 없이 saveData()를 호출해, 스케줄러 스레드의 saveData()와 동시에
 *    같은 파일/컬렉션을 건드려 파일 손상·ConcurrentModificationException 위험이 있었다.)
 * - UI 호출(showPopup 등)은 락을 빠져나온 뒤에 한다.
 */
public class PaymentController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final SeatController seatController;
    private final Object seatLock; // 스케줄러/좌석 컨트롤러와 공유하는 잠금 객체

    public PaymentController(StudyCafeRepository repository,
                             ViewNavigator navigator,
                             Session session,
                             SeatController seatController,
                             Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.seatController = seatController;
        this.seatLock = seatLock;
    }

    /**
     * 결제 처리
     *
     * @param ticket     결제할 이용권
     * @param paidAmount 사용자가 투입/지불한 가상 금액
     */
    public void pay(Ticket ticket, int paidAmount) {
        if (ticket == null) {
            navigator.showPopup("선택된 이용권이 없습니다.");
            return;
        }
        if (paidAmount < ticket.getPrice()) {
            navigator.showPopup("결제 금액이 부족합니다. (가격: " + ticket.getPrice() + "원)");
            return;
        }

        int change = paidAmount - ticket.getPrice();

        // 구매한 이용권 시간을 회원 계정에 적립 (적립 + 저장은 락 안에서 한 번에)
        User user = session.getUser();
        if (user != null) {
            synchronized (seatLock) {
                if (ticket instanceof TimeTicket) {
                    user.addRemainingHours(((TimeTicket) ticket).getAddHours());
                } else if (ticket instanceof PeriodTicket) {
                    user.addRemainingDays(((PeriodTicket) ticket).getAddDays());
                }
                repository.saveData(); // 회원/좌석 정보를 한 번에 저장
            }
        }

        session.setSelectedTicket(ticket);

        // ── UI는 락 밖에서 ──
        if (change > 0) {
            navigator.showPopup("결제가 완료되었습니다. 거스름돈: " + change + "원");
        } else {
            navigator.showPopup("결제가 완료되었습니다.");
        }

        // 결제 완료 → 좌석 현황 & 선택
        seatController.openSeatSelection();
    }
}
