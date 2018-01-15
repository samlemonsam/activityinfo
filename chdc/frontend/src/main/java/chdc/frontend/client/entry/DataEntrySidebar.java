package chdc.frontend.client.entry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class DataEntrySidebar extends Widget {

    public DataEntrySidebar() {
        Element main = Document.get().createElement("aside");
        main.setAttribute("role", "complementary");
        main.addClassName("sidebar");
        setElement(main);

        renderIncident();
    }

    public void renderIncident() {
        String html = "<div class=\"datasheet\">\n" +
                "  \n" +
                "  <header class=\"datasheet__head\">\n" +
                "    <h2 class=\"incident-title\">\n" +
                "      INCIDENT\n" +
                "      <strong>ID #ARC-0021452</strong>\n" +
                "    </h2>\n" +
                "\n" +
                "    <div class=\"detail-field\">\n" +
                "      <h4 class=\"incident-detail\">Narrative</h4>\n" +
                "      <p class=\"output output-text\">CF conducted an airstrike targeting IS positions.</p>\n" +
                "    </div>\n" +
                "  </header>\n" +
                "\n" +
                "  <div class=\"datasheet__body\">\n" +
                "\n" +
                "    <section class=\"detail-container\">\n" +
                "      <h3>Date &amp; Time</h3>\n" +
                "      <div class=\"formgrid\">\n" +
                "        <div class=\"detail-field date\">\n" +
                "          <h4 class=\"incident-detail\">Date</h4>\n" +
                "          <p class=\"output\">21-01-2016</p>\n" +
                "        </div>\n" +
                "        <div class=\"detail-field time\">\n" +
                "          <h4 class=\"incident-detail\">Time</h4>\n" +
                "          <p class=\"output\">11:00</p>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "    </section>\n" +
                "\n" +
                "    <section class=\"detail-container\">\n" +
                "      <h3>Location</h3>\n" +
                "      <div class=\"detail-field location\">\n" +
                "        <h4 class=\"incident-detail\">Location</h4>\n" +
                "        <p class=\"output output-text\">Ninawa, Mosul, Mosul</p>\n" +
                "      </div>\n" +
                "      <div class=\"detail-field location\">\n" +
                "        <h4 class=\"incident-detail\">Precise location</h4>\n" +
                "        <p class=\"output output-text\">Hay Al-Dubat area</p>\n" +
                "      </div>\n" +
                "\n" +
                "      <div class=\"formgrid\">\n" +
                "        <div class=\"detail-field latitude\">\n" +
                "          <h4 class=\"incident-detail\">Latitude</h4>\n" +
                "          <p class=\"output\">36,339698</p>\n" +
                "        </div>\n" +
                "        <div class=\"detail-field longitude\">\n" +
                "          <h4 class=\"incident-detail\">Longitude</h4>\n" +
                "          <p class=\"output\">43,152521</p>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "    </section>\n" +
                "\n" +
                "    <section class=\"detail-container actor\">\n" +
                "      <h3>Actor</h3>\n" +
                "      <div class=\"formgrid\">\n" +
                "        <div class=\"detail-field\">\n" +
                "          <h4 class=\"incident-detail\">Perpetrator</h4>\n" +
                "          <p class=\"output output-text\">Coalition</p>\n" +
                "        </div>\n" +
                "        <div class=\"detail-field\">\n" +
                "          <h4 class=\"incident-detail\">Victim</h4>\n" +
                "          <p class=\"output output-text\">IS</p>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "\n" +
                "    </section>\n" +
                "\n" +
                "\n" +
                "\n" +
                "    <section class=\"detail-container\">\n" +
                "      <h3>Act</h3>\n" +
                "      <div class=\"detail-field\">\n" +
                "        <h4 class=\"incident-detail\">Mode of action</h4>\n" +
                "        <p class=\"output output-text\">Perpetrated</p>\n" +
                "      </div>\n" +
                "      \n" +
                "      <div class=\"detail-field\">\n" +
                "        <h4 class=\"incident-detail\">Act</h4>\n" +
                "        <p class=\"output output-text\">Air-to-ground attack</p>\n" +
                "      </div>\n" +
                "\n" +
                "    </section>\n" +
                "\n" +
                "\n" +
                "    <section class=\"detail-container\">\n" +
                "      <h3>Means</h3>\n" +
                "      <div class=\"detail-field\">\n" +
                "        <h4 class=\"incident-detail\">Means</h4>\n" +
                "        <p class=\"output output-text\">Air-to-surface missiles</p>\n" +
                "      </div>\n" +
                "\n" +
                "    </section>\n" +
                "\n" +
                "    <section class=\"detail-container\">\n" +
                "      <h3>Impact</h3>\n" +
                "      <div class=\"detail-field\">\n" +
                "        <h4 class=\"incident-detail\">IS</h4>\n" +
                "        <p class=\"output output-text\">Unknown</p>\n" +
                "      </div>\n" +
                "\n" +
                "    </section>\n" +
                "  </div>\n" +
                "\n" +
                "\n" +
                "  <div class=\"datasheet__controls\">\n" +
                "\n" +
                "    <button class=\"button button--icon\" data-print=\"\">\n" +
                "      <svg viewBox=\"0 0 64 64\" class=\"icon\">\n" +
                "        <use xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"#icon-printer\"></use>\n" +
                "      </svg>\n" +
                "      <span>Print</span>\n" +
                "    </button>\n" +
                "\n" +
                "    <button class=\"button button--icon\" data-panel=\"panel-share\">\n" +
                "      <svg viewBox=\"0 0 64 64\" class=\"icon\">\n" +
                "        <use xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"#icon-email\"></use>\n" +
                "      </svg>\n" +
                "      <span>E-mail</span>\n" +
                "    </button>\n" +
                "\n" +
                "    \n" +
                "  </div>\n" +
                "\n" +
                "</div>";

        getElement().setInnerHTML(html);
    }
}
