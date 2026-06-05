package controller;

import model.PeriodTicket;
import model.Ticket;
import model.TimeTicket;
import model.User;
import view.ViewNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * TicketController
 * - 보유 이용권(분/정기권 유효기간)이 있으면 결제를 생략하고 좌석 선택으로.
 * - 정기권(기간권)은 '회원(isMember)'만 구매 가능.
 */
public class TicketController {

    private final ViewNavigator navigator;
    private final Session session;
    private final SeatController seatController;
    private final PaymentController paymentController;

    public TicketController(ViewNavigator navigator,
                            Session session,
                            SeatController seatController,
                            PaymentController paymentController) {
        this.navigator = navigator;
        this.session = session;
        this.seatController = seatController;
        this.paymentController = paymentController;
    }

    public void handleMemberLogin(User user) {
        List<Ticket> owned = getOwnedTickets(user);
        if (!owned.isEmpty()) {
            navigator.showOwnedTickets(owned);
        } else {
            showPurchase();
        }
    }

    public void showPurchase() {
        navigator.showTicketPurchase(getTicketCatalog());
    }

    public void selectOwnedTicket(Ticket ticket) {
        if (ticket == null) {
            navigator.showPopup("사용할 수 없는 이용권입니다.");
            return;
        }
        session.setSelectedTicket(ticket);
        seatController.openSeatSelection();
    }

    public void selectTicketToPurchase(Ticket ticket) {
        if (ticket == null) {
            navigator.showPopup("이용권을 선택해 주세요.");
            return;
        }
        session.setSelectedTicket(ticket);
        navigator.showPayment(ticket);
    }

    /** 회원의 현재 보유 잔여를 표시용 Ticket으로 합성 */
    private List<Ticket> getOwnedTickets(User user) {
        List<Ticket> list = new ArrayList<>();

        int minutes = user.getRemainingMinutes();
        if (minutes > 0) {
            int h = minutes / 60;
            int m = minutes % 60;
            String label = (h > 0)
                    ? "시간권 (잔여 " + h + "시간 " + m + "분)"
                    : "시간권 (잔여 " + m + "분)";
            list.add(new Ticket(label, 0));
        }
        if (user.isPeriodActive()) {
            list.add(new Ticket("정기권 (잔여 " + user.getRemainingDays() + "일)", 0));
        }
        return list;
    }

    /**
     * 판매 카탈로그. 정기권은 '회원 입구로 들어온 회원'에게만 노출한다.
     * 회원 번호라도 '비회원 이용' 입구로 들어온 세션(session.isGuest()=true)에는 정기권을 노출하지 않는다.
     */
    private List<Ticket> getTicketCatalog() {
        List<Ticket> catalog = new ArrayList<>();
        catalog.add(new TimeTicket("2시간권", 4000, 2, 0));
        catalog.add(new TimeTicket("4시간권", 7000, 4, 0));

        User user = session.getUser();
        if (user != null && user.isMember() && !session.isGuest()) {
            catalog.add(new PeriodTicket("정기권(30일)", 99000, 30));
        }
        return catalog;
    }
}
