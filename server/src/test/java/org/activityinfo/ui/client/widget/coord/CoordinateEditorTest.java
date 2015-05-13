package org.activityinfo.ui.client.widget.coord;

import com.teklabs.gwt.i18n.server.LocaleProxy;
import org.activityinfo.core.server.type.converter.JreNumberFormats;
import org.activityinfo.core.shared.type.converter.CoordinateAxis;
import org.activityinfo.i18n.shared.I18N;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class CoordinateEditorTest {

    @Before
    public void setupLocale() {
        LocaleProxy.initialize();
    }
    
    @Test
    public void rdcFrench() {
        
        LocaleProxy.setLocale(Locale.FRANCE);
        
        CoordinateEditor latitude = new CoordinateEditor(CoordinateAxis.LATITUDE, new JreNumberFormats());
        latitude.setMinValue(-13.45599996);
        latitude.setMaxValue(5.386098154);
        latitude.setOutOfBoundsMessage(I18N.MESSAGES.coordOutsideBounds("RDC"));
        
        assertThat(latitude.validate("2.3 N"), nullValue());
        assertThat(latitude.validate("3° 30' 48,32\" S"), nullValue());
    }
    
}