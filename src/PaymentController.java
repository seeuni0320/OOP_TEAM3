package studycafe.controller;

import studycafe.model.StudyCafeRepository;
import studycafe.model.Ticket;
import studycafe.model.User;
import studycafe.view.ViewNavigator;

/**
 * PaymentController
 * - 결제 버튼 클릭 시 금액을 검증하고,
 *   StudyCafeRepository를 통해 매출 데이터를 갱신한다.
 * - 결제가 끝나면 좌석 선택 단계로 보낸다.
 */
public class PaymentController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final SeatController seatController;

    public PaymentController(StudyCafeRepository repository,
                             ViewNavigator navigator,
                             Session session,
                             SeatController seatController) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.seatController = seatController;
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

        // 매출 데이터 갱신 (Repository가 파일/누적값 처리)
        repository.recordSale(ticket.getPrice());

        // 회원이면 구매한 이용권을 계정에 적립
        User user = session.getUser();
        if (user != null && !session.isGuest()) {
            user.addTicket(ticket);
            repository.saveUsers();
        }

        session.setSelectedTicket(ticket);

        if (change > 0) {
            navigator.showPopup("결제가 완료되었습니다. 거스름돈: " + change + "원");
        } else {
            navigator.showPopup("결제가 완료되었습니다.");
        }

        // 결제 완료 → 좌석 현황 & 선택
        seatController.openSeatSelection();
    }
}
