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
 * - 멀티스레드로 구동한다.
 * - 주기마다 이용 중인 좌석의 사용자(User) 잔여 시간을 차감하고,
 *   시간이 만료되면 좌석을 자동 퇴실 처리한다.
 *
 * [모델 적응 메모]
 * - 모델은 남은 시간을 좌석이 아니라 회원(User.remainingHours)에 저장한다.
 *   좌석의 assignedUserPhone으로 사용자를 찾아 시간을 차감한다.
 * - 모델 단위가 '시간'이므로 한 틱마다 1시간 차감한다.
 *   데모 시 TICK_UNIT을 SECONDS로 바꾸면 빠르게 확인할 수 있다.
 *
 * [모델 변경에 맞춘 수정]
 * - 모델이 시간권+정기권을 동시에 가진 회원의 타입을 "Both"로 표시하게 바뀌었다.
 *   따라서 타입 문자열("Time")로 판단하지 않고 "남은 시간(remainingHours)이 있으면 차감"한다.
 *   (정기권만 있는 회원은 remainingHours==0 이라 자동으로 차감 대상에서 빠진다.)
 * - 자동 퇴실은 "시간도 0이고 정기권 일수도 0일 때"만 한다.
 *   (시간권이 끝나도 정기권 일수가 남았으면 좌석을 비우지 않는다.)
 *
 * [동시성] 좌석 데이터 변경만 seatLock 안에서 처리하고, UI 호출은 락 밖에서 한다(교착 방지).
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
            // 만료되어 자동 퇴실된 좌석 번호를 모았다가, 락을 빠져나온 뒤 알림을 띄운다.
            List<Integer> expiredSeats = new ArrayList<>();

            synchronized (seatLock) {
                for (Seat seat : repository.getSeatList()) {
                    if (!seat.isOccupied()) {
                        continue;
                    }

                    User user = repository.findUser(seat.getAssignedUserPhone());
                    if (user == null) {
                        continue;
                    }

                    // 남은 '시간'이 있는 회원만 차감 (시간권 단독이든 "Both"이든 모두 포함).
                    // 정기권만 있는 회원은 remainingHours==0 이라 여기서 자연히 제외된다.
                    if (user.getRemainingHours() > 0) {
                        user.subRemainingHours(HOURS_PER_TICK);
                        changed = true;

                        // 시간도 0이고 정기권 일수도 0이면(= 더 이용할 권한이 없으면) 자동 퇴실.
                        if (user.getRemainingHours() <= 0 && user.getRemainingDays() <= 0) {
                            int seatNumber = seat.getSeatNumber();
                            seat.release(); // 좌석의 사용자 연결(전화번호)도 함께 해제됨
                            expiredSeats.add(seatNumber);
                        }
                    }
                }
                if (changed) {
                    repository.saveData();
                }
            }

            // ── 여기서부터는 락 바깥. UI만 호출한다(교착 방지). ──
            navigator.refreshSeats(repository.getSeatList());
            for (int seatNumber : expiredSeats) {
                navigator.showPopup(seatNumber + "번 좌석 이용 시간이 만료되어 자동 퇴실 처리되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 스케줄러 종료 (프로그램 종료 시 호출) */
    public void stop() {
        scheduler.shutdownNow();
    }
}