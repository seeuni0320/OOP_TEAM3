package model;

public class PeriodTicket extends Ticket {  private int addDays;

    public PeriodTicket(String ticketName, int price, int addDays) {
        super(ticketName, price);
        this.addDays = addDays;
    }

    public int getAddDays() {
        return addDays;
    }
    
}
