package view;

import controller.*;
import model.Seat;
import model.Ticket;
import javax.swing.*;
import java.util.Collection;
import java.util.List;

/**
 * [SwingNavigator]
 * 프론트엔드의 총사령관 역할을 하는 클래스입니다.
 * 화면 전환(Navigation)을 통제하고, 백엔드 Controller들과 View를 연결합니다.
 */

// viewNavigator를 받아서 진짜 자바 Swing 문법으로 화면을 켜고 끄고 새로고침해주는 코드입니다.


public class SwingNavigator implements ViewNavigator {
    // 5개의 화면 객체를 안전하게 관리할 변수
    private MainMenuView mainMenuView;
    private LoginView loginView;
    private TicketSelectionView ticketSelectionView;
    private PaymentView paymentView;
    private SeatSelectionView seatSelectionView;

    // 백엔드와 소통할 컨트롤러 리모컨들
    private LoginController loginController;
    private TicketController ticketController;
    private PaymentController paymentController;
    private SeatController seatController;

    public SwingNavigator() {
        // 초기화 로직 구조 유지
    }

    // [Main 전용] 프로그램 시작 시 백엔드 컨트롤러들을 한 번에 주입받는 메서드
    public void setControllers(LoginController lc, TicketController tc, PaymentController pc, SeatController sc) {
        this.loginController = lc;
        this.ticketController = tc;
        this.paymentController = pc;
        this.seatController = sc;
    }

    // 프로그램 최초 실행 시 메인 메뉴를 띄우는 스타트 버튼
    public void showMainMenu() {
        // 'this'(내비게이터 자신)을 호출.
        if (mainMenuView == null) mainMenuView = new MainMenuView(this); 
        hideAll();
        mainMenuView.setVisible(true);
    }

    @Override
    public void showPopup(String message) {
        // 간단한 알림 팝업창 출력
        JOptionPane.showMessageDialog(null, message);
    }

    @Override
    public void showLogin(boolean isMember) {
        hideAll();
        loginView = new LoginView(loginController, isMember);
        loginView.setVisible(true);
    }

    @Override
    public void showOwnedTickets(List<Ticket> usableTickets) {
        hideAll();
        // 끝에 true를 보내어 [보유 이용권 조회] 모드로 화면을 켭니다.
        ticketSelectionView = new TicketSelectionView(ticketController, usableTickets, true);
        ticketSelectionView.setVisible(true);
    }

    @Override
    public void showTicketPurchase(List<Ticket> catalog) {
        hideAll();
        // 끝에 false를 보내어 [신규 이용권 구매 메뉴판] 모드로 화면을 켭니다.
        ticketSelectionView = new TicketSelectionView(ticketController, catalog, false);
        ticketSelectionView.setVisible(true);
    }

    @Override
    public void showPayment(Ticket ticket) {
        hideAll();
        paymentView = new PaymentView(paymentController, ticket);
        paymentView.setVisible(true);
    }

    @Override
    public void showSeatSelection(Collection<Seat> seats) {
        hideAll();
        seatSelectionView = new SeatSelectionView(seatController, seats);
        seatSelectionView.setVisible(true);
    }

    @Override
    public void refreshSeats(Collection<Seat> seats) {
        // 좌석 화면이 켜져 있는 상태라면, 화면 전체를 껐다 켜지 않고 불빛(데이터)만 실시간 새로고침!
        if (seatSelectionView != null) {
            seatSelectionView.updateSeatButtons(seats); // 우리가 맞춘 isOccupied() 기반 메서드 호출!
        }
    }

    @Override
    public void showSeatUsagePopup(Seat seat, Runnable onConfirm) {
        // 좌석 선택 시 최종 확인을 받는 모달 팝업창
        int reply = JOptionPane.showConfirmDialog(null, 
                seat.getSeatNumber() + "번 자리를 이용하시겠습니까?", "좌석 선택 확인", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            onConfirm.run(); // 백엔드(Controller)로 '입실 처리' 로직 실행 요청!
        }
    }

    // 새로운 화면을 열기 전에 기존에 열려있던 모든 JFrame을 화면에서 숨깁니다.
    private void hideAll() {
        if (mainMenuView != null) mainMenuView.setVisible(false);
        if (loginView != null) loginView.setVisible(false);
        if (ticketSelectionView != null) ticketSelectionView.setVisible(false);
        if (paymentView != null) paymentView.setVisible(false);
        if (seatSelectionView != null) seatSelectionView.setVisible(false);
    }
}
