package org.activityinfo.ui.client.pageView.folder;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.client.Resources;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.core.shared.form.FormInstance;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.chrome.PageHeader;
import org.activityinfo.ui.client.page.instance.InstancePlace;
import org.activityinfo.ui.client.pageView.InstancePageView;
import org.activityinfo.ui.client.pageView.InstanceViewModel;

import javax.annotation.Nullable;
import java.util.List;

import static org.activityinfo.core.shared.application.ApplicationProperties.DESCRIPTION_PROPERTY;
import static org.activityinfo.core.shared.application.ApplicationProperties.LABEL_PROPERTY;

/**
 * View for Folder instances
 */
public class FolderPageView implements InstancePageView {


    public static final String NON_BREAKING_SPACE = "\u00A0";
    private FormInstance instance;
    private Resources resources;

    interface FolderViewUiBinder extends UiBinder<HTMLPanel, FolderPageView> {
    }

    public interface Templates extends SafeHtmlTemplates {

        @Template("<li><a href='{0}'><i class='fa fa-folder-o'></i> {1}</a></li>")
        SafeHtml folder(String link, String name);
    }

    private static FolderViewStylesheet stylesheet = GWT.create(FolderViewStylesheet.class);

    private static FolderViewUiBinder ourUiBinder = GWT.create(FolderViewUiBinder.class);

    private static Templates templates = GWT.create(Templates.class);

    private final HTMLPanel rootElement;

    @UiField PageHeader pageHeader;

    @UiField UListElement folderList;
    @UiField DivElement formListBody;

    public FolderPageView(ResourceLocator resourceLocator) {
        this.resources = new Resources(resourceLocator);
        rootElement = ourUiBinder.createAndBindUi(this);
        stylesheet.ensureInjected();
    }

    @Override
    public Promise<Void> show(InstanceViewModel view) {
        this.instance = view.getInstance();
        pageHeader.setPageTitle(instance.getString(FolderClass.LABEL_FIELD_ID));
        pageHeader.setIconStyle("glyphicon glyphicon-folder-open");

        return resources.query(childrenQuery()).then(new Function<List<Projection>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable List<Projection> input) {
                renderFolders(input);
                renderForms(input);
                return null;
            }
        });
    }

    private void renderFolders(List<Projection> projections) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        for(Projection projection : projections) {
            if(projection.getRootClassId().equals(FolderClass.CLASS_ID)) {
                html.append(templates.folder(
                        InstancePlace.safeUri(projection.getRootInstanceId()).asString(),
                        projection.getStringValue(LABEL_PROPERTY)));
            }
        }
        folderList.setInnerSafeHtml(html.toSafeHtml());
    }

    private void renderForms(List<Projection> projections) {

        FormItemRenderer renderer = GWT.create(FormItemRenderer.class);

        SafeHtmlBuilder html = new SafeHtmlBuilder();
        for(Projection projection : projections) {
            if(projection.getRootClassId().equals(FormClass.CLASS_ID)) {
                renderer.render(html, projection.getStringValue(LABEL_PROPERTY),
                        Objects.firstNonNull(projection.getStringValue(DESCRIPTION_PROPERTY), NON_BREAKING_SPACE),
                        InstancePlace.safeUri(projection.getRootInstanceId()).asString());
            }
        }
        formListBody.setInnerSafeHtml(html.toSafeHtml());
    }

    private InstanceQuery childrenQuery() {
        return InstanceQuery
            .select(
                    ApplicationProperties.LABEL_PROPERTY,
                    ApplicationProperties.DESCRIPTION_PROPERTY)
            .where(ParentCriteria.isChildOf(instance.getId())).build();
    }


    @Override
    public Widget asWidget() {
        return rootElement;
    }

}