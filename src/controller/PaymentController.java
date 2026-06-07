package controller;

import model.PeriodTicket;
import model.StudyCafeRepository;
import model.Ticket;
import model.TimeTicket;
import model.User;
import view.ViewNavigator;

/**
 * PaymentController
 * - 시간권은 '분'으로 환산해 적립, 정기권은 '만료 시각'을 연장한다.
 * - 적립 + 저장은 seatLock 안에서, UI는 락 밖에서.
 */
public class PaymentController {

    // ========== 인자 5개 입력 (필드 + 생성자) ==========

    private final StudyCafeRepository repository; // 창고(데이터)
    private final ViewNavigator navigator;         // 화면 전환 리모컨
    private final Session session;                 // 현재 손님 정보 보관소
    private final SeatController seatController;    // 결제 후 좌석 선택 화면으로 넘기기 위해 필요
    private final Object seatLock;                 // 동시 접근을 막는 자물쇠

    public PaymentController(StudyCafeRepository repository,
                             ViewNavigator navigator,
                             Session session,
                             SeatController seatController,
                             Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.session = session;
        this.seatController = seatController;
        this.seatLock = seatLock;
    }

    // ========== 결제 ==========

    public void pay(Ticket ticket, int paidAmount) {//paidAmount는 실제 결제가 가능한 환경에서 사용
        User user = session.getUser();                         // 현재 손님 꺼내기
        if (user != null) {                                    // 손님이 있으면
            synchronized (seatLock) {                          // 자물쇠 잠그고(데이터 건드림)
                if (ticket instanceof TimeTicket) {            // 이용권이 시간권이면
                    TimeTicket t = (TimeTicket) ticket;        // 시간권 타입으로 변환
                    user.addRemainingMinutes(t.getAddHours() * 60 + t.getAddMinutes()); // 시간→분 적립
                } else if (ticket instanceof PeriodTicket) {   // 정기권이면
                    user.activatePeriod(((PeriodTicket) ticket).getAddDays()); // 만료일을 일수만큼 연장
                }
                repository.saveUser(user);                     // 비회원 자동삭제 대비해 다시 등록
                repository.saveData();                         // 변경 저장
            }
        }

        session.setSelectedTicket(ticket);                     // 결제한 이용권 기록
        navigator.showPopup("결제가 완료되었습니다.");             // 결제 완료 안내
        seatController.openSeatSelection();                    // 좌석 선택 화면으로 넘김
    }
}