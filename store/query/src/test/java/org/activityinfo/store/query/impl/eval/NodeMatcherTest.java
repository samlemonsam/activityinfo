package org.activityinfo.store.query.impl.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class NodeMatcherTest {

    private FormClass rootFormClass;
    private Map<ResourceId, FormClass> formClasses = Maps.newHashMap();
    private NodeMatcher symbolTable;
    
    
    @Test
    public void basicForm() {
        givenRootForm("Contact Form", field("A", "Name"), field("B", "Phone Number"));
        
        assertThat(resolve("A"), contains("A"));
        assertThat(resolve("Name"), contains("A"));
        assertThat(resolve("[Phone Number]"), contains("B"));
        assertThat(resolve("[phone number]"), contains("B"));

    }
    
    @Test(expected = AmbiguousSymbolException.class)
    public void ambiguousRootField() {
        givenRootForm("Contact Form", field("A", "Name"), field("B", "name"));

        // Should always be able to resolve by ID
        assertThat(resolve("A"), contains("A"));
 
        // If there are conflicting matches at the root level, then throw an exception
        resolve("Name");
    }
    
    @Test
    public void childMatch() {
        givenRootForm("Project Site",
                field("A", "Name"),
                referenceField("B", "Location",
                        formClass("LocationForm",
                                field("LA", "Name"),
                                field("LB", "Population"))));
        
        
        assertThat(resolve("Name"), contains("A"));
        assertThat(resolve("Location.Name"), contains("B.LA"));
        assertThat(resolve("Population"), contains("B.LB"));
    }
    
    @Test
    public void childUnionMatch() {
        givenRootForm("Site",
                field("A", "Name"),
                referenceField("B", "Location",
                        formClass("Village",
                                field("VA", "Name"),
                                field("VB", "Population")),
                        formClass("Health Center",
                                field("HA", "Name"))));
        prettyPrintTree();
        
        assertThat(resolve("Name"), contains("A"));
        assertThat(resolve("Location.Name"), Matchers.containsInAnyOrder("B.VA", "B.HA"));
        assertThat(resolve("Location.Population"), contains("B.VB"));
    }

    @Test
    public void descendantUnionResourceId() {
        given(formClass("Province", field("PN", "Name")));
        given(formClass("Territoire", field("TN", "Name"), referenceField("TP", "Province", "Province")));
        
        givenRootForm("Project Site",
                field("A", "Name"),
                referenceField("B", "Location", "Province", "Territoire"));
        
        prettyPrintTree();
        
        assertThat(resolve("Province._id"), contains("B.Province@id", "B.TP.Province@id"));
    }
    
    @Test
    public void descendantFormClass() {
        given(formClass("Province", field("PN", "Name")));
        given(formClass("Territoire", field("TN", "Name"), referenceField("TP", "Province", "Province")));
        givenRootForm("Project Site",
                field("SN", "Name"),
                referenceField("SL", "Location",
                        "Territoire",
                        "Province"));
        
        
        assertThat(resolve("Name"), contains("SN"));
        assertThat(resolve("Province.Name"), Matchers.containsInAnyOrder("SL.TP.PN", "SL.PN"));
        
    }
    
    
    private void prettyPrintTree() {
        FormTreePrettyPrinter prettyPrinter = new FormTreePrettyPrinter();
        prettyPrinter.printTree(tree());
    }
    

    private void givenRootForm(String label, FormField... fields) {
        if(rootFormClass != null) { 
            throw new IllegalStateException("Root Form Class already set");
        }
        
        rootFormClass = new FormClass(ResourceId.valueOf(label));
        rootFormClass.getElements().addAll(Arrays.asList(fields));
    }
    
    
    private void given(FormClass formClass) {
        formClasses.put(formClass.getId(), formClass);
    }
    
    private FormField field(String id, String label) {
        FormField field = new FormField(ResourceId.valueOf(id));
        field.setLabel(label);
        field.setType(TextType.INSTANCE);
        
        return field;
    }
    
    private FormClass formClass(String id, FormField... fields) {
        FormClass formClass = new FormClass(ResourceId.valueOf(id));
        formClass.setLabel(id);
        for (FormField field : fields) {
            formClass.addElement(field);
        }
        return formClass;
    }
    
    private FormField referenceField(String id, String label, FormClass... formClasses) {
        for (FormClass formClass : formClasses) {
            given(formClass);
        }
        
        String rangeIds[] = new String[formClasses.length];
        for (int i = 0; i < formClasses.length; i++) {
            rangeIds[i] = formClasses[i].getId().asString();
        }
        
        return referenceField(id, label, rangeIds);
    }
    
    private FormField referenceField(String id, String label, String... formClasses) {
        Set<ResourceId> range = new HashSet<>();
        for (String formClass : formClasses) {
            range.add(ResourceId.valueOf(formClass));
        }
        ReferenceType type = new ReferenceType();
        type.setCardinality(Cardinality.SINGLE);
        type.setRange(range);
        
        FormField field = new FormField(ResourceId.valueOf(id));
        field.setLabel(label);
        field.setType(type);
        
        return field;
    }
    

    private Collection<String> resolve(String exprString) {

        FormTree tree = tree();

        symbolTable = new NodeMatcher(tree);
        
        ExprNode expr = ExprParser.parse(exprString);
        Collection<NodeMatch> matches;
        if(expr instanceof SymbolExpr) {
            matches = symbolTable.resolveSymbol((SymbolExpr) expr);
        } else if(expr instanceof CompoundExpr) {
            matches = symbolTable.resolveCompoundExpr((CompoundExpr) expr);
        } else {
            throw new IllegalArgumentException(exprString);
        }
        
        // Create a string that we can match against easily
        List<String> strings = Lists.newArrayList();
        for (NodeMatch match : matches) {
            strings.add(match.toDebugString());
        }
        
        System.out.println("Resolved " + exprString + " => " + strings);
        
        return strings;
    }

    private FormTree tree() {
        if(rootFormClass == null) {
            throw new IllegalStateException("Root FormClass is unset");
        }

        FormClassProvider provider = new FormClassProvider() {

            @Override
            public FormClass getFormClass(ResourceId resourceId) {
                if(rootFormClass.getId().equals(resourceId)) {
                    return rootFormClass;
                }
                FormClass formClass = formClasses.get(resourceId);
                if(formClass == null) {
                    throw new IllegalArgumentException(resourceId.toString());
                }
                return formClass;
            }
        };
        FormTreeBuilder builder = new FormTreeBuilder(provider);
        return builder.queryTree(rootFormClass.getId());
    }


}