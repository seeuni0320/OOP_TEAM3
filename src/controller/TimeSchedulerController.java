package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TimeSchedulerController
 * 소비 순서(명확화):
 *   1) 정기권 유효기간(isPeriodActive) 내라면 → 분 차감 없이 좌석 유지.
 *   2) 정기권이 없거나 만료됐다면 → 시간권(분)을 차감.
 *   3) 정기권도 만료되고 분도 0이면 → 자동 퇴실.
 * 즉 "정기권 우선, 그 다음 시간권" 으로 일관되게 소비한다.
 */
public class TimeSchedulerController {

    private final StudyCafeRepository repository;
    private final ViewNavigator navigator;
    private final Object seatLock;

    private ScheduledExecutorService scheduler;

    private static final long TICK_PERIOD = 1;
    private static final TimeUnit TICK_UNIT = TimeUnit.SECONDS;
    private static final int MINUTES_PER_TICK = 1;

    public TimeSchedulerController(StudyCafeRepository repository,
                                   ViewNavigator navigator,
                                   Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.seatLock = seatLock;
    }

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::decrementTime, TICK_PERIOD, TICK_PERIOD, TICK_UNIT);
        System.out.println("🚨 백그라운드 시간 차감 스케줄러가 가동되었습니다.");
    }

    private void decrementTime() {
        try {
            List<Integer> expiredSeats = new ArrayList<>();
            boolean changed = false;

            synchronized (seatLock) {
                for (Seat seat : repository.getSeatList()) {
                    if (!seat.isOccupied()) continue;

                    User user = repository.findUser(seat.getAssignedUserPhone());
                    if (user == null) continue;

                    // 1) 정기권 유효기간 내면 분 차감/만료 없이 유지
                    if (user.isPeriodActive()) {
                        continue;
                    }

                    // 2) 정기권 없음/만료 → 시간권(분) 차감
                    if (user.getRemainingMinutes() > 0) {
                        user.subRemainingMinutes(MINUTES_PER_TICK);
                        changed = true;
                    }

                    // 3) 사용 가능한 잔여가 전혀 없으면 자동 퇴실
                    if (!user.hasUsableBalance()) {
                        int seatNumber = seat.getSeatNumber();
                        seat.release();
                        expiredSeats.add(seatNumber);
                        changed = true;
                    }
                }
                if (changed) repository.saveData();
            }

            navigator.refreshSeats(repository.getSeatList());
            for (int seatNumber : expiredSeats) {
                navigator.showPopup(seatNumber + "번 좌석 이용 시간이 만료되어 자동 퇴실 처리되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }
}
