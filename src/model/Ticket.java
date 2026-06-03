package model;

public class Ticket {
    protected String ticketName;
    protected int price;

    public Ticket(String ticketName, int price) {
        this.ticketName = ticketName;
        this.price = price;
    }

    public String getTicketName() {
        return ticketName;
    }
    
    public int getPrice() {
        return price;
    }
}


//시간권
class TimeTicket extends Ticket {
    private int addHours;
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

//기간권
class PeriodTicket extends Ticket {
    private int addDays;

    public PeriodTicket(String ticketName, int price, int addDays) {
        super(ticketName, price);
        this.addDays = addDays;
    }

    public int getAddDays() {
        return addDays;
    }
}
