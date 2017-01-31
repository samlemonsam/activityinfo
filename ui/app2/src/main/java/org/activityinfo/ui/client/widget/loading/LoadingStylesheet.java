package org.activityinfo.ui.client.widget.loading;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.client.GWT;


@Source("Loading.less")
public interface LoadingStylesheet extends Stylesheet {

    public static final LoadingStylesheet INSTANCE = GWT.create(LoadingStylesheet.class);

    String loadingContainer();
    String indicator();
    String loading();
    String failed();
    String loaded();

    String imageContainer();
}
