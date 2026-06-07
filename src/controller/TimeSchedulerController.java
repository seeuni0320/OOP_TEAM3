package controller;

import model.Seat;
import model.StudyCafeRepository;
import model.User;
import view.ViewNavigator;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// 별도 스레드에서 60초 주기로 실행되며, 점유 좌석의 잔여 시간을 차감하고 소진 시 자동 퇴실시키는 스케줄러
public class TimeSchedulerController {

    private final StudyCafeRepository repository; // 좌석·사용자 데이터 접근
    private final ViewNavigator navigator;        // UI 갱신/팝업
    private final Object seatLock;                // Seat/Payment와 공유하는 락 객체(상호 배제용)

    private ScheduledExecutorService scheduler;   // 주기 실행을 담당하는 스레드 풀

    private static final long TICK_PERIOD = 60;             // 실행 주기 값
    private static final TimeUnit TICK_UNIT = TimeUnit.SECONDS; // 주기 단위(초)
    private static final int MINUTES_PER_TICK = 1;          // 한 주기에 차감할 분

    public TimeSchedulerController(StudyCafeRepository repository,
                                   ViewNavigator navigator,
                                   Object seatLock) {
        this.repository = repository;
        this.navigator = navigator;
        this.seatLock = seatLock;
    }

    // 스케줄러 시작
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) return; // 이미 실행 중이면 중복 시작 방지
        scheduler = Executors.newSingleThreadScheduledExecutor();  // 단일 스레드 스케줄러 생성
        scheduler.scheduleAtFixedRate(this::decrementTime,         // decrementTime을
                                      TICK_PERIOD, TICK_PERIOD, TICK_UNIT); // 60초 후부터 60초 주기로 반복 실행
        System.out.println("백그라운드 시간 차감 스케줄러가 가동.");
    }

    // 한 주기 작업: 점유 좌석별로 시간 차감 / 자동 퇴실 처리
    private void decrementTime() {
        try {                                              // 예외 시 스케줄러가 멈추지 않도록 전체를 감쌈
            List<Integer> expiredSeats = new ArrayList<>(); // 이번 주기에 자동 퇴실된 좌석번호(팝업용)
            boolean changed = false;                        // 데이터 변경 여부(저장 트리거)

            synchronized (seatLock) {                       // 공유 데이터 변경 구간 → 한 시점에 한 스레드만 진입
                for (Seat seat : repository.getSeatList()) {// 전체 좌석 순회
                    if (!seat.isOccupied()) continue;       // 비점유 좌석은 건너뜀

                    User user = repository.findUser(seat.getAssignedUserPhone()); // 좌석에 배정된 사용자 조회

                    if (user == null) {                     // 사용자 레코드 없는 좌석(데이터 불일치) → 해제
                        seat.release();
                        changed = true;
                        continue;
                    }

                    if (user.isPeriodActive()) continue;    // 정기권 유효 → 분 차감 없이 유지(정기권 우선)

                    if (user.getRemainingMinutes() > 0) {   // 정기권 없음/만료 + 시간권 잔여 있음 → 차감
                        user.subRemainingMinutes(MINUTES_PER_TICK);
                        changed = true;
                    }

                    if (!user.hasUsableBalance()) {         // 사용 가능한 잔여가 전혀 없으면 자동 퇴실
                        int seatNumber = seat.getSeatNumber();
                        seat.release();
                        expiredSeats.add(seatNumber);
                        changed = true;
                    }
                }
                if (changed) repository.saveData();         // 변경 있었으면 저장(락 구간 안에서)
            }                                               // 락 해제

            final List<Integer> expiredForUi = expiredSeats; // 람다 캡처용 final 참조
            SwingUtilities.invokeLater(() -> {              // UI 작업은 EDT에서만 → 작업 큐에 위임
                navigator.refreshSeats(repository.getSeatList());    // 좌석 화면 갱신
                for (int seatNumber : expiredForUi) {
                    navigator.showPopup(seatNumber + "번 좌석 이용 시간이 만료되어 자동 퇴실 처리되었습니다."); // 퇴실 알림
                }
            });
        } catch (Exception e) {
            e.printStackTrace();                            // 예외가 주기 실행을 중단시키지 않게 로깅만
        }
    }

    // 스케줄러 정지 (프로그램 종료 시 호출)
    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }
}