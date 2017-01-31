package org.activityinfo.ui.client.style;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.shared.GWT;

/**
 * Defines the base styles for ActivityInfo, including normalize.css
 * and standard typographic styles for basic elements like {@code h1},
 * {@code h2}, {@code p}, etc.
 *
 * <p>For the moment, the rules are prefixed by {@code .bs} to avoid
 * interference with GXT styles.</p>
 */
@Source("base.less")
public interface BaseStylesheet extends Stylesheet {

    public static final BaseStylesheet INSTANCE = GWT.create(BaseStylesheet.class);


    /**
     * The Bootstrap styles have been enclosed in this style
     * to avoid messing with rest of the GXT styles for the time being.
     */
    String CONTAINER_STYLE = "bs";
}
