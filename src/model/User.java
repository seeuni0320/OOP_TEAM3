package model;

public class User {

    private String phoneNumber;
    private boolean hasActiveTicket;
    private String activeTicketType;
    private int remainingMinutes;
    private int remainingDays;

    // 신규회원 생성
    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.hasActiveTicket = false;
        this.activeTicketType = "None";
        this.remainingMinutes = 0;
        this.remainingDays = 0;
    }

    // Getter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isHasActiveTicket() {
        return hasActiveTicket;
    }

    public String getActiveTicketType() {
        return activeTicketType;
    }

    public synchronized int getRemainingMinutes() {
        return remainingMinutes;
    }

    public synchronized int getRemainingDays() {
        return remainingDays;
    }

    // 기존 Setter
    public void setHasActiveTicket(boolean hasActiveTicket) {
        this.hasActiveTicket = hasActiveTicket;
    }

    public void setActiveTicketType(String activeTicketType) {
        this.activeTicketType = activeTicketType;
    }

    // [수정 1] Repository 로드용 부수효과 없는 순수 Setter 추가
    public synchronized void setRemainingMinutes(int remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public synchronized void setRemainingDays(int remainingDays) {
        this.remainingDays = remainingDays;
    }

    // [수정 2] 티켓 상태를 잔여 시간에 따라 자동으로 계산해 주는 메서드
    private void updateTicketState() {
        if (this.remainingMinutes > 0 && this.remainingDays > 0) {
            this.activeTicketType = "Both";
            this.hasActiveTicket = true;
        } else if (this.remainingMinutes > 0) {
            this.activeTicketType = "Time";
            this.hasActiveTicket = true;
        } else if (this.remainingDays > 0) {
            this.activeTicketType = "Period";
            this.hasActiveTicket = true;
        } else {
            this.activeTicketType = "None";
            this.hasActiveTicket = false;
        }
    }

    // [수정 3] add 및 sub 메서드에서 덮어쓰기 대신 updateTicketState() 호출
    // 시간권
    public synchronized void addRemainingMinutes(int minutes) {
        this.remainingMinutes += minutes;
        updateTicketState();
    }

    public synchronized void subRemainingMinutes(int minutes) {
        this.remainingMinutes -= minutes;
        if (this.remainingMinutes <= 0) {
            this.remainingMinutes = 0;
        }
        updateTicketState(); // 시간이 0이 되어도 기간권이 남아있으면 안 꺼짐
    }

    // 기간권
    public synchronized void addRemainingDays(int days) {
        this.remainingDays += days;
        updateTicketState();
    }

    public synchronized void subRemainingDays(int days) {
        this.remainingDays -= days;
        if (this.remainingDays <= 0) {
            this.remainingDays = 0;
        }
        updateTicketState(); // 일수가 0이 되어도 시간권이 남아있으면 안 꺼짐
    }
}