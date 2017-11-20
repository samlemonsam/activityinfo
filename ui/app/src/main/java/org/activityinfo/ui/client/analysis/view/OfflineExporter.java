package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;

/**
 * Exports a CSV file from the browser without a call to the server
 */
public class OfflineExporter {

    public static final String CSV_MIMETYPE = "text/csv;charset=utf-8";

    public static void export(String filename, String data, String mimeType) {
        if(isIE()) {
            exportUsingMsBlob(filename, data);
        } else {
            exportUsingDataUri(filename, data, mimeType);
        }
    }

    private static native void exportUsingMsBlob(String filename, String csv) /*-{
        var blobObject = new $wnd.Blob([csv]);
        $wnd.navigator.msSaveBlob(blobObject, filename);
    }-*/;

    private static void exportUsingDataUri(String filename, String csv, String mimeType) {
        String href = createDataUri(mimeType, csv);
        AnchorElement a = createLinkElement(filename, href);
        Document.get().getBody().appendChild(a);

        click(a);

        a.removeFromParent();
    }

    private static boolean isIE() {
        return System.getProperty("user.agent", "safari").startsWith("ie");
    }

    private static native void click(AnchorElement a) /*-{
        a.click();
    }-*/;

    private static native String createDataUri(String mimeType, String data) /*-{
        var type = 'data:' + mimeType;
        if ($wnd.btoa) {
            type += ';base64';
            data = $wnd.btoa(data);
        } else {
            data = $wnd.encodeURIComponent(data);
        }
        return type + ',' + data;
    }-*/;

    private static AnchorElement createLinkElement(String filename, String href) {
        AnchorElement a = Document.get().createAnchorElement();
        a.setInnerText("");
        a.setHref(href);
        a.setAttribute("download", filename);
        a.getStyle().setDisplay(Style.Display.NONE);
        return a;
    }




}
