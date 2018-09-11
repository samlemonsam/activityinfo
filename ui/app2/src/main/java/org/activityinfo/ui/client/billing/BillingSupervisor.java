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

    public static final String SNOOZE_KEY_PREFIX = "accountSnooze";

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
        if(status.shouldWarn(now) && !isSnoozed(status)) {
            BillingWarning warning = new BillingWarning(status);
            RootPanel.get().add(warning);
        }
    }

    /**
     * Snooze account warnings until the given {@code snoozeDate}. Billing warnings
     * will appear again starting *on* this date.
     * @param snoozeDate
     */
    static void snooze(AccountStatus status) {
        LocalDate snoozeDate = status.snoozeDate(new Date());
        Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            storage.setItem(SNOOZE_KEY_PREFIX + status.getUserAccountId(), snoozeDate.toString());
        }
    }

    static boolean isSnoozed(AccountStatus status) {
        try {
            Storage storage = Storage.getLocalStorageIfSupported();
            if(storage == null) {
                return false;
            }
            String accountSnooze = storage.getItem(SNOOZE_KEY_PREFIX + status.getUserAccountId());
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
