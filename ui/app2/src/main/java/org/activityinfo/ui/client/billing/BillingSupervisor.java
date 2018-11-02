package org.activityinfo.ui.client.billing;

import com.google.common.base.Function;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.RootPanel;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.model.account.AccountStatus;
import org.activityinfo.model.type.time.LocalDate;

import javax.annotation.Nullable;
import java.util.Date;

public class BillingSupervisor {

    public static final String BILLING_SNOOZE_KEY_PREFIX = "accountSnooze";
    public static final String PAYMENT_SNOOZE_KEY_PREFIX = "paymentSnooze";

    private final ActivityInfoClientAsync client = new ActivityInfoClientAsyncImpl();


    public void run() {
        client.getAccountStatus().then(new Function<AccountStatus, Void>() {
            @Nullable
            @Override
            public Void apply(AccountStatus accountStatus) {
                maybeShowStatus(accountStatus);
                return null;
            }
        });
    }

    private void maybeShowStatus(AccountStatus status) {
        Date now = new Date();
        if(status.shouldWarn(now) && !isSnoozed(status, BILLING_SNOOZE_KEY_PREFIX)) {
            BillingWarning warning = new BillingWarning(status);
            RootPanel.get().add(warning);
        }
        if(status.shouldNudgeForPayment(now) && !isSnoozed(status, PAYMENT_SNOOZE_KEY_PREFIX)) {
            PaymentWarning warning = new PaymentWarning(status);
            RootPanel.get().add(warning);
        }
    }

    /**
     * Snooze account warnings until the given {@code snoozeDate}. Billing warnings
     * will appear again starting *on* this date.
     * @param snoozeDate
     */
    static void snooze(AccountStatus status, String snoozePrefix) {
        LocalDate snoozeDate;
        if (PAYMENT_SNOOZE_KEY_PREFIX.equals(snoozePrefix)) {
            snoozeDate = status.paymentSnoozeDate(new Date());
        } else {
            snoozeDate = status.snoozeDate(new Date());
        }
        Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            storage.setItem(snoozePrefix + status.getUserAccountId(), snoozeDate.toString());
        }
    }

    static boolean isSnoozed(AccountStatus status, String snoozePrefix) {
        try {
            Storage storage = Storage.getLocalStorageIfSupported();
            if(storage == null) {
                return false;
            }
            String accountSnooze = storage.getItem(snoozePrefix + status.getUserAccountId());
            if(accountSnooze == null) {
                return false;
            }
            LocalDate snoozeDate = LocalDate.parse(accountSnooze);
            LocalDate today = new LocalDate();
            if(today.before(snoozeDate)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
