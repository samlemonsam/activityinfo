package org.activityinfo.server.mail;

import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;

public class SuccessfulDatabaseTransferMessage extends MessageModel {

    private User recipient;
    private Database database;

    public SuccessfulDatabaseTransferMessage(User recipient, Database database) {
        this.recipient = recipient;
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public User getRecipient() {
        return recipient;
    }


}
