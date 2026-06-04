package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

/**
 * LoginController
 * - 회원 로그인 / 회원 신규등록 / 비회원 이용 / 퇴실 본인 인증을 처리한다.
 * - 회원/비회원은 이제 User.isMember()라는 '영속 속성'으로 구분한다.
 *   (잔여 시간이 0이 되어도 회원 자격은 사라지지 않는다.)
 */
public class LoginController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final TicketController ticketController;
    private final SeatController seatController;

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

    /** 회원 로그인: 이미 등록된 회원만 진입한다. */
    public void loginAsMember(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }
        String phone = normalize(phoneNumber);

        User user = repository.findUser(phone);
        if (user == null) {
            navigator.showPopup("등록된 회원 정보가 없습니다.\n'신규등록'으로 회원 가입하거나 비회원으로 이용해 주세요.");
            return;
        }
        enterAsMember(user);
    }

    /** 회원 신규등록: 회원 자격(isMember=true)을 영속적으로 부여한다. */
    public void registerMember(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }
        String phone = normalize(phoneNumber);

        User existing = repository.findUser(phone);
        if (existing != null) {
            // 이미 비회원으로 쓰던 번호면 회원으로 승격
            if (!existing.isMember()) {
                existing.setMember(true);
                repository.saveData();
            }
            navigator.showPopup("이미 등록된 번호입니다. 회원으로 로그인합니다.");
            enterAsMember(existing);
            return;
        }

        User member = new User(phone);
        member.setMember(true);
        repository.saveUser(member);
        repository.saveData();
        navigator.showPopup("회원 등록이 완료되었습니다.");
        enterAsMember(member);
    }

    /** 회원 진입 공통 처리 (로그인/등록이 공유) */
    private void enterAsMember(User user) {
        session.clear();
        session.setUser(user);
        session.setGuest(false);

        // 이미 좌석 이용 중이면 구매를 건너뛰고 좌석 이동만 허용한다.
        Seat occupied = findOccupiedSeatByPhone(user.getPhoneNumber());
        if (occupied != null) {
            navigator.showPopup("현재 " + occupied.getSeatNumber() + "번 좌석을 이용 중입니다.\n옮길 좌석을 선택하시면 이동됩니다.");
            navigator.showSeatSelection(repository.getSeatList());
            return;
        }
        ticketController.handleMemberLogin(user);
    }

    /** 비회원 이용: 회원 자격은 부여하지 않는다. */
    public void loginAsGuest(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }
        String phone = normalize(phoneNumber);

        User guest = repository.findUser(phone);
        if (guest == null) {
            guest = new User(phone); // isMember 기본 false
            repository.saveUser(guest);
        }

        session.clear();
        session.setUser(guest);
        session.setGuest(!guest.isMember());

        Seat occupied = findOccupiedSeatByPhone(phone);
        if (occupied != null) {
            navigator.showPopup("현재 " + occupied.getSeatNumber() + "번 좌석을 이용 중입니다.\n옮길 좌석을 선택하시면 이동됩니다.");
            navigator.showSeatSelection(repository.getSeatList());
            return;
        }
        ticketController.showPurchase();
    }

    /** 퇴실 본인 인증 (LoginView 퇴실 모드 전용 진입점) */
    public void exitAsMember(String phoneNumber) {
        checkout(phoneNumber);
    }

    /** 퇴실 처리: 실제 좌석 해제는 SeatController에 맡긴다. */
    public void checkout(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }
        String phone = normalize(phoneNumber);

        if (seatController != null) {
            seatController.checkoutByPhone(phone);
        } else {
            checkoutWithoutSeatController(phone);
        }
    }

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

    private Seat findOccupiedSeatByPhone(String phone) {
        if (phone == null) return null;
        for (Seat s : repository.getSeatList()) {
            if (s.isOccupied() && phone.equals(s.getAssignedUserPhone())) {
                return s;
            }
        }
        return null;
    }

    private String normalize(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.matches("01[0-9]{8,9}");
    }
}
