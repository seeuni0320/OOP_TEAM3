package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

/**
 * LoginController
 * - 회원이면 전화번호로 회원을 조회한다.
 * - 비회원이면 전화번호 입력 후 이용권 구매 단계로 이동한다.
 * - 이미 좌석을 이용 중인 번호로는 재진입을 막아 한 사람이 두 좌석을 점유하지 못하게 한다.
 *
 * [메모]
 * - 전화번호는 숫자만 남겨(예: 010-1234-5678 → 01012345678) 통일된 식별자로 사용한다.
 *   (표기만 달라도 다른 사람으로 인식되는 문제 방지)
 * - 비회원도 좌석 타이머(TimeScheduler)가 전화번호로 찾을 수 있도록 repository에 등록한다.
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
        String phone = normalize(phoneNumber);

        User user = repository.findUser(phone);
        if (user == null) {
            navigator.showPopup("등록된 회원 정보가 없습니다. 비회원으로 이용하거나 신규등록을 진행해 주세요.");
            return;
        }

        session.clear();
        session.setUser(user);
        session.setGuest(false);

        // 이미 좌석 이용 중이면 구매를 건너뛰고 좌석 이동만 허용한다.
        // (새 이용권을 사지 않고 좌석만 옮기게 → 클릭 시 handleMove가 기존 좌석을 비우므로 두 좌석 점유 방지)
        Seat occupied = findOccupiedSeatByPhone(phone);
        if (occupied != null) {
            navigator.showPopup("현재 " + occupied.getSeatNumber() + "번 좌석을 이용 중입니다.\n옮길 좌석을 선택하시면 이동됩니다.");
            navigator.showSeatSelection(repository.getSeatList());
            return;
        }

        // 회원 → 보유 이용권 유무에 따라 분기
        ticketController.handleMemberLogin(user);
    }

    /** 비회원: 전화번호 입력 후 바로 이용권 구매창으로 이동한다. */
    public void loginAsGuest(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
            return;
        }
        String phone = normalize(phoneNumber);

        // 모델 User는 전화번호 1-인자 생성자만 제공한다.
        User guest = repository.findUser(phone);
        if (guest == null) {
            guest = new User(phone);
            // 좌석 타이머가 전화번호로 사용자를 찾아 시간을 차감할 수 있도록 등록
            repository.saveUser(guest);
        }

        session.clear();
        session.setUser(guest);
        session.setGuest(true);

        // 이미 좌석 이용 중이면 구매를 건너뛰고 좌석 이동만 허용한다.
        Seat occupied = findOccupiedSeatByPhone(phone);
        if (occupied != null) {
            navigator.showPopup("현재 " + occupied.getSeatNumber() + "번 좌석을 이용 중입니다.\n옮길 좌석을 선택하시면 이동됩니다.");
            navigator.showSeatSelection(repository.getSeatList());
            return;
        }

        // 비회원은 보유 이용권이 없으므로 곧바로 구매 단계로
        ticketController.showPurchase();
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