package org.activityinfo.ui.client;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.common.base.Function;
import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.account.AccountStatus;

import javax.annotation.Nullable;

public class BillingSupervisor {

    private final ActivityInfoClientAsync client;

    public BillingSupervisor(ActivityInfoClientAsync client) {
        this.client = client;
    }

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
        if(accountStatus.getUserCount() > accountStatus.getUserLimit()) {
            MessageBox.alert("Account Problem", "You currently have " + accountStatus.getUserCount() +
                " users, while your plan is limited to " + accountStatus.getUserLimit() +
                    ". Please contact us within the next seven days to upgrade your plan and avoid interruption " +
                    " to your account.", null);
        }
    }
}
