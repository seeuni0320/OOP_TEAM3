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
 * - 회원이 사용 가능한 잔여 시간이 있으면 결제를 생략하고 좌석 선택으로 보낸다.
 * - 잔여가 없거나 비회원이면 이용권 구매창을 표시한다.
 *
 * [모델 적응 메모]
 * - 모델 User는 Ticket 리스트를 보관하지 않고 remainingHours/remainingDays로 잔여를 관리한다.
 *   따라서 "보유 이용권 조회"는 잔여 시간/일수를 표시용 Ticket으로 합성해서 보여준다.
 * - 카탈로그를 TimeTicket/PeriodTicket으로 생성하여 PaymentView의 instanceof 분기가 동작하도록 했다.
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

    /** 회원 로그인 직후 호출: 잔여 이용권 유무에 따라 분기 */
    public void handleMemberLogin(User user) {
        List<Ticket> owned = getOwnedTickets(user);
        if (!owned.isEmpty()) {
            navigator.showOwnedTickets(owned); // 있음 → 보유 이용권 조회 화면
        } else {
            showPurchase();                    // 없음 → 이용권 구매창
        }
    }

    /** 이용권 구매창 표시 (비회원, 또는 잔여 없는 회원) */
    public void showPurchase() {
        navigator.showTicketPurchase(getTicketCatalog());
    }

    /** 보유 이용권을 선택한 경우: 결제 생략 → 좌석 선택으로 */
    public void selectOwnedTicket(Ticket ticket) {
        if (ticket == null) {
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
     * 회원이 현재 보유 중인 잔여를 표시용 Ticket 목록으로 합성한다.
     * (모델 User에 잔여 Ticket 리스트가 없으므로 remainingHours/Days를 기반으로 생성)
     */
    private List<Ticket> getOwnedTickets(User user) {
        List<Ticket> list = new ArrayList<>();
        if (user.getRemainingHours() > 0) {
            list.add(new Ticket("시간권 (잔여 " + user.getRemainingHours() + "시간)", 0));
        }
        if (user.getRemainingDays() > 0) {
            list.add(new Ticket("정기권 (잔여 " + user.getRemainingDays() + "일)", 0));
        }
        return list;
    }

    /**
     * 판매 중인 이용권 목록.
     * TimeTicket(name, price, addHours, addMinutes) / PeriodTicket(name, price, addDays)
     * 정기권(기간권)은 회원만 구매 가능 → 비회원에게는 제외한다.
     */
    private List<Ticket> getTicketCatalog() {
        List<Ticket> catalog = new ArrayList<>();
        catalog.add(new TimeTicket("2시간권", 4000, 2, 0));
        catalog.add(new TimeTicket("4시간권", 7000, 4, 0));
        if (!session.isGuest()) {
            catalog.add(new PeriodTicket("정기권(30일)", 99000, 30));
        }
        return catalog;
    }
}