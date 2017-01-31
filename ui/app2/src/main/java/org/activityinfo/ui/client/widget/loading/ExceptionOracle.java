package org.activityinfo.ui.client.widget.loading;

import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.icons.Icons;

/**
 * Provides user-intelligible information about exceptions
 */
public class ExceptionOracle {

    public static void setLoadingStyle(Widget widget, LoadingState state) {
        widget.setStyleName(LoadingStylesheet.INSTANCE.loading(), state == LoadingState.LOADING);
        widget.setStyleName(LoadingStylesheet.INSTANCE.failed(), state == LoadingState.FAILED);
        widget.setStyleName(LoadingStylesheet.INSTANCE.loaded(), state == LoadingState.LOADED);
    }

    private static boolean isConnectionFailure(Throwable caught) {
        if(caught instanceof StatusCodeException) {
            // Status code 0 indicates that the xhr error flag is set,
            // or loading has been canceled by the browser
            // http://stackoverflow.com/questions/3825581/does-an-http-status-code-of-0-have-any-meaning/26451773#26451773
            StatusCodeException sce = (StatusCodeException) caught;
            if(sce.getStatusCode() == 0) {
                return true;
            }
        } else if(caught instanceof RequestTimeoutException) {
            // if the *browser* is timing out the HTTP connection, then it is likely
            // a network failure as AppEngine should time the request out long before this
            return true;
        }
        return false;
    }

    public static String getIcon(Throwable caught) {
        return isConnectionFailure(caught) ?
                Icons.INSTANCE.connectionProblem() :
                Icons.INSTANCE.exception();
    }

    public static String getHeading(Throwable caught) {
        return isConnectionFailure(caught) ?
                I18N.CONSTANTS.connectionProblem()   :
                I18N.CONSTANTS.unexpectedException() ;
    }

    public static String getExplanation(Throwable caught) {
        return isConnectionFailure(caught) ?
            I18N.CONSTANTS.connectionProblemText() :
            I18N.CONSTANTS.unexpectedExceptionExplanation();
    }
}
