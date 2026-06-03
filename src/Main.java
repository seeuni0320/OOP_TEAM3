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
 * (SeatController → PaymentController → TicketController → LoginController 순으로
 *  뒤 컨트롤러가 앞 컨트롤러를 필요로 한다.)
 *
 * 이미 프로젝트에 Main/진입점이 있으면 이 파일의 "조립 순서"만 참고하세요.
 */
public class Main {
    public static void main(String[] args) {
        StudyCafeRepository repository = new StudyCafeRepository();
        SwingNavigator navigator = new SwingNavigator();
        Session session = new Session();
        Object seatLock = new Object(); // SeatController와 TimeScheduler가 공유

        // 의존성 순서대로 조립
        SeatController seatController =
                new SeatController(repository, navigator, session, seatLock);
        PaymentController paymentController =
                new PaymentController(repository, navigator, session, seatController);
        TicketController ticketController =
                new TicketController(navigator, session, seatController, paymentController);
        LoginController loginController =
                new LoginController(repository, navigator, session, ticketController);

        // 네비게이터에 컨트롤러 주입
        navigator.setControllers(loginController, ticketController, paymentController, seatController);

        // 좌석 시간 차감 스케줄러 시작
        TimeSchedulerController scheduler =
                new TimeSchedulerController(repository, navigator, seatLock);
        scheduler.start();

        // 프로그램 종료 시 데이터 저장 + 스케줄러 정리
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.stop();
            repository.saveData();
        }));

        // Swing은 UI 스레드에서 화면을 띄운다
        SwingUtilities.invokeLater(navigator::showMainMenu);
    }
}
