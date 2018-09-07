package org.activityinfo.ui.client.billing;

import com.google.common.base.Function;
import com.google.gwt.user.client.ui.RootPanel;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.api.client.ActivityInfoClientAsyncImpl;
import org.activityinfo.model.account.AccountStatus;

import javax.annotation.Nullable;

public class BillingSupervisor {

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


    private void maybeShowStatus(AccountStatus accountStatus) {
        if(!accountStatus.isTrial() && accountStatus.getExpirationTime() > 0) {
            BillingWarning warning = new BillingWarning(accountStatus);
            RootPanel.get().add(warning);
        }
    }
}
