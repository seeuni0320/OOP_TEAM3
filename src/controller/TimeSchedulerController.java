package studycafe.controller;

import studycafe.model.Seat;
import studycafe.model.StudyCafeRepository;
import studycafe.model.User;
import studycafe.view.ViewNavigator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TimeSchedulerController
 * - 멀티스레드로 구동한다.
 * - 1분마다 이용 중인 모든 좌석의 남은 시간을 실시간으로 차감하고,
 *   시간이 만료되면(0분 이하) 퇴실 처리를 실행한다.
 *
 * [동시성 주의]
 * SeatController와 동일한 seatLock으로 좌석 데이터를 보호한다.
 * UI 갱신(refreshSeats / showPopup)은 GUI 프레임워크 규칙에 따라
 * UI 스레드에서 실행되도록 ViewNavigator 구현체에서 처리해야 한다.
 * (Swing: SwingUtilities.invokeLater, JavaFX: Platform.runLater)
 */
public class TimeSchedulerController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Object seatLock; // SeatController와 공유하는 잠금 객체

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
        // 1분 뒤 시작, 이후 1분 간격 반복
        scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.MINUTES);
    }

    /** 1분마다 실행되는 작업 */
    private void tick() {
        try {
            boolean changed = false;
            synchronized (seatLock) {
                for (Seat seat : repository.getSeats()) {
                    if (!seat.isInUse()) {
                        continue;
                    }
                    seat.deductTime(1);
                    changed = true;
                    if (seat.getRemainingMinutes() <= 0) {
                        checkout(seat);
                    }
                }
                if (changed) {
                    repository.saveSeats();
                    repository.saveUsers();
                }
            }
            // 좌석 현황 UI 실시간 갱신
            navigator.refreshSeats(repository.getSeats());
        } catch (Exception e) {
            // 스케줄러 스레드에서 예외가 전파되면 이후 주기가 멈추므로 반드시 잡는다
            e.printStackTrace();
        }
    }

    /** 시간이 만료된 좌석 퇴실 처리 */
    private void checkout(Seat seat) {
        User user = repository.findUserById(seat.getUserId());
        if (user != null) {
            user.setCurrentSeatNumber(-1);
        }
        int seatNumber = seat.getSeatNumber();
        seat.release();
        navigator.showPopup(seatNumber + "번 좌석 이용 시간이 만료되어 자동 퇴실 처리되었습니다.");
    }

    /** 스케줄러 종료 (프로그램 종료 시 호출) */
    public void stop() {
        scheduler.shutdownNow();
    }
}
