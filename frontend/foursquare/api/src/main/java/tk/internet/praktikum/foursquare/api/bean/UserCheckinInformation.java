package tk.internet.praktikum.foursquare.api.bean;

import java.util.Date;

public class UserCheckinInformation {
    private String user;
    private int count;
    private Date last;

    public String getUserID() {
        return user;
    }

    public int getCount() {
        return count;
    }

    public Date getLastDate() {
        return last;
    }
}
