package model;

public class TimeTicket extends Ticket {  private int addHours;
    private int addMinutes;

    public TimeTicket(String ticketName, int price, int addHours, int addMinutes) {
        super(ticketName, price);
        this.addHours = addHours;
        this.addMinutes = addMinutes;
    }

    public int getAddHours() {
        return addHours;
    }

    public int getAddMinutes() {
        return addMinutes;
    }
    
}
