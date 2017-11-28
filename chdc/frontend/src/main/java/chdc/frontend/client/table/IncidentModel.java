package chdc.frontend.client.table;

import com.google.gwt.core.client.GWT;

import java.util.Date;

public class IncidentModel {

    public static final IncidentProperties PROPERTIES = GWT.create(IncidentProperties.class);

    private String id;
    private String narrative;
    private Date date;
    private String time;
    private String perpretrator;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPerpretrator() {
        return perpretrator;
    }

    public void setPerpretrator(String perpretrator) {
        this.perpretrator = perpretrator;
    }
}
