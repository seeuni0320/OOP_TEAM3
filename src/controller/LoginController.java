package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

import java.awt.Window;
import javax.swing.JFrame;

/**
 * LoginController
 *
 * View를 수정하지 않고 퇴실 기능을 처리하기 위해,
 * LoginView가 퇴실 모드에서도 loginAsMember()를 호출하는 구조를 그대로 사용한다.
 * 대신 현재 떠 있는 로그인 창 제목이 "퇴실 본인 인증"이면 일반 로그인 흐름이 아니라 퇴실 흐름으로 보낸다.
 */
public class LoginController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final TicketController ticketController;
    private final SeatController seatController;

    /**
     * 기존 Main 코드가 4개 인자 생성자를 쓰는 경우도 컴파일되도록 유지한다.
     * 단, 퇴실 처리는 SeatController가 있어야 가장 안전하므로 가능하면 5개 인자 생성자를 쓰는 것을 권장한다.
     */
    public LoginController(StudyCafeRepository repository,
                           ViewNavigator navigator,
                           Session session,
                           TicketController ticketController) {
        this(repository, navigator, session, ticketController, null);
    }

    public LoginController(StudyCafeRepository repository,
                           ViewNavigator navigator,
                           Session session,
                           TicketController ticketController,
                           SeatController seatController) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.ticketController = ticketController;
        this.seatController = seatController;
    }

    /** 회원 로그인 또는 퇴실 본인 인증 처리 */
    public void loginAsMember(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }

        String phone = normalize(phoneNumber);

        // LoginView를 수정하지 않기 위한 우회 처리:
        // 퇴실 화면의 버튼도 loginAsMember()를 호출하므로, 현재 창 제목으로 퇴실 흐름을 구분한다.
        if (isExitLoginWindowOpen()) {
            checkout(phone);
            return;
        }

        User user = repository.findUser(phone);
        if (user == null) {
            navigator.showPopup("등록된 회원 정보가 없습니다. 비회원으로 이용하거나 신규등록을 진행해 주세요.");
            return;
        }

        session.clear();
        session.setUser(user);
        session.setGuest(false);

        // 이미 좌석 이용 중이면 구매를 건너뛰고 좌석 이동만 허용한다.
        Seat occupied = findOccupiedSeatByPhone(phone);
        if (occupied != null) {
            navigator.showPopup("현재 " + occupied.getSeatNumber() + "번 좌석을 이용 중입니다.\n옮길 좌석을 선택하시면 이동됩니다.");
            navigator.showSeatSelection(repository.getSeatList());
            return;
        }

        ticketController.handleMemberLogin(user);
    }

    /** 비회원: 전화번호 입력 후 바로 이용권 구매창으로 이동한다. */
    public void loginAsGuest(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }

        String phone = normalize(phoneNumber);

        User guest = repository.findUser(phone);
        if (guest == null) {
            guest = new User(phone);
            repository.saveUser(guest);
        }

        session.clear();
        session.setUser(guest);
        session.setGuest(true);

        Seat occupied = findOccupiedSeatByPhone(phone);
        if (occupied != null) {
            navigator.showPopup("현재 " + occupied.getSeatNumber() + "번 좌석을 이용 중입니다.\n옮길 좌석을 선택하시면 이동됩니다.");
            navigator.showSeatSelection(repository.getSeatList());
            return;
        }

        ticketController.showPurchase();
    }

    /** 퇴실 처리: 전화번호 검증 후 실제 좌석 해제는 SeatController에 맡긴다. */
    public void checkout(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }

        String phone = normalize(phoneNumber);

        if (seatController != null) {
            seatController.checkoutByPhone(phone);
        } else {
            // 4개 인자 생성자를 사용하는 기존 Main을 위한 최소 방어 처리.
            checkoutWithoutSeatController(phone);
        }
    }

    /** SeatController가 주입되지 않은 경우의 방어용 퇴실 처리 */
    private void checkoutWithoutSeatController(String phone) {
        Seat occupied = findOccupiedSeatByPhone(phone);
        if (occupied == null) {
            navigator.showPopup("현재 이용 중인 좌석이 없습니다.");
            navigator.showMainMenu();
            return;
        }

        int seatNumber = occupied.getSeatNumber();
        occupied.release();
        repository.saveData();

        session.clear();
        navigator.showPopup(seatNumber + "번 좌석 퇴실이 완료되었습니다.");
        navigator.showMainMenu();
    }

    /** 현재 보이는 JFrame 중 퇴실 본인 인증 창이 있는지 확인한다. */
    private boolean isExitLoginWindowOpen() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window.isVisible()) {
                String title = ((JFrame) window).getTitle();
                if ("퇴실 본인 인증".equals(title)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 해당 전화번호가 현재 점유 중인 좌석을 찾는다. 없으면 null. */
    private Seat findOccupiedSeatByPhone(String phone) {
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

    /** 전화번호에서 숫자만 남겨 통일된 식별자로 만든다. */
    private String normalize(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }

    /** 숫자만 추출해 010으로 시작하는 10~11자리인지 간단히 검증 */
    private boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }

        String digits = phone.replaceAll("[^0-9]", "");
        return digits.matches("01[0-9]{8,9}");
    }
}
