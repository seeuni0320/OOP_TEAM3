package controller;

import model.PeriodTicket;
import model.StudyCafeRepository;
import model.Ticket;
import model.TimeTicket;
import model.User;
import view.ViewNavigator;

/**
 * PaymentController
 * - 시간권은 '분'으로 환산해 적립, 정기권은 '만료 시각'을 연장한다.
 * - 적립 + 저장은 seatLock 안에서, UI는 락 밖에서.
 */
public class PaymentController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final SeatController seatController;
    private final Object seatLock;

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

        User user = session.getUser();
        if (user != null) {
            synchronized (seatLock) {
                if (ticket instanceof TimeTicket) {
                    TimeTicket t = (TimeTicket) ticket;
                    user.addRemainingMinutes(t.getAddHours() * 60 + t.getAddMinutes());
                } else if (ticket instanceof PeriodTicket) {
                    // 정기권: 보유 만료 시각을 days만큼 연장 (만료 시각 기반)
                    user.activatePeriod(((PeriodTicket) ticket).getAddDays());
                }
                // 비회원은 로그인~결제 사이에 saveData()의 자동 정리 로직으로
                // userMap에서 빠졌을 수 있다. 저장 전에 현재 사용자를 다시 등록해
                // '좌석은 점유됐는데 User가 없어 사용중으로만 표시'되는 문제를 막는다.
                repository.saveUser(user);
                repository.saveData();
            }
        }

        session.setSelectedTicket(ticket);

        if (change > 0) {
            navigator.showPopup("결제가 완료되었습니다. 거스름돈: " + change + "원");
        } else {
            navigator.showPopup("결제가 완료되었습니다.");
        }

        seatController.openSeatSelection();
    }
}
