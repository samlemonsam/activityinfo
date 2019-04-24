package org.activityinfo.server.login;

import org.junit.Test;

public class SignUpControllerTest {

    @Test(expected = IllegalArgumentException.class)
    public void linkSpam() {
        SignUpController.checkParam("AШ ВЫЙГPЫШЬ CОСTАВИЛ https://--nter.com|Как прошел", false);
    }

    @Test
    public void notLinkSpam() {
        SignUpController.checkParam("Bob", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void linkSpamRequiredField() {
        SignUpController.checkParam("AШ ВЫЙГPЫШЬ CОСTАВИЛ https://--nter.com|Как прошел", true);
    }

    @Test
    public void nullDataNotRequiredField() {
        SignUpController.checkParam(null, false);
    }

}