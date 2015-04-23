package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.LinkedHashMap;

import static org.activityinfo.server.endpoint.odk.FieldValueParserFactory.fromFieldType;

public class XFormInstanceReader {
    final private LinkedHashMap<String, Object> array[];
    final private FormClass formClass;

    public XFormInstanceReader(LinkedHashMap<String, Object> array[], FormClass formClass) {
        Preconditions.checkNotNull(array, formClass);
        this.array = array;
        this.formClass = formClass;
    }

    public FormInstance[] build() {
        final int length = array.length;
        final ResourceId formClassId = formClass.getId();
        final FormInstance formInstances[] = new FormInstance[length];

        for (int i = 0; i < length; i++) {
            formInstances[i] = new FormInstance(CuidAdapter.newLegacyFormInstanceId(formClassId), formClassId);
        }

        for (FormField formField : formClass.getFields()) {
            final FieldValueParser fieldValueParser = fromFieldType(formField.getType(), false, false);
            final String code = formField.getCode();

            for (int i = 0; i < length; i++) {
                final LinkedHashMap<String, Object> map = array[i];
                final FormInstance formInstance = formInstances[i];

                if (map != null) {
                    final Object value = map.get(code);

                    if (value instanceof String) {
                        formInstance.set(formField.getId(), fieldValueParser.parse((String) value));
                    }
                }
            }
        }

        return formInstances;
    }
}
