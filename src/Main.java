import controller.LoginController;
import controller.PaymentController;
import controller.SeatController;
import controller.Session;
import controller.TicketController;
import controller.TimeSchedulerController;
import model.StudyCafeRepository;
import view.SwingNavigator;

import javax.swing.SwingUtilities;

/**
 * Main
 * 컨트롤러 간 의존 관계 때문에 생성 순서가 중요하다.
 * (SeatController → PaymentController → TicketController → LoginController 순)
 *
 * [수정됨]
 * - PaymentController가 seatLock을 받도록 변경(결제 시 저장을 락으로 보호).
 * - 종료 훅의 saveData()도 seatLock 안에서 호출(스케줄러 잔여 작업과의 충돌 방지).
 * - navigator.setRepository(repository) 호출 추가
 *   (SeatSelectionView가 좌석별 남은 시간을 표시하려면 navigator가 repository를 알아야 한다.
 *    SwingNavigator에 public void setRepository(StudyCafeRepository) 를 추가해 두어야 한다.)
 */
public class Main {
    public static void main(String[] args) {
        StudyCafeRepository repository = new StudyCafeRepository();
        SwingNavigator navigator = new SwingNavigator();
        Session session = new Session();
        Object seatLock = new Object(); // SeatController / PaymentController / TimeScheduler가 공유

        // 의존성 순서대로 조립
        SeatController seatController =
                new SeatController(repository, navigator, session, seatLock);
        PaymentController paymentController =
                new PaymentController(repository, navigator, session, seatController, seatLock);
        TicketController ticketController =
                new TicketController(navigator, session, seatController, paymentController);
        LoginController loginController =
                new LoginController(repository, navigator, session, ticketController);

        // 네비게이터에 컨트롤러 + 레포지토리 주입
        navigator.setControllers(loginController, ticketController, paymentController, seatController);
        navigator.setRepository(repository); // ← 좌석 남은시간 표시에 필요

        // 좌석 시간 차감 스케줄러 시작
        TimeSchedulerController scheduler =
                new TimeSchedulerController(repository, navigator, seatLock);
        scheduler.start();

        // 프로그램 종료 시 데이터 저장 + 스케줄러 정리
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.stop();
            synchronized (seatLock) {
                repository.saveData();
            }
        }));

        // Swing은 UI 스레드에서 화면을 띄운다
        SwingUtilities.invokeLater(navigator::showMainMenu);
    }
}