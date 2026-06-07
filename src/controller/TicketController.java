package controller;

import model.PeriodTicket;
import model.Ticket;
import model.TimeTicket;
import model.User;
import view.ViewNavigator;

import java.util.ArrayList;
import java.util.List;

public class TicketController {

    private final ViewNavigator navigator;       // 화면 전환
    private final Session session;               // 손님 상태 보관
    private final SeatController seatController;  // 좌석 화면으로 넘길 때

    public TicketController(ViewNavigator navigator, Session session,
                            SeatController seatController) {
        this.navigator = navigator;
        this.session = session;
        this.seatController = seatController;
    }

    // 회원 로그인 직후: 보유 이용권 있으면 목록, 없으면 구매 화면
    public void handleMemberLogin(User user) {
        List<Ticket> owned = getOwnedTickets(user);
        if (!owned.isEmpty()) {
            navigator.showOwnedTickets(owned);
        } else {
            showPurchase();
        }
    }

    // 구매 화면 띄우기
    public void showPurchase() {
        navigator.showTicketPurchase(getTicketCatalog());
    }

    // 보유 이용권 선택: 결제 없이 좌석 선택으로
    public void selectOwnedTicket(Ticket ticket) {
        if (ticket == null) {
            navigator.showPopup("사용할 수 없는 이용권입니다.");
            return;
        }
        session.setSelectedTicket(ticket);
        seatController.openSeatSelection();
    }

    // 구매할 이용권 선택: 결제 화면으로
    public void selectTicketToPurchase(Ticket ticket) {
        if (ticket == null) {
            navigator.showPopup("이용권을 선택해 주세요.");
            return;
        }
        session.setSelectedTicket(ticket);
        navigator.showPayment(ticket);
    }

    // 회원이 가진 잔여를 화면용 Ticket 목록으로 만든다 (가격 0)
    private List<Ticket> getOwnedTickets(User user) {
        List<Ticket> list = new ArrayList<>();

        int minutes = user.getRemainingMinutes();
        if (minutes > 0) {                             // 시간권 잔여
            int h = minutes / 60;
            int m = minutes % 60;
            String label = (h > 0)
                    ? "시간권 (잔여 " + h + "시간 " + m + "분)"
                    : "시간권 (잔여 " + m + "분)";
            list.add(new Ticket(label, 0));
        }
        if (user.isPeriodActive()) {                   // 정기권 잔여
            list.add(new Ticket("정기권 (잔여 " + user.getRemainingDays() + "일)", 0));
        }
        return list;
    }

    // 판매 목록. 정기권은 회원(게스트 모드 아님)에게만 노출
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