import controller.LoginController;
import controller.PaymentController;
import controller.SeatController;
import controller.Session;
import controller.TicketController;
import controller.TimeSchedulerController;
import model.StudyCafeRepository;
import view.SwingNavigator;
// 각 패키지 호출
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
        //객체 생성
        StudyCafeRepository repository = new StudyCafeRepository(); //회원 정보, 좌석 정보, 이용권 정보 등을 관리하는 저장소
        SwingNavigator navigator = new SwingNavigator(); // 화면 전환 View를 연결하고 화면 이동을 관리하는 객체
        Session session = new Session(); // 현재 이용 중인 사용자의 상태를 임시로 저장하는 객체
        Object seatLock = new Object(); // SeatController / PaymentController / TimeScheduler가 공유

        // 의존성 순서대로 컨트롤러 생성, 조립
        SeatController seatController =//좌석 데이터 확인/수정, 좌석 화면 이동, 현재 사용자 정보 확인, 좌석 데이터 동시 접근 방지
                new SeatController(repository, navigator, session, seatLock);
        PaymentController paymentController =// 결제 후 좌석 선택으로 넘어가야 하므로 seatController가 필요
                new PaymentController(repository, navigator, session, seatController, seatLock);
        TicketController ticketController =// 사용자가 이용권을 고르면 결제로 넘어가야 하므로 paymentController가 필요
                new TicketController(navigator, session, seatController);//이미 이용권이 있으면 바로 좌석 선택으로 갈 수도 있으므로 seatController도 필요
        LoginController loginController =// 로그인/등록 후 이용권 단계(보유 이용권 확인 또는 구매)로 넘기기 위해 ticketController가 필요
                                                        // 퇴실 처리를 SeatController에 맡기기 위해 seatController가 필요
        new LoginController(repository, navigator, session, ticketController, seatController);

        // 네비게이터에 컨트롤러 + 레포지토리 주입
        // SwingNavigator에게 필요한 객체들을 넘겨줌
        navigator.setControllers(loginController, ticketController, paymentController, seatController);
        navigator.setRepository(repository); // ← 좌석 남은시간 표시에 필요

        // 좌석 시간 차감 스케줄러 시작
        // 이 스케줄러도 좌석 정보와 이용권 시간을 건드리기 때문에 repository와 seatLock을 받는다
        TimeSchedulerController scheduler =
                new TimeSchedulerController(repository, navigator, seatLock);
        scheduler.start();

        // 프로그램 종료 시 데이터 저장 + 스케줄러 정리
        // 좌석 정보나 이용권 정보가 수정되는 중에 저장하면 꼬일 수 있기 때문에 seatLock으로 잠근 뒤 저장
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