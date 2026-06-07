package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

/**
 * SeatController
 * - 좌석 현황/선택, 좌석 클릭 처리(신규 이용/이동), 전화번호 기준 퇴실.
 * - 잔여 검증은 user.hasUsableBalance()(정기권 유효 또는 분 잔여)로 통일.
 */
public class SeatController {

    // ========== 인자 4개 입력 (필드 + 생성자) ==========

    private final StudyCafeRepository repository; // 창고(좌석·회원 데이터)
    private final ViewNavigator navigator;         // 화면 전환 리모컨
    private final Session session;                 // 현재 손님 정보 보관소
    private final Object seatLock;                 // 좌석 동시 접근을 막는 자물쇠

    public SeatController(StudyCafeRepository repository,
                          ViewNavigator navigator,
                          Session session,
                          Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.seatLock = seatLock;
    }

    // ========== 도구 (보조 메서드) ==========

    // 좌석 번호로 좌석 찾기 (없으면 null)
    private Seat findSeat(int seatNumber) {
        for (Seat s : repository.getSeatList()) {
            if (s.getSeatNumber() == seatNumber) return s;
        }
        return null;
    }

    // 전화번호로 그 사람이 앉아 있는 좌석 찾기 (없으면 null)
    private Seat findSeatByUserPhone(String phone) {
        if (phone == null) return null;
        for (Seat s : repository.getSeatList()) {
            if (s.isOccupied() && phone.equals(s.getAssignedUserPhone())) {
                return s;
            }
        }
        return null;
    }

    // ========== 좌석 선택 · 입실 ==========

    // 좌석 선택 화면
    // repository에서 좌석 목록 가져오기 → navigator에게 좌석 선택 화면 보여달라고 요청
    public void openSeatSelection() {
        navigator.showSeatSelection(repository.getSeatList());
    }

    // 사용자가 좌석 버튼을 클릭했을 때 실행
    // ex)사용자가 3번 좌석을 누르면, handleSeatClick(3);
    public void handleSeatClick(int seatNumber) {
        // 새로 앉을 수 있는 좌석이면, 바로 앉히지 않고 먼저 확인 팝업을 띄움
        // 팝업을 띄우기 위해 임시로 좌석을 저장
        Seat seatToConfirm = null;
        //사용자에게 보여줄 안내 문구
        String message = null;
        //좌석 이동이 끝났을 때 메인 화면으로 돌아갈지 표시하는 변수
        boolean backToMain = false;

        synchronized (seatLock) {
            // 사용자가 누른 좌석 번호에 해당하는 Seat 객체를 찾는다
            Seat seat = findSeat(seatNumber);

            // 로그인한 사용자 확인, session에서 현재 사용자를 꺼냄
            User user = session.getUser();
            if (user == null) {
                // 사용자 정보가 없을때 출력
                message = "로그인 정보가 없습니다.";
            } else {
                //로그인한 사용자의 전화번호로, 이미 사용 중인 좌석이 있는지 찾음
                Seat current = findSeatByUserPhone(user.getPhoneNumber());
                if (current == null) {// 사용자가 아직 아무 좌석에도 앉아 있지 않음
                    if (seat.isOccupied()) {// 클릭한 좌석이 이미 사용 중이면 출력
                        message = "이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.";
                    } else {// 클릭한 좌석이 비어 있으면 우선 바로 앉히지 않고 seatToConfirm에 저장
                        seatToConfirm = seat;
                    }
                } else if (seat.getSeatNumber() == current.getSeatNumber()) {
                    // 사용자가 이미 앉아 있는데, 자기 좌석을 다시 눌렀을 때 출력
                    message = "현재 이용 중인 좌석입니다.";
                } else if (seat.isOccupied()) {
                    // 사용자가 이미 앉아 있는데, 다른 사용 중인 좌석을 눌렀을 때 출력
                    message = "이미 사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.";
                } else {
                    //사용자가 이미 앉아 있고, 다른 빈 좌석을 누름
                    current.release(); // 기존 좌석을 비움
                    seat.occupy(user.getPhoneNumber()); // 새 좌석에 사용자의 전화번호를 등록
                    repository.saveData(); // 변경된 좌석 상태를 저장
                    message = seat.getSeatNumber() + "번 좌석으로 이동했습니다."; // 이동 메시지 출력
                    backToMain = true; // 좌석 이동이 끝났으니 나중에 메인 화면으로 돌아가게 표시
                }
            }
        }

        if (seatToConfirm != null) {
            // 그 좌석을 target이라는 이름으로 다시 저장
            final Seat target = seatToConfirm;
            // target 좌석에 대해 "이 좌석을 이용하시겠습니까?" 팝업을 띄움
            // 사용자가 확인을 누르면, startUse(target) 실행 → 실제로 좌석을 사용 중으로 바꿈
            navigator.showSeatUsagePopup(target, () -> startUse(target));
            return;
        }
        if (message != null) {
            // message에 문구가 들어 있으면 팝업으로 보여줌
            navigator.showPopup(message);
        }
        if (backToMain) {
            //session 초기화 후 메인으로 이동
            session.clear();
            navigator.showMainMenu();
        }
    }

    // 확인창에서 "예"를 누르면 실제로 좌석에 앉힌다.
    private void startUse(Seat seat) {
        synchronized (seatLock) {
            User user = session.getUser();
            seat.occupy(user.getPhoneNumber()); // 좌석에 사용자 전화번호 등록
            // 비회원이 로그인~착석 사이에 자동 삭제로 빠졌을 수 있으므로 다시 등록
            repository.saveUser(user);
            repository.saveData(); // 변경된 좌석 상태 저장
        }

        navigator.showPopup(seat.getSeatNumber() + "번 좌석 이용을 시작합니다.");
        session.clear();
        navigator.showMainMenu();
    }

    // ========== 퇴실 ==========

    public void checkoutByPhone(String phone) {
        String message;

        synchronized (seatLock) {
            Seat current = findSeatByUserPhone(phone);
            if (current == null) {// 입력된 전화번호가 좌석 사용중이 아닐 때
                message = "현재 이용 중인 좌석이 없습니다.";
            } else {// 입력된 전화번호가 좌석 사용중일 때
                int seatNumber = current.getSeatNumber();
                current.release();

                // 비회원은 퇴실 시 잔여 시간과 무관하게 정보를 정리한다.
                // 잔여를 0으로 만들면 saveData()의 (동기화된) 비회원 자동 삭제가 처리한다.
                // 회원은 건드리지 않으므로 잔여 시간을 그대로 유지한다.
                User user = repository.findUser(phone); // 전화번호와 같이 저장된 정보 확인
                if (user != null && !user.isMember()) { // 사용자 정보가 있고 비회원일 때 이용권 잔여를 0으로
                    user.setRemainingMinutes(0);
                    user.setPeriodStartTime(0);
                    user.setPeriodEndTime(0);
                }
                repository.saveData();
                message = seatNumber + "번 좌석 퇴실이 완료되었습니다.";
            }
        }

        navigator.refreshSeats(repository.getSeatList());
        navigator.showPopup(message);
        session.clear();
        navigator.showMainMenu();
    }
}