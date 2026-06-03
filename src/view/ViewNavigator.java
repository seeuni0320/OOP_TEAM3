package view;

import model.Seat;
import model.Ticket;
import java.util.Collection;
import java.util.List;

//controller가 view.viewNavigator를 부르고 있길래 파일 새로 만들어봤습니닷
public interface ViewNavigator {

    // 1. 메인 메뉴 화면을 띄우는 규칙
    void showMainMenu();

    // 2. 에러나 안내 메시지를 팝업창으로 띄우는 규칙
    void showPopup(String message);

    // 3. 로그인 화면을 띄우는 규칙 (회원/비회원 구분)
    void showLogin(boolean isMember);

    // 4. 회원이 보유한 이용권 목록 조회 화면을 띄우는 규칙
    void showOwnedTickets(List<Ticket> usableTickets);

    // 5. 신규 이용권 구매 메뉴판 화면을 띄우는 규칙
    void showTicketPurchase(List<Ticket> catalog);

    // 6. 결제 확인 및 결제하기 화면을 띄우는 규칙
    void showPayment(Ticket ticket);

    // 7. 좌석 선택 배정 화면을 띄우는 규칙
    void showSeatSelection(Collection<Seat> seats);

    // 8. 좌석의 실시간 상태(초록불/빨간불)를 새로고침하는 규칙
    void refreshSeats(Collection<Seat> seats);

    // 9. 좌석 클릭 시 진짜 앉을 건지 최종 확인 팝업을 띄우는 규칙
    void showSeatUsagePopup(Seat seat, Runnable onConfirm);
}
