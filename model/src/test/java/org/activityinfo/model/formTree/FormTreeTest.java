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
package org.activityinfo.model.formTree;

import org.activityinfo.json.JsonValue;
import org.activityinfo.json.impl.JsonUtil;
import org.activityinfo.model.resource.ResourceId;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class FormTreeTest {

    private Forms forms = new Forms();
    
    @Test
    public void builder() {
        FormTreeBuilder builder = new FormTreeBuilder(forms);
        FormTree formTree = builder.queryTree(forms.student.getId());
        
        checkStudentTree(formTree);
    }

    @Test
    public void toJson() {
        FormTreeBuilder builder = new FormTreeBuilder(forms);
        FormTree formTree = builder.queryTree(forms.student.getId());

        checkStudentTree(formTree);

        JsonValue object = JsonFormTreeBuilder.toJson(formTree);
        
        System.out.println(JsonUtil.stringify(object, 2));
        
        FormTree reTree = JsonFormTreeBuilder.fromJson(object);
        
        checkStudentTree(reTree);

    }
    
    private void checkStudentTree(FormTree tree) {

        FormTreePrettyPrinter.print(tree);

        assertThat(tree.getRootFormId(), equalTo(forms.student.getId()));
        assertThat(tree, hasNode(path("name"), notNullValue()));
        assertThat(tree, hasNode(path("school", "schoolId"), notNullValue()));
        assertThat(tree, hasNode(path("school", "village", "name"), notNullValue()));
    }
    
    
    private TypeSafeMatcher<FormTree> hasNode(final FieldPath path, final Matcher matcher) {
        return new TypeSafeMatcher<FormTree>() {
            @Override
            protected boolean matchesSafely(FormTree tree) {
                FormTree.Node node;
                try {
                    node = tree.getNodeByPath(path);
                } catch (IllegalArgumentException e) {
                    return false;
                }
                return matcher.matches(node);
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("FormTree with node ").appendValue(path).appendText(" ").appendDescriptionOf(matcher);
            }
        };
    }
    
    private FieldPath path(String... fields) {
        List<ResourceId> fieldIds = new ArrayList<>();
        for (String field : fields) {
            fieldIds.add(ResourceId.valueOf(field));
        }
        return new FieldPath(fieldIds);
    }


}