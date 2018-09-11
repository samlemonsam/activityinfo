package org.activityinfo.legacy.shared.command.result;

import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.account.AccountStatus;

/**
 * An exception raised indicating that the user does not have the right to
 * complete the given action because of a billing account issue.
 */
public class BillingException extends CommandException {

    private boolean trial;
    private String billingAccountName;
    private int userLimit;

    public BillingException() {
        super("Billing limit exceeded");
    }

    public BillingException(AccountStatus status) {
        super("Billing limit exceeded");
        trial = status.isTrial();
        userLimit = status.getUserLimit();
        billingAccountName = status.getBillingAccountName();
    }

    public boolean isTrial() {
        return trial;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public String getBillingAccountName() {
        return billingAccountName;
    }
}
