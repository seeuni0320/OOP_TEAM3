package controller;

import model.Ticket;
import model.User;

/**
 * Session
 * 키오스크에서 진행 중인 한 손님의 이용 흐름(로그인 ~ 좌석 배정)을 담는 상태 보관소.
 * 컨트롤러들이 공유하며, 한 손님의 이용이 끝나면 clear()로 초기화한다.
 *
 * [참고] 기존 컨트롤러들이 사용하던 Session 파일이 프로젝트에 없어서 새로 만든 클래스입니다.
 *        controller 패키지에 함께 두세요.
 */
public class Session {

    private User user;             // 현재 로그인(또는 비회원) 사용자
    private boolean guest;         // 비회원 여부
    private Ticket selectedTicket; // 방금 구매했거나 선택한 이용권

    public void clear() {
        this.user = null;
        this.guest = false;
        this.selectedTicket = null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public Ticket getSelectedTicket() {
        return selectedTicket;
    }

    public void setSelectedTicket(Ticket selectedTicket) {
        this.selectedTicket = selectedTicket;
    }
}
