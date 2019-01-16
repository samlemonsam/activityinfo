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
package org.activityinfo.store.testing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.formTree.FormMetadataProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class FormTreeBuilderTest {

    public class TestFormMetadataProvider implements FormMetadataProvider {

        private Map<ResourceId,FormMetadata> forms = Maps.newHashMap();

        public TestFormMetadataProvider(List<TestForm> testForms) {
            FormClass formClass;
            for (TestForm form : testForms) {
                 formClass = form.getFormClass();
                 forms.put(formClass.getId(), FormMetadata.of(0, formClass, FormPermissions.readWrite(null)));
            }
        }

        @Override
        public FormMetadata getFormMetadata(ResourceId formId) {
            return forms.get(formId);
        }
    }


    @Test
    public void buildAndLoadBlankSubform() {
        List<TestForm> testForms = Lists.newArrayList();

        GenericForm generic = new GenericForm();
        BlankSubForm blank = new BlankSubForm(generic);

        testForms.add(generic);
        testForms.add(blank);

        TestFormMetadataProvider provider = new TestFormMetadataProvider(testForms);
        FormTreeBuilder builder = new FormTreeBuilder(provider);

        // Build the form tree, and attempt to fetch the blank sub form
        FormTree tree = builder.queryTree(generic.getFormId());
        tree.getFormClass(blank.getFormId());
    }


}