package org.activityinfo.server.endpoint.odk.build;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.io.xform.form.Bind;
import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.Body;
import org.activityinfo.io.xform.form.BodyElement;
import org.activityinfo.io.xform.form.Instance;
import org.activityinfo.io.xform.form.InstanceElement;
import org.activityinfo.io.xform.form.Model;
import org.activityinfo.io.xform.form.Translation;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ParametrizedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.server.endpoint.odk.OdkField;
import org.activityinfo.server.endpoint.odk.OdkFormFieldBuilder;
import org.activityinfo.server.endpoint.odk.OdkFormFieldBuilderFactory;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.activityinfo.model.legacy.CuidAdapter.END_DATE_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.GPS_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.LOCATION_NAME_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.START_DATE_FIELD;
import static org.activityinfo.model.legacy.CuidAdapter.field;
import static org.activityinfo.server.endpoint.odk.OdkHelper.isLocation;

/**
 * Constructs an XForm from a FormClass
 */
public class XFormBuilder {
    private OdkFormFieldBuilderFactory factory;
    private String userId;
    private FormClass formClass;
    private List<OdkField> fields;
    private ResourceId startDateFieldId;
    private ResourceId endDateFieldId;
    private Set<ResourceId> dateFields;
    private ResourceId locationNameFieldId;
    private ResourceId gpsFieldId;
    private XPathBuilder xPathBuilder;
    private XForm xform;

    public XFormBuilder(OdkFormFieldBuilderFactory factory) {
        this.factory = factory;
    }

    public XFormBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public XForm build(FormClass formClass) {
        this.formClass = formClass;

        startDateFieldId = field(formClass.getId(), START_DATE_FIELD);
        endDateFieldId = field(formClass.getId(), END_DATE_FIELD);
        dateFields = Sets.newHashSet(startDateFieldId, endDateFieldId);
        locationNameFieldId = field(formClass.getId(), LOCATION_NAME_FIELD);
        gpsFieldId = field(formClass.getId(), GPS_FIELD);

        fields = createFieldBuilders(formClass);
        xPathBuilder = new XPathBuilder(fields);
        xform = new XForm();
        xform.getHead().setTitle(formClass.getLabel());
        xform.getHead().setModel(createModel());
        xform.setBody(createBody());

        return xform;
    }

    private List<OdkField> createFieldBuilders(FormClass formClass) {
        fields = new ArrayList<>();
        for (FormField field : formClass.getFields()) {
            if(field.isVisible() && isValid(field)) {
                OdkFormFieldBuilder builder = factory.get(field.getType());
                if (builder != null) {
                    fields.add(new OdkField(field, builder));
                }
            }
        }
        return fields;
    }

    private boolean isValid(FormField field) {
        if(!(field.getType() instanceof ParameterizedType)) {
            return true;
        }
        ParametrizedFieldType type = (ParametrizedFieldType) field.getType();
        return type.isValid();
    }

    private Model createModel() {
        Model model = new Model();
        model.getItext().getTranslations().add(Translation.defaultTranslation());
        model.setInstance(createInstance());
        model.getBindings().addAll(createBindings());
        return model;
    }

    private Instance createInstance() {

        InstanceElement data = new InstanceElement("data");
        data.setId(formClass.getId().asString());
        data.addChild(
                new InstanceElement("meta",
                        new InstanceElement("instanceID"),
                        new InstanceElement("userID", userId)));

        data.addChild(new InstanceElement("field_" + startDateFieldId.asString()));
        data.addChild(new InstanceElement("field_" + endDateFieldId.asString()));

        for (OdkField field : fields) {
            if (isLocation(formClass, field.getModel())) {
                data.addChild(new InstanceElement("field_" + locationNameFieldId.asString()));
                data.addChild(new InstanceElement("field_" + gpsFieldId.asString()));
            } else if (!dateFields.contains(field.getModel().getId())) {
                data.addChild(new InstanceElement(field.getRelativeFieldName()));
            }
        }

        return new Instance(data);
    }


    private Collection<Bind> createBindings() {
        List<Bind> bindings = Lists.newArrayList();
        bindings.add(instanceIdBinding());
        bindings.add(startDate());
        bindings.add(endDate());

        for (OdkField field : fields) {
            // As a transitional hack, populate the startDate and endDate of the "activity"
            // with the start/end date of interview
            if (isLocation(formClass, field.getModel())) {
                bindings.add(locationNameField());
                bindings.add(gpsField());
            } else if (!dateFields.contains(field.getModel().getId())) {
                Bind bind = new Bind();
                bind.setNodeSet(field.getAbsoluteFieldName());
                bind.setType(field.getBuilder().getModelBindType());
                if (field.getModel().isReadOnly()) {
                    bind.setReadonly(XPathBuilder.TRUE);
                }
                //TODO Fix this
                //bind.calculate = formField.getExpression();
                bind.setRelevant(xPathBuilder.build(field.getModel().getRelevanceConditionExpression()));
                if (field.getModel().isRequired()) {
                    bind.setRequired(XPathBuilder.TRUE);
                }
                bindings.add(bind);
            }
        }
        return bindings;
    }

    private Body createBody() {
        Body body = new Body();

        for (OdkField field : fields) {
            if (isLocation(formClass, field.getModel())) {
                body.addElement(createPresentationElement(locationName(field.getModel())));
                body.addElement(createPresentationElement(gps(field.getModel())));
            } else if (field.getModel().isVisible() && !dateFields.contains(field.getModel().getId())) {
                body.addElement(createPresentationElement(field));
            }
        }
        return body;
    }

    private BodyElement createPresentationElement(OdkField formField) {
        return formField.getBuilder().createBodyElement(
                formField.getAbsoluteFieldName(),
                formField.getModel().getLabel(),
                formField.getModel().getDescription());
    }

    private Bind instanceIdBinding() {
        Bind bind = new Bind();
        bind.setNodeSet("/data/meta/instanceID");
        bind.setType(BindingType.STRING);
        bind.setReadonly(XPathBuilder.TRUE);
        bind.setCalculate("concat('uuid:',uuid())");
        return bind;
    }

    private Bind startDate() {
        Bind bind = new Bind();
        bind.setNodeSet("/data/field_" + startDateFieldId.asString());
        bind.setType(BindingType.DATETIME);
        bind.setPreload("timestamp");
        bind.setPreloadParams("start");
        return bind;
    }

    private Bind endDate() {
        Bind bind = new Bind();
        bind.setNodeSet("/data/field_" + endDateFieldId.asString());
        bind.setReadonly(XPathBuilder.TRUE);
        bind.setPreload("timestamp");
        bind.setPreloadParams("end");
        return bind;
    }

    private Bind locationNameField() {
        Bind bind = new Bind();
        bind.setNodeSet("/data/field_" + locationNameFieldId.asString());
        bind.setType(BindingType.STRING);
        return bind;
    }

    private Bind gpsField() {
        Bind bind = new Bind();
        bind.setNodeSet("/data/field_" + gpsFieldId.asString());
        bind.setType(BindingType.GEOPOINT);
        return bind;
    }

    private OdkField locationName(FormField original) {
        FormField formField = new FormField(locationNameFieldId);
        formField.setType(TextType.INSTANCE);
        formField.setLabel(original.getLabel());
        return new OdkField(formField, factory.get(formField.getType()));
    }

    private OdkField gps(FormField original) {
        FormField formField = new FormField(gpsFieldId);
        formField.setType(GeoPointType.INSTANCE);
        formField.setLabel("GPS coordinates (" + original.getLabel() + ")");
        return new OdkField(formField, factory.get(formField.getType()));
    }
}
