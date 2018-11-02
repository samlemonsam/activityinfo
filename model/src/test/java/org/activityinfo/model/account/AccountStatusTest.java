package org.activityinfo.model.account;

import org.activityinfo.model.type.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class AccountStatusTest {


    @Test
    public void testTrialAccountExpiring() {
        AccountStatus status = new AccountStatus.Builder()
                .setDatabaseCount(215)
                .setExpirationTime(new LocalDate(2018, 10, 14))
                .setTrial(true)
                .setLegacy(true)
                .build();


        Date now = new LocalDate(2018, 9, 10).atMidnightInMyTimezone();

        assertTrue(status.shouldWarn(now));

        // Today is monday (no more warning)
        // Tomorrow is tuesday 11th (+1)
        //      ... is wednesday 12th (+2)
        //      ... is thursday 13th( +3)
        //      ... is friady 14th... time for another warning

        assertThat(status.snoozeDate(now), equalTo(new LocalDate(2018, 9, 14)));
    }


    @Test
    public void newTrialAccountExpiring() {


        AccountStatus status = new AccountStatus.Builder()
                .setDatabaseCount(215)
                .setExpirationTime(new LocalDate(2018, 12, 31))
                .setTrial(true)
                .setLegacy(false)
                .build();

        // No warning for first two weeks
        assertFalse(status.shouldWarn(new LocalDate(2018, 12, 1).atMidnightInMyTimezone()));
        assertFalse(status.shouldWarn(new LocalDate(2018, 12, 2).atMidnightInMyTimezone()));
        assertFalse(status.shouldWarn(new LocalDate(2018, 12, 14).atMidnightInMyTimezone()));

        // With two weeks to go, issue the first warning
        assertTrue(status.shouldWarn(new LocalDate(2018, 12, 17).atMidnightInMyTimezone()));
        assertTrue(status.shouldWarn(new LocalDate(2018, 12, 20).atMidnightInMyTimezone()));
        assertTrue(status.shouldWarn(new LocalDate(2018, 12, 30).atMidnightInMyTimezone()));
    }

    @Test
    public void paymentDue() {

        AccountStatus status = new AccountStatus.Builder()
                .setDatabaseCount(215)
                .setExpectedPaymentDate(new LocalDate(2018, 12, 31))
                .setTrial(false)
                .setLegacy(false)
                .build();

        // No warning for first two weeks
        assertFalse(status.shouldNudgeForPayment(new LocalDate(2018, 12, 1).atMidnightInMyTimezone()));
        assertFalse(status.shouldNudgeForPayment(new LocalDate(2018, 12, 2).atMidnightInMyTimezone()));
        assertFalse(status.shouldNudgeForPayment(new LocalDate(2018, 12, 14).atMidnightInMyTimezone()));

        // With two weeks to go, issue the first warning
        assertTrue(status.shouldNudgeForPayment(new LocalDate(2018, 12, 17).atMidnightInMyTimezone()));
        assertTrue(status.shouldNudgeForPayment(new LocalDate(2018, 12, 20).atMidnightInMyTimezone()));
        assertTrue(status.shouldNudgeForPayment(new LocalDate(2018, 12, 30).atMidnightInMyTimezone()));
    }

    @Test
    public void testPaymentOverdue() {
        AccountStatus status = new AccountStatus.Builder()
                .setDatabaseCount(215)
                .setExpirationTime(new LocalDate(2018, 11, 16))
                .setExpectedPaymentDate(new LocalDate(2018, 11, 16))
                .setTrial(false)
                .setLegacy(false)
                .build();

        Date now = new LocalDate(2018, 11, 2).atMidnightInMyTimezone();
        assertTrue(status.shouldNudgeForPayment(now));
        // Today is friday with 2 weeks till payment (no more warning)
        // Should be snoozed until 1 week before
        assertThat(status.paymentSnoozeDate(now), equalTo(new LocalDate(2018, 11, 9)));

        now = new LocalDate(2018, 11, 9).atMidnightInMyTimezone();
        assertTrue(status.shouldNudgeForPayment(now));
        // Today is friday with 1 week till payment (no more warning)
        // Should be snoozed until 3 days before
        assertThat(status.paymentSnoozeDate(now), equalTo(new LocalDate(2018, 11, 13)));

        now = new LocalDate(2018, 11, 14).atMidnightInMyTimezone();
        assertTrue(status.shouldNudgeForPayment(now));
        // Today is wednesday with 2 days till payment
        // Should be snoozed until tomorrow
        assertThat(status.paymentSnoozeDate(now), equalTo(new LocalDate(2018, 11, 15)));
    }

    @Test
    public void expireString() {

        AccountStatus status = new AccountStatus.Builder()
                .setExpirationTime(new LocalDate(2019, 1, 16))
                .build();


        assertThat(expiringIn(status, 2018, 11, 7), equalTo("expiring in 10 weeks"));
        assertThat(expiringIn(status, 2018, 11, 8), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 9), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 10), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 11), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 12), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 13), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 14), equalTo("expiring in 9 weeks"));
        assertThat(expiringIn(status, 2018, 11, 15), equalTo("expiring in 8 weeks"));
        assertThat(expiringIn(status, 2018, 11, 16), equalTo("expiring in 8 weeks"));
        assertThat(expiringIn(status, 2018, 11, 17), equalTo("expiring in 8 weeks"));
        assertThat(expiringIn(status, 2018, 11, 18), equalTo("expiring in 8 weeks"));
        assertThat(expiringIn(status, 2018, 11, 19), equalTo("expiring in 8 weeks"));
        assertThat(expiringIn(status, 2018, 11, 20), equalTo("expiring in 8 weeks"));
        assertThat(expiringIn(status, 2018, 11, 21), equalTo("expiring in 8 weeks"));

        assertThat(expiringIn(status, 2018, 12, 26), equalTo("expiring in 3 weeks"));
        assertThat(expiringIn(status, 2018, 12, 27), equalTo("expiring in 20 days"));
        assertThat(expiringIn(status, 2018, 12, 28), equalTo("expiring in 19 days"));
        assertThat(expiringIn(status, 2018, 12, 29), equalTo("expiring in 18 days"));
        assertThat(expiringIn(status, 2018, 12, 30), equalTo("expiring in 17 days"));
        assertThat(expiringIn(status, 2018, 12, 31), equalTo("expiring in 16 days"));
        assertThat(expiringIn(status, 2019, 1, 1), equalTo("expiring in 15 days"));
        assertThat(expiringIn(status, 2019, 1, 2), equalTo("expiring in 14 days"));
        assertThat(expiringIn(status, 2019, 1, 3), equalTo("expiring in 13 days"));
        assertThat(expiringIn(status, 2019, 1, 4), equalTo("expiring in 12 days"));
    }

    @Test
    public void paymentExpectedString() {
        AccountStatus status = new AccountStatus.Builder()
                .setExpectedPaymentDate(new LocalDate(2018, 1, 31))
                .build();

        assertThat(paymentExpectedIn(status, 2018, 1, 10), equalTo("due in 3 week(s)"));
        assertThat(paymentExpectedIn(status, 2018, 1, 17), equalTo("due in 2 week(s)"));
        assertThat(paymentExpectedIn(status, 2018, 1, 24), equalTo("due in 1 week(s)"));
        assertThat(paymentExpectedIn(status, 2018, 1, 25), equalTo("due in 6 days"));
        assertThat(paymentExpectedIn(status, 2018, 1, 26), equalTo("due in 5 days"));
        assertThat(paymentExpectedIn(status, 2018, 1, 27), equalTo("due in 4 days"));
        assertThat(paymentExpectedIn(status, 2018, 1, 28), equalTo("due in 3 days"));
        assertThat(paymentExpectedIn(status, 2018, 1, 29), equalTo("due in 48 hours"));
        assertThat(paymentExpectedIn(status, 2018, 1, 30), equalTo("due in 24 hours"));
        assertThat(paymentExpectedIn(status, 2018, 1, 31), equalTo("overdue"));
        assertThat(paymentExpectedIn(status, 2018, 2, 1), equalTo("overdue"));
    }

    private String expiringIn(AccountStatus status, int year, int month, int day) {
        return status.expiringIn(new LocalDate(year, month, day).atMidnightInMyTimezone());
    }

    private String paymentExpectedIn(AccountStatus status, int year, int month, int day) {
        return status.paymentExpectedIn(new LocalDate(year, month, day).atMidnightInMyTimezone());
    }

    @Test
    public void integerOverflow() {
        AccountStatus status = new AccountStatus.Builder()
                .setExpirationTime(new LocalDate(2999, 1, 1))
                .build();

        assertTrue(status.getExpirationTime() > 0);
        assertFalse(status.isExpired());
    }
}