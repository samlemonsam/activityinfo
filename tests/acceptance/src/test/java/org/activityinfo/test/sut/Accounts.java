package org.activityinfo.test.sut;

/**
 * Strategy for creating or accessing accounts on the server under test
 */
public interface Accounts {


    UserAccount createAccount(String email);

    UserAccount getAccount(String user);
    
    UserAccount any();

}
