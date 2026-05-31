package studycafe.controller;

import studycafe.model.StudyCafeRepository;
import studycafe.model.User;
import studycafe.view.ViewNavigator;

/**
 * LoginController
 * - 회원이면 전화번호 입력 후 회원을 조회한다.
 * - 비회원이면 전화번호 입력 후 이용권 구매 단계로 이동한다.
 */
public class LoginController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Session session;
    private final TicketController ticketController;

    public LoginController(StudyCafeRepository repository,
                           ViewNavigator navigator,
                           Session session,
                           TicketController ticketController) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.ticketController = ticketController;
    }

    /** 회원 로그인: 전화번호로 회원을 조회한다. */
    public void loginAsMember(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }

        User user = repository.findUserByPhone(phoneNumber);
        if (user == null) {
            navigator.showPopup("등록된 회원 정보가 없습니다. 비회원으로 이용하거나 회원가입을 진행해 주세요.");
            return;
        }

        session.clear();
        session.setUser(user);
        session.setGuest(false);

        // 회원 → 보유 이용권 조회 및 선택 단계로 위임
        ticketController.handleMemberLogin(user);
    }

    /** 비회원: 전화번호 입력 후 바로 이용권 구매창으로 이동한다. */
    public void loginAsGuest(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }

        // 전화번호를 식별자로 하는 임시 비회원 생성
        User guest = new User(phoneNumber, "비회원", phoneNumber);

        session.clear();
        session.setUser(guest);
        session.setGuest(true);

        // 비회원은 보유 이용권이 없으므로 곧바로 구매 단계로
        ticketController.showPurchase();
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
