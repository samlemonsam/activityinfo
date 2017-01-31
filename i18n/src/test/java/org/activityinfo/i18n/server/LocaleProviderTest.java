package org.activityinfo.i18n.server;

import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Verify JDK behavior...
 */
public class LocaleProviderTest {

    @Test
    public void test() {
        Locale en = Locale.forLanguageTag("en");
        assertThat(en.getLanguage(), equalTo("en"));

        Locale american = Locale.forLanguageTag("en-US");
        assertThat(american.getLanguage(), equalTo("en"));
        assertThat(american.getCountry(), equalTo("US"));
    }
}
