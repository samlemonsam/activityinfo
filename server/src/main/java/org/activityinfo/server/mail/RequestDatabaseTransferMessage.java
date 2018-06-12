package org.activityinfo.server.mail;

import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;

public class RequestDatabaseTransferMessage extends MessageModel {

    private User recipient;
    private User requester;
    private Database database;

    public RequestDatabaseTransferMessage(User recipient, User requester, Database database) {
        this.recipient = recipient;
        this.requester = requester;
        this.database = database;
    }

    public User getRequester() {
        return requester;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public User getRecipient() {
        return recipient;
    }


}
