package org.activityinfo.io.xform;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.io.xform.form.*;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Creates a {@link org.activityinfo.model.form.FormClass} from an XForm Document.
 */
public class XFormReader {

    private static final Logger LOGGER = Logger.getLogger(XFormReader.class.getName());

    private XForm xForm;
    private FormClass formClass = new FormClass(ResourceId.generateId());

    private Map<String, Bind> bindings = new HashMap<>();
    private Map<String, InstanceElement> instanceElements = new HashMap<>();
    private Map<InstanceElement, InstanceElement> instanceElementParents = new HashMap<>();

    public XFormReader(XForm xForm) {
        this.xForm = xForm;
    }

    public void setActivityId(int activityId) {
        this.formClass.setId(CuidAdapter.activityFormClass(activityId));
    }

    public void setDatabaseId(int databaseId) {
        this.formClass.setOwnerId(CuidAdapter.databaseId(databaseId));
    }


    public FormClass build() {

        formClass.setLabel(xForm.getHead().getTitle());

        findInstanceElements("", xForm.getHead().getModel().getInstance().getRoot());
        findBindings();

        addFieldElements(formClass, xForm.getBody().getElements());

        return formClass;
    }

    private void findInstanceElements(String pathPrefix, InstanceElement element) {
        String path = pathPrefix + "/" + element.getName();
        instanceElements.put(path, element);

        for(InstanceElement child : element.getChildren()) {
            instanceElementParents.put(child, element);
            findInstanceElements(path, child);
        }
    }

    private void findBindings() {
        for (Bind binding : xForm.getHead().getModel().getBindings()) {
            bindings.put(binding.getNodeSet(), binding);
        }
    }

    private void addFieldElements(FormElementContainer container, List<BodyElement> elements) {
        for(BodyElement element : elements) {
            if (element instanceof Group) {
                Group group = (Group) element;
                if (!Strings.isNullOrEmpty(element.getLabel())) {
                    FormSection section = createSection(group);
                    addFieldElements(section, group.getElements());
                    container.addElement(section);
                } else {
                    addFieldElements(container, group.getElements());
                }
            } else {
                container.addElement(createField(element));
            }
        }
    }

    private FormSection createSection(Group element) {
        FormSection section = new FormSection(ResourceId.generateId());
        section.setLabel(element.getLabel());
        return section;
    }

    private FormElement createField(BodyElement element) {

        if(Strings.isNullOrEmpty(element.getLabel())) {
            throw new RuntimeException("Element " + element.getRef() + " has no label");
        }

        InstanceElement instanceElement = instanceElements.get(element.getRef());
        Bind bind = bindings.get(element.getRef());

        FieldType type = createType(element, bind);

        FormField field = new FormField(ResourceId.generateFieldId(type.getTypeClass()));
        field.setCode(buildCode(null, instanceElement).toString());
        field.setLabel(element.getLabel());
        field.setDescription(element.getHint());
        field.setType(type);
        return field;
    }

    private FieldType createType(BodyElement element, Bind binding) {
        switch (binding.getType()) {
            case STRING:
                return TextType.INSTANCE;
            case DATE:
            case DATETIME:
                return LocalDateType.INSTANCE;
            case SELECT1:
                return new EnumType(Cardinality.SINGLE, enumItems((SelectElement) element));
            case SELECT:
                return new EnumType(Cardinality.MULTIPLE, enumItems((SelectElement) element));
            case INT:
            case DECIMAL:
                return new QuantityType();
            case GEOPOINT:
                return GeoPointType.INSTANCE;
        }
        throw new IllegalArgumentException(binding.getType().name());
    }

    private List<EnumItem> enumItems(SelectElement element) {
        List<EnumItem> enumItems = Lists.newArrayList();
        for (Item item : element.getItems()) {
            EnumItem enumItem = new EnumItem(EnumItem.generateId(), item.getLabel());
            enumItem.setCode(item.getValue());
            enumItems.add(enumItem);
        }
        return enumItems;
    }

    private StringBuilder buildCode(InstanceElement instanceElement, InstanceElement parentInstanceElement) {
        StringBuilder stringBuilder = null;

        if (parentInstanceElement != null) {
            stringBuilder = buildCode(parentInstanceElement, instanceElementParents.get(parentInstanceElement));

            if (instanceElement != null) {
                if (stringBuilder == null) {
                    stringBuilder = new StringBuilder();
                } else {
                    stringBuilder.append('/');
                }

                stringBuilder.append(instanceElement.getName());
            }
        }

        return stringBuilder;
    }
}
