/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.endpoint.odk.build;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.io.xform.form.*;
import org.activityinfo.io.xform.xpath.XPathBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ParametrizedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.server.endpoint.odk.OdkField;
import org.activityinfo.server.endpoint.odk.OdkFormFieldBuilder;
import org.activityinfo.server.endpoint.odk.OdkFormFieldBuilderFactory;
import org.activityinfo.server.endpoint.odk.OdkSymbolHandler;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.server.endpoint.odk.OdkHelper.extractLocationReference;
import static org.activityinfo.server.endpoint.odk.OdkHelper.isLocation;

/**
 * Constructs an XForm from a FormClass
 */
public class XFormBuilder {

    private static final Logger LOGGER = Logger.getLogger(XFormBuilder.class.getName());

    private OdkFormFieldBuilderFactory factory;
    private String userId;
    private FormClass formClass;
    private List<OdkField> fields;
    private ResourceId startDateFieldId;
    private ResourceId endDateFieldId;
    private Set<ResourceId> dateFields;
    private ResourceId locationNameFieldId;
    private ResourceId gpsFieldId;
    private OdkSymbolHandler odkSymbolHandler;
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
        odkSymbolHandler = new OdkSymbolHandler(fields);
        xPathBuilder = new XPathBuilder(odkSymbolHandler);
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
                if (builder != OdkFormFieldBuilder.NONE) {
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
                bind.setConstraint(field.getBuilder().getConstraint().orNull());

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
                createLocationElements(field.getModel(), body);
            } else if (field.getModel().isVisible() && !dateFields.contains(field.getModel().getId())) {
                BodyElement element = createPresentationElement(field);
                if (element.isValid()) {
                    body.addElement(element);
                } else {
                    if (field.getModel().isRequired()) {
                        LOGGER.severe("Invalid required field. Deadlock for user. " +
                                "Field id: " + field.getModel().getId() + ", FormClass id: " + formClass.getId());
                    }
                }
            }
        }
        return body;
    }

    private void createLocationElements(FormField field, Body body) {
        ResourceId locationRef = extractLocationReference(field);
        if(locationRef != null) {
            body.addElement(createPresentationElement(locationName(field)));
            body.addElement(createPresentationElement(gps(field)));
        }
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
        bind.setRequired(XPathBuilder.TRUE);
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
        formField.setType(TextType.SIMPLE);
        formField.setLabel(original.getLabel());
        formField.setRequired(original.isRequired());
        return new OdkField(formField, factory.get(formField.getType()));
    }

    private OdkField gps(FormField original) {
        FormField formField = new FormField(gpsFieldId);
        formField.setType(GeoPointType.INSTANCE);
        formField.setLabel("GPS coordinates (" + original.getLabel() + ")");
        return new OdkField(formField, factory.get(formField.getType()));
    }
}
