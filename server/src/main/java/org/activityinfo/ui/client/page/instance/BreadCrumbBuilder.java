package org.activityinfo.ui.client.page.instance;

import com.google.common.base.Function;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.core.shared.criteria.IdCriteria;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.promise.Promise;
import org.activityinfo.i18n.shared.I18N;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Displays a set of parent links
 */
public class BreadCrumbBuilder {

    public interface LinkTemplates extends SafeHtmlTemplates {

        @Template("<a href=\"{0}\">{1}</a>")
        SafeHtml link(SafeUri link, String label);

        @Template("<ol class='breadcrumb'>{0}</ol>")
        SafeHtml container(SafeHtml listItems);
    }

    public static final LinkTemplates TEMPLATES = GWT.create(LinkTemplates.class);

    private final ResourceLocator resourceLocator;
    private final Element element;

    public BreadCrumbBuilder(ResourceLocator resourceLocator, Element element) {
        this.resourceLocator = resourceLocator;
        this.element = element;
    }

    public void show(FormInstance instance) {
        if(instance.getOwnerId() == null) {
            element.setInnerSafeHtml(TEMPLATES.link(UriUtils.fromTrustedString("#home"), I18N.CONSTANTS.home()));

        } else {
            element.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString("&nbsp;"));

            queryParent(instance.getOwnerId())
            .then(new Function<List<Projection>, Void>() {
                @Nullable
                @Override
                public Void apply(List<Projection> projections) {
                    return render(projections);
                }
            });
        }

    }

    private Void render(List<Projection> parents) {
        Projection parent = parents.get(0);

        SafeHtml links = TEMPLATES.link(
                InstancePlace.safeUri(parent.getRootInstanceId()),
                parent.getStringValue(ApplicationProperties.LABEL_PROPERTY));

        element.setInnerSafeHtml(links);

        return null;
    }

    private Promise<List<Projection>> queryParent(ResourceId parentId) {
        return resourceLocator.query(InstanceQuery
                .select(ApplicationProperties.LABEL_PROPERTY)
                .where(new IdCriteria(parentId))
                .build());
    }

}
