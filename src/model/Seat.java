package model;

public class Seat {

    private int seatNumber;
    private boolean isOccupied;
    private String assignedUserPhone;

    //좌석 초기화
    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.isOccupied = false;
        this.assignedUserPhone = null;
    }

    //getter & setter
    public int getSeatNumber() {
        return seatNumber;
    }
    
    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean isOccupied) {
        this.isOccupied = isOccupied;
    }

    public String getAssignedUserPhone() {
        return assignedUserPhone;
    }
    public void setAssignedUserPhone(String assignedUserPhone) {
        this.assignedUserPhone = assignedUserPhone;
    }
    
    //사용자가 이 좌석을 선택해서 입실할 때 호출하는 메서드
    public void occupy(String phoneNumber) {
        this.isOccupied = true;
        this.assignedUserPhone = phoneNumber;
    }

    //사용자가 퇴실하거나 이용시간이 만료되어 좌석을 비울 때 호출하는 메서드
    public void release() {
        this.isOccupied = false;
        this.assignedUserPhone = null;
    }
}
