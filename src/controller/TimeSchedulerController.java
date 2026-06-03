package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TimeSchedulerController
 * - 멀티스레드로 구동한다.
 * - 주기마다 이용 중인 좌석의 사용자(User) 잔여 시간을 차감하고,
 *   시간이 만료되면 좌석을 자동 퇴실 처리한다.
 *
 * [모델 적응 메모]
 * - 모델은 남은 시간을 좌석이 아니라 회원(User.remainingHours)에 저장한다.
 *   따라서 좌석의 assignedUserPhone으로 사용자를 찾아 시간을 차감한다.
 * - 모델 단위가 '시간'이므로 한 틱마다 1시간 차감한다(원본의 분 단위 차감에서 변경).
 *   데모 시에는 아래 TICK_UNIT을 SECONDS 등으로 바꿔 빠르게 확인할 수 있다.
 * - 정기권(Period)은 일 단위라 세션 중 자동 차감/퇴실하지 않는다.
 *
 * [동시성] SeatController와 동일한 seatLock으로 좌석 데이터를 보호한다.
 * UI 갱신은 ViewNavigator 구현체에서 UI 스레드로 처리하는 것이 안전하다
 * (Swing: SwingUtilities.invokeLater).
 */
public class TimeSchedulerController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Object seatLock; // SeatController와 공유하는 잠금 객체

    // ── 차감 주기/단위 (데모 시 TICK_UNIT을 SECONDS로 바꾸면 빠르게 확인 가능) ──
    private static final long TICK_PERIOD = 1;
    private static final TimeUnit TICK_UNIT = TimeUnit.HOURS;
    private static final int HOURS_PER_TICK = 1; // 한 틱마다 차감할 시간(시간권 기준)

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread t = new Thread(runnable, "time-scheduler");
                t.setDaemon(true); // 메인 종료 시 함께 종료되도록 데몬 스레드로
                return t;
            });

    public TimeSchedulerController(StudyCafeRepository repository,
                                   ViewNavigator navigator,
                                   Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.seatLock = seatLock;
    }

    /** 스케줄러 시작 (프로그램 시작 시 1회 호출) */
    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, TICK_PERIOD, TICK_PERIOD, TICK_UNIT);
    }

    /** 주기마다 실행되는 작업 */
    private void tick() {
        try {
            boolean changed = false;
            synchronized (seatLock) {
                for (Seat seat : repository.getSeatList()) {
                    if (!seat.isOccupied()) {
                        continue;
                    }

                    User user = repository.findUser(seat.getAssignedUserPhone());
                    if (user == null) {
                        continue;
                    }

                    // 시간권 사용자만 차감 (정기권은 일 단위라 세션 중 자동 차감하지 않음)
                    if ("Time".equals(user.getActiveTicketType())) {
                        user.subRemainingHours(HOURS_PER_TICK); // <=0이면 모델이 내부적으로 0/None 처리
                        changed = true;
                        if (user.getRemainingHours() <= 0) {
                            checkout(seat);
                        }
                    }
                }
                if (changed) {
                    repository.saveData();
                }
            }
            // 좌석 현황 UI 실시간 갱신
            navigator.refreshSeats(repository.getSeatList());
        } catch (Exception e) {
            // 스케줄러 스레드에서 예외가 전파되면 이후 주기가 멈추므로 반드시 잡는다
            e.printStackTrace();
        }
    }

    /** 시간이 만료된 좌석 퇴실 처리 */
    private void checkout(Seat seat) {
        int seatNumber = seat.getSeatNumber();
        seat.release(); // 좌석의 사용자 연결(전화번호)도 함께 해제됨
        navigator.showPopup(seatNumber + "번 좌석 이용 시간이 만료되어 자동 퇴실 처리되었습니다.");
    }

    /** 스케줄러 종료 (프로그램 종료 시 호출) */
    public void stop() {
        scheduler.shutdownNow();
    }
}
