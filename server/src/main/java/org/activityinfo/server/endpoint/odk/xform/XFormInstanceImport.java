package org.activityinfo.server.endpoint.odk.xform;

import com.google.common.collect.HashMultiset;
import org.activityinfo.io.xform.XFormReader;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.server.endpoint.odk.XFormInstanceReader;
import org.codehaus.jackson.map.ObjectMapper;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import static java.lang.System.exit;

public class XFormInstanceImport {
    public static void main(String[] args) {
        final File form, instance;

        if (args.length == 2) {
            form = new File(args[0]);
            instance = new File(args[1]);
        } else {
            System.err.println("Two command line arguments needed: an XML form definition and its JSON instance array");
            exit(2);
            return;                     // Honestly, this should not be necessary, but the compiler complains otherwise.
        }

        final XForm xform;
        final XFormReader reader;
        final FormClass formClass;
        final XFormInstanceReader xFormInstanceReader;
        final ArrayList<FormField> formFields;

        try {
            xform = (XForm) JAXBContext.newInstance(XForm.class).createUnmarshaller().unmarshal(form.toURI().toURL());
            reader = new XFormReader(xform);
            formClass = reader.build();
            xFormInstanceReader = new XFormInstanceReader(new ObjectMapper().readValue(instance, LinkedHashMap[].class),
                    formClass);
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
            return;                     // Honestly, this should not be necessary, but the compiler complains otherwise.
        }

        formFields = new ArrayList<>(formClass.getFields());
        Collections.sort(formFields, new Comparator<FormField>() {
            @Override
            public int compare(FormField o1, FormField o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        });

        FormInstance formInstances[] = xFormInstanceReader.build();
        int total = 0;

        System.out.printf("%d form instances were read.\n", formInstances.length);

        for (FormInstance formInstance : formInstances) {
            HashMultiset<Class<? extends FieldValue>> classes = HashMultiset.create();

            for (FieldValue fieldValue : formInstance.getFieldValueMap().values()) {
                classes.add(fieldValue.getClass());
            }

            for (Class<? extends FieldValue> fieldValueClass : classes.elementSet()) {
                System.out.printf("%s occurs %d times, ", fieldValueClass.getSimpleName(), classes.count(fieldValueClass));
            }

            int size = classes.size();
            total += size;

            System.out.printf("for a total of %d field values in a single form instance.\n", size);
        }

        System.out.printf("A grand total of %d field values were read.\n", total);
    }
}
