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
