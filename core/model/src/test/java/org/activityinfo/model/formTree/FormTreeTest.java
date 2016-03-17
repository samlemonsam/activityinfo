package org.activityinfo.model.formTree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FormTreeTest {

    private Forms forms = new Forms();
    
    @Test
    public void builder() {
        FormTreeBuilder builder = new FormTreeBuilder(forms);
        FormTree formTree = builder.queryTree(forms.student.getId());
        
        checkStudentTree(formTree);
    }

    @Test
    public void asyncBuilder() {
        AsyncFormTreeBuilder builder = new AsyncFormTreeBuilder(forms.async());
        FormTree formTree = builder.apply(forms.student.getId()).get();

        checkStudentTree(formTree);
    }
    
    @Test
    public void toJson() {
        FormTreeBuilder builder = new FormTreeBuilder(forms);
        FormTree formTree = builder.queryTree(forms.student.getId());

        checkStudentTree(formTree);

        JsonObject object = JsonFormTreeBuilder.toJson(formTree);
        
        Gson prettyPrintingJson = new GsonBuilder().setPrettyPrinting().create();
        
        System.out.println(prettyPrintingJson.toJson(object));
        
        FormTree reTree = JsonFormTreeBuilder.fromJson(object);
        
        checkStudentTree(reTree);
        


    }
    
    private void checkStudentTree(FormTree tree) {

        FormTreePrettyPrinter.print(tree);

        assertThat(tree.getRootFormClasses().values(), contains(hasProperty("id", equalTo(forms.student.getId()))));
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