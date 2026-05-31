package studycafe.controller;

import studycafe.model.Ticket;
import studycafe.model.User;
import studycafe.view.ViewNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * TicketController
 * - 회원이 사용 가능한 이용권을 보유하고 있으면 결제를 생략하고 좌석 선택으로 보낸다.
 * - 보유 이용권이 없거나 비회원이면 이용권 구매창을 표시한다.
 */
public class TicketController {

    private final ViewNavigator navigator;
    private final Session session;
    private final SeatController seatController;
    private final PaymentController paymentController; // 구매 시 결제 화면 연계용

    public TicketController(ViewNavigator navigator,
                            Session session,
                            SeatController seatController,
                            PaymentController paymentController) {
        this.navigator = navigator;
        this.session = session;
        this.seatController = seatController;
        this.paymentController = paymentController;
    }

    /** 회원 로그인 직후 호출: 보유 이용권 유무에 따라 분기 */
    public void handleMemberLogin(User user) {
        List<Ticket> usable = user.getUsableTickets();
        if (!usable.isEmpty()) {
            // 있음 → 보유 이용권 조회 및 선택 화면
            navigator.showOwnedTickets(usable);
        } else {
            // 없음 → 이용권 구매창
            showPurchase();
        }
    }

    /** 이용권 구매창 표시 (비회원, 또는 보유 이용권 없는 회원) */
    public void showPurchase() {
        navigator.showTicketPurchase(getTicketCatalog());
    }

    /** 보유 이용권을 선택한 경우: 결제 생략 → 좌석 선택으로 */
    public void selectOwnedTicket(Ticket ticket) {
        if (ticket == null || ticket.isUsed()) {
            navigator.showPopup("사용할 수 없는 이용권입니다.");
            return;
        }
        session.setSelectedTicket(ticket);
        seatController.openSeatSelection();
    }

    /** 구매할 이용권을 선택한 경우: 결제 화면으로 이동 */
    public void selectTicketToPurchase(Ticket ticket) {
        if (ticket == null) {
            navigator.showPopup("이용권을 선택해 주세요.");
            return;
        }
        session.setSelectedTicket(ticket);
        navigator.showPayment(ticket);
    }

    /**
     * 판매 중인 이용권 목록.
     * 구매할 때마다 새 인스턴스가 발급되도록 매번 새로 생성한다.
     * (가격/종류는 기획에 맞게 수정)
     */
    private List<Ticket> getTicketCatalog() {
        List<Ticket> catalog = new ArrayList<>();
        catalog.add(new Ticket("2시간권", 120, 4000));
        catalog.add(new Ticket("4시간권", 240, 7000));
        catalog.add(new Ticket("정기권(30일)", 60 * 24 * 30, 99000));
        return catalog;
    }
}
