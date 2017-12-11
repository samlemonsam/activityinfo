package chdc.frontend.client.theme;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * List of all the icons used in the CHDC application.
 *
 */
public enum Icon {

    TABLE, PLUS, LIST, SEARCH;

    public final SafeHtml svg() {
        return SafeHtmlUtils.fromSafeConstant(
                "<svg viewBox=\"0 0 64 64\" class=\"icon icon--small\">" +
                "<use xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"" + symbolName() + "\"></use>" +
                "</svg>");
    }

    private String symbolName() {
        return "#icon-" + name().toLowerCase();
    }
}
