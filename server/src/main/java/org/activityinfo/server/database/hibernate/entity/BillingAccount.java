package org.activityinfo.server.database.hibernate.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "billingaccount")
public class BillingAccount {


    private int id;

    private String name;

    private String code;

    private int userLimit;

    private Date startTime;
    private Date endTime;

    private Date expectedPaymentDate;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getExpectedPaymentDate() {
        return expectedPaymentDate;
    }

    public void setExpectedPaymentDate(Date expectedPaymentDate) {
        this.expectedPaymentDate = expectedPaymentDate;
    }
}
