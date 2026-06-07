package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

/**
 * LoginController
 * - 회원 로그인 / 회원 신규등록 / 비회원 이용 / 퇴실 본인 인증을 처리한다.
 * - 회원/비회원은 User.isMember()라는 '영속 속성'으로 구분한다.
 *   (잔여 시간이 0이 되어도 회원 자격은 사라지지 않는다.)
 *
 * [중요] LoginView는 컨트롤러 호출 직후 자기 자신을 dispose()로 닫는다.
 *        진입에 실패하는 모든 분기에서는 navigator.showMainMenu()로 복귀시켜
 *        '보이는 창이 하나도 없는' 상태(프로그램이 꺼진 것처럼 보임)를 막는다.
 */
public class LoginController {

    // ========== 인자 5개 입력 (필드 + 생성자) ==========

    private final StudyCafeRepository repository;   // 창고(회원·좌석 데이터)
    private final ViewNavigator navigator;           // 화면 전환 리모컨
    private final Session session;                   // 현재 손님 정보 보관소
    private final TicketController ticketController;  // 로그인 후 이용권 단계로 넘기기 위해 필요
    private final SeatController seatController;      // 퇴실 처리를 맡기기 위해 필요

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

    // ========== 도구 (보조 메서드) ==========

    /** 번호 형식이 맞으면 true. 틀리면 안내 팝업 + 메인 복귀 후 false. */
    private boolean checkPhone(String phoneNumber) {
        if (isValidPhone(phoneNumber)) return true;
        navigator.showPopup("전화번호 형식이 올바르지 않습니다.");
        navigator.showMainMenu(); // 빈 화면 방지: 메인으로 복귀
        return false;
    }

    /** 입력된 번호가 앉아 있는 좌석 찾기 (없으면 null) */
    private Seat findOccupiedSeatByPhone(String phone) {
        if (phone == null) return null;
        for (Seat s : repository.getSeatList()) {
            if (s.isOccupied() && phone.equals(s.getAssignedUserPhone())) {
                return s;
            }
        }
        return null;
    }

    /** 전화번호에서 숫자만 남기기 (예: 010-1234-5678 → 01012345678) */
    private String normalize(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }

    /** 전화번호 형식 검사 (01로 시작 + 총 10~11자리면 true) */
    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.matches("01[0-9]{8,9}");
    }

    // ========== 회원 ==========

    /** 회원 로그인: 이미 등록된 회원만 진입한다. */
    public void loginAsMember(String phoneNumber) {
        if (!checkPhone(phoneNumber)) return;
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
     * 진입(이용권/좌석 화면)은 LoginView가 이어서 부르는 loginAsMember가 단 한 번만 처리한다.
     * 형식 오류 안내도 여기서는 띄우지 않는다(loginAsMember가 한 번만 하게 해서 중복 팝업 방지).
     */
    public void registerMember(String phoneNumber) {
        if (!isValidPhone(phoneNumber)) {
            return; // 안내/복귀는 뒤이어 호출되는 loginAsMember가 담당 (중복 팝업 방지)
        }
        String phone = normalize(phoneNumber);

        User existing = repository.findUser(phone);
        if (existing != null) {
            if (!existing.isMember()) {
                // 비회원으로 쓰다가 신규 가입 시 회원으로 승격
                existing.setMember(true);
                repository.saveData();
                navigator.showPopup("비회원으로 이용하시던 번호를 회원으로 전환했습니다.");
            } else {
                // 이미 회원인 번호
                navigator.showPopup("이미 등록된 회원입니다. 회원으로 로그인합니다.");
            }
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

    // ========== 비회원 ==========

    /** 비회원 이용: 회원 자격은 부여하지 않는다. */
    public void loginAsGuest(String phoneNumber) {
        if (!checkPhone(phoneNumber)) return;
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

    // ========== 퇴실 ==========

    /** 퇴실 본인 인증 (LoginView 퇴실 모드 전용 진입점) */
    public void exitAsMember(String phoneNumber) {
        checkout(phoneNumber);
    }

    /** 퇴실 처리: 실제 좌석 해제는 SeatController에 맡긴다. */
    public void checkout(String phoneNumber) {
        if (!checkPhone(phoneNumber)) return;
        seatController.checkoutByPhone(normalize(phoneNumber));
    }
}