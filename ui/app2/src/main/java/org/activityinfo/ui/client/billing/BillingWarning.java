package org.activityinfo.ui.client.billing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.account.AccountStatus;

import java.util.Date;

public class BillingWarning extends Composite {

    private final AccountStatus status;

    interface BillingWarningUiBinder extends UiBinder<HTMLPanel, BillingWarning> {
    }

    private static BillingWarningUiBinder ourUiBinder = GWT.create(BillingWarningUiBinder.class);


    interface Templates extends SafeHtmlTemplates {

        @Template("<strong>{0}</strong> {1} <a href=\"mailto:info@activityinfo.org\">{2}</a>")
        SafeHtml freeTrialExpired(String title, String message, String buttonLink);

    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);


    @UiField
    DivElement warningText;

    @UiField
    Button closeButton;

    public BillingWarning(AccountStatus status) {
        this.status = status;
        initWidget(ourUiBinder.createAndBindUi(this));

        if(status.isTrial()) {
            if(status.isExpired()) {
                warningText.setInnerSafeHtml(TEMPLATES.freeTrialExpired(
                        I18N.CONSTANTS.trialAccountExpired(),
                        I18N.CONSTANTS.trialAccountExpiredMessage(),
                        I18N.CONSTANTS.contactUs()));

            } else {
                warningText.setInnerHTML("<strong>Your free trial is " + status.expiringIn(new Date()) + ".</strong> " +
                        "After this, you won't be able to access " +
                        "<a href=\"#dblist\">" + status.getDatabaseCount() + " database(s)</a> you own. " +
                        "Please <a href=\"mailto:info@activityinfo.org\">contact us</a> to set up billing. " +
                        "Questions? Take a look at the <a href=\"https://www.activityinfo.org/about/faq.html\">FAQ</a>.");
            }
        } else {
            warningText.setInnerHTML("<strong>Your account is " + status.expiringIn(new Date()) + ".</strong> " +
                    "To avoid interruption in service, <a href=\"mailto:info@activityinfo.org\">contact us</a> " +
                    "to start the renewal process. ");
        }
    }

    @UiHandler("closeButton")
    public void closeButtonClick(ClickEvent event) {
        BillingSupervisor.snooze(status);
        getElement().removeFromParent();
    }

}