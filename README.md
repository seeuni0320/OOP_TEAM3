# OOP_TEAM3
##스터디카페 무인 키오스크 구현
###Flowchart
<img width="396" height="553" alt="카페키오스크_흐름도 " src="https://github.com/user-attachments/assets/401451da-d650-427d-b03f-328db0c0aaa5" />

###전체 Framework
Model(박세은)
  Seat: 좌석 번호, 현재 이용 여부(true/false), 남은 시간(분) 저장 및 차감 기능 제공.
  User: 회원 아이디, 이름, 현재 이용 중인 좌석 번호 관리.
  Ticket: 시간권 종류(2시간, 4시간, 정기권 등) 및 가격 정보 저장.
  StudyCafeRepository: 좌석 현황과 회원 데이터를 파일(seats.txt, users.txt)에서 읽어오고 프로그램 종료 시 저장하는 역할 (파일 I/O 담당).

View(이후림)
  MainMenuView : 첫페이지. 로그인 버튼 배치 
  LoginView : 회원 비회원 전화번호  입력 화면
  SeatSelectionView: 스터디카페의 전체 좌석 배치도 화면. .
  TicketSelectionView: 스터디카페의 이용권을 선택하고 결제 요청을 보내는 화면.
  PaymentView: 가상근액을 보여주고 결제하는 화면.

Controller(이세은)
  LoginController: 회원이면 전화번호 입력 후 회원 조회. 비회원이면 전화번호 입력후 이용권 구매로 이동.
  TicketController: 회원이 사용 가능한 이용권 보유 시 결제 생략 후 좌석 선택으로. 없거나 비회원이면 이용권 구매창 표시
  SeatController: 사용자가 좌석을 클릭했을 때, 빈 좌석인지 검증하고 이용 팝업을 띄우거나 이동 요청을 처리.
  PaymentController: 결제 버튼 클릭 시 금액을 검증하고 StudyCafeRepository를 통해 매출 데이터를 갱신.
  TimeSchedulerController: 멀티스레드로 구동. 1분마다 이용 중인 모든 좌석의 남은 시간을 실시간으로 차감, 시간이 만료되면 퇴실 처리를 실행

