package org.activityinfo.legacy.shared.command.result;

import org.activityinfo.legacy.shared.exception.CommandException;

/**
 * Thrown when a user with the given email address already exists in the database.
 */
public class UserExistsException extends CommandException {
}
