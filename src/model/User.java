package model;

public class User {

    private String phoneNumber;
    private boolean isMember;         // 회원 여부 (비회원 삭제를 위한 기준)
    private int remainingMinutes;     // 잔여 시간권 (분)
    private long periodStartTime;     // 정기권 시작 시각 (컴퓨터 밀리초 기준)
    private long periodEndTime;       // 정기권 종료 시각

    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.isMember = false;        // 기본값: 비회원
        this.remainingMinutes = 0;
        this.periodStartTime = 0;
        this.periodEndTime = 0;
    }

    // 기본 Getter & Setter
    public String getPhoneNumber() { return phoneNumber; }
    
    public synchronized boolean isMember() { return isMember; }
    public synchronized void setMember(boolean member) { isMember = member; }

    public synchronized int getRemainingMinutes() { return remainingMinutes; }
    public synchronized void setRemainingMinutes(int minutes) { this.remainingMinutes = minutes; }

    public synchronized long getPeriodStartTime() { return periodStartTime; }
    public synchronized void setPeriodStartTime(long time) { this.periodStartTime = time; }

    public synchronized long getPeriodEndTime() { return periodEndTime; }
    public synchronized void setPeriodEndTime(long time) { this.periodEndTime = time; }

   
    // 1. 정기권이 지금 유효한가? (현재 시간 < 종료 시간)
    public synchronized boolean isPeriodActive() {
        return System.currentTimeMillis() < periodEndTime;
    }

    // 2. Controller 화면 표시용: 남은 일수 계산
    public synchronized int getRemainingDays() {
        if (!isPeriodActive()) return 0;
        long diff = periodEndTime - System.currentTimeMillis();
        // 남은 밀리초를 일(Day)로 변환 후 올림 처리
        return (int) Math.ceil(diff / (24.0 * 60 * 60 * 1000));
    }

    // 3. 정기권 충전 (기간 연장)
    public synchronized void activatePeriod(int days) {
        long now = System.currentTimeMillis();
        long addedMillis = days * 24L * 60 * 60 * 1000L; // 추가할 기간을 밀리초로 환산

        if (isPeriodActive()) {
            this.periodEndTime += addedMillis; // 기존에 쓰던 중이면 만료일만 늘림
        } else {
            this.periodStartTime = now;
            this.periodEndTime = now + addedMillis; // 처음 사거나 만료됐으면 지금부터 시작
        }
    }

    // 4. 시간권 충전 및 차감
    public synchronized void addRemainingMinutes(int minutes) {
        this.remainingMinutes += minutes;
    }

    public synchronized void subRemainingMinutes(int minutes) {
        this.remainingMinutes -= minutes;
        if (this.remainingMinutes < 0) {
            this.remainingMinutes = 0;
        }
    }

    // 5. 사용 가능한 잔여(정기권 or 시간권)가 있는지 확인
    public synchronized boolean hasUsableBalance() {
        return isPeriodActive() || remainingMinutes > 0;
    }
}