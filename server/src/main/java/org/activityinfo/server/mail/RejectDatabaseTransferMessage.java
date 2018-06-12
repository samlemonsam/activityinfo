package org.activityinfo.server.mail;

import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;

public class RejectDatabaseTransferMessage extends MessageModel {

    private User recipient;
    private User approver;
    private Database database;

    public RejectDatabaseTransferMessage(User recipient, User approver, Database database) {
        this.recipient = recipient;
        this.approver = approver;
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public User getApprover() {
        return approver;
    }

    @Override
    public User getRecipient() {
        return recipient;
    }


}
