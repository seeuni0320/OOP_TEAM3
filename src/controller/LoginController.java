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
 *
 * [중요] LoginView는 컨트롤러 호출 직후 자기 자신을 dispose()로 닫는다.
 *        로그인 창을 열 때 메인 화면도 이미 hideAll()로 숨겨진 상태이므로,
 *        여기서 팝업만 띄우고 그냥 return하면 '보이는 창이 하나도 없는' 상태가 되어
 *        프로그램이 종료된 것처럼 보인다.
 *        따라서 진입에 실패하는 모든 분기에서는 navigator.showMainMenu()로 복귀시킨다.
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
            navigator.showMainMenu(); // 빈 화면 방지: 메인으로 복귀
            return;
        }
        String phone = normalize(phoneNumber);

        User user = repository.findUser(phone);
        if (user == null) {
            navigator.showPopup("등록된 회원 정보가 없습니다.\n'신규등록'으로 회원 가입하거나 비회원으로 이용해 주세요.");
            navigator.showMainMenu(); // 빈 화면 방지: 메인으로 복귀
            return;
        }
        enterAsMember(user);
    }

    /**
     * 회원 신규등록: 회원 자격(isMember=true)만 영속적으로 부여한다.
     * 진입(이용권/좌석 화면)은 호출 측(LoginView)이 이어서 부르는 loginAsMember가 단 한 번만 처리한다.
     *
     * 잘못된 번호 안내도 여기서는 띄우지 않는다. LoginView가 registerMember 직후
     * loginAsMember를 부르므로, 형식 오류 팝업/복귀 처리는 loginAsMember가 한 번만 하게 해서
     * 동일 팝업이 두 번 뜨는 것을 막는다.
     */
    public void registerMember(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            return; // 안내/복귀는 뒤이어 호출되는 loginAsMember가 담당 (중복 팝업 방지)
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
            return; // 진입은 뒤이어 호출되는 loginAsMember가 담당 (중복 진입 방지)
        }

        User member = new User(phone);
        member.setMember(true);
        repository.saveUser(member);
        repository.saveData();
        navigator.showPopup("회원 등록이 완료되었습니다.");
        // 진입은 뒤이어 호출되는 loginAsMember가 담당 (중복 진입 방지)
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
            navigator.showMainMenu(); // 빈 화면 방지: 메인으로 복귀
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
        // 비회원 입구로 들어온 세션은 항상 게스트 모드로 취급한다.
        // (회원 번호로 이 입구를 타도 이번 세션에서는 정기권을 노출하지 않기 위함)
        session.setGuest(true);

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
            navigator.showMainMenu(); // 빈 화면 방지: 메인으로 복귀
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

        // 비회원은 퇴실 시 잔여 시간과 무관하게 정보를 정리한다.
        // 잔여를 0으로 만들면 saveData()의 비회원 자동 삭제가 처리한다.
        // 회원은 건드리지 않으므로 잔여 시간을 그대로 유지한다.
        User user = repository.findUser(phone);
        if (user != null && !user.isMember()) {
            user.setRemainingMinutes(0);
            user.setPeriodStartTime(0);
            user.setPeriodEndTime(0);
        }

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