package model;
public class User {

    private String phoneNumber;
    private boolean hasActiveTicket;
    private String activeTicketType;
    private int remainingHours;
    private int remainingDays;


    //신규회원 생성
    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.hasActiveTicket = false;
        this.activeTicketType = "None";
        this.remainingHours = 0;
        this.remainingDays = 0;
    }


    //Getter Setter
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isHasActiveTicket() {
        return hasActiveTicket;
    }

    public void setHasActiveTicket(boolean hasActiveTicket) {
        this.hasActiveTicket = hasActiveTicket;
    }
    
    public String getActiveTicketType() {
        return activeTicketType;
    }

    public void setActiveTicketType(String activeTicketType) {
        this.activeTicketType = activeTicketType;
    }

    //시간권
    public synchronized int getRemainingHours() {
        return remainingHours;
    }

    //시간 추가
    public synchronized void addRemainingHours(int hours) {
        this.remainingHours += hours;
        this.hasActiveTicket = true;
        this.activeTicketType = "Time";

    }

    //멀티스레드 시간차감
    public synchronized void subRemainingHours(int hours) {
        this.remainingHours -= hours;
        if (this.remainingHours <= 0) {
            this.remainingHours = 0;
            this.hasActiveTicket = false;
            this.activeTicketType = "None";
        }
    }

    //기간권
    public synchronized int getRemainingDays() {
        return remainingDays;
    }
   //기간권추가
    public synchronized void addRemaningDays(int days) {
        this.remainingDays += days;
        this.hasActiveTicket = true;
        this.activeTicketType = "Period";

    }
    //기간차감
    public synchronized void subRemainingDays(int days) {
        this.remainingDays -= days;
        if (this.remainingDays <= 0) {
            this.remainingDays = 0;
            this.hasActiveTicket = false;
            this.activeTicketType = "None";
        }
    }

}
