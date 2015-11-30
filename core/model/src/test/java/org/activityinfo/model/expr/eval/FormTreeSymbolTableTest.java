package org.activityinfo.model.expr.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.diagnostic.AmbiguousSymbolException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Test;

import java.util.*;

import static org.activityinfo.model.expr.eval.FormTreeSymbolTable.toQueryPath;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class FormTreeSymbolTableTest {

    private FormClass rootFormClass;
    private Map<ResourceId, FormClass> formClasses = Maps.newHashMap();
    private FormTreeSymbolTable symbolTable;

    
    @Test
    public void queryPath() {
        
        assertThat(toQueryPath((CompoundExpr) ExprParser.parse("Location.Name")),
                contains("Location", "Name"));

        assertThat(toQueryPath((CompoundExpr) ExprParser.parse("Location.Province.Name")),
                contains("Location", "Province", "Name"));

    }
    
    @Test
    public void basicForm() {
        givenRootForm("Contact Form", field("A", "Name"), field("B", "Phone Number"));
        
        assertThat(resolve("A"), contains(path("A")));
        assertThat(resolve("Name"), contains(path("A")));
        assertThat(resolve("[Phone Number]"), contains(path("B")));
        assertThat(resolve("[phone number]"), contains(path("B")));

    }
    
    @Test(expected = AmbiguousSymbolException.class)
    public void ambiguousRootField() {
        givenRootForm("Contact Form", field("A", "Name"), field("B", "name"));

        // Should always be able to resolve by ID
        assertThat(resolve("A"), contains(path("A")));
 
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
        
        
        assertThat(resolve("Name"), contains(path("A")));
        assertThat(resolve("Location.Name"), contains(path("B", "LA")));
        assertThat(resolve("Population"), contains(path("B", "LB")));
    }
    
    @Test
    public void childUnionMatch() {
        givenRootForm("Project Site",
                field("A", "Name"),
                referenceField("B", "Location",
                        formClass("Village",
                                field("VA", "Name"),
                                field("VB", "Population")),
                        formClass("Health Center",
                                field("HA", "Name"))));
        prettyPrintTree();
        
        assertThat(resolve("Name"), contains(path("A")));
        assertThat(resolve("Location.Name"), containsInAnyOrder(path("B", "VA"), path("B", "HA")));
        assertThat(resolve("Location.Population"), contains(path("B", "VB")));
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
        
        
        assertThat(resolve("Name"), contains(path("SN")));
        assertThat(resolve("Province.Name"), containsInAnyOrder(
                path("SL", "TP", "PN"),
                path("SL", "PN")));
        
    }
    
    
    private void prettyPrintTree() {
        FormTreePrettyPrinter prettyPrinter = new FormTreePrettyPrinter();
        prettyPrinter.printTree(tree());
    }

    private FieldPath path(String... path) {
        ResourceId ids[] = new ResourceId[path.length];
        for (int i = 0; i < path.length; i++) {
            ids[i] = ResourceId.valueOf(path[i]);
        }
        return new FieldPath(ids);
    }


    private void givenRootForm(String label, FormField... fields) {
        if(rootFormClass != null) {
            throw new IllegalStateException("Root Form Class already set");
        }
        
        rootFormClass = new FormClass(ResourceId.generateId());
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
    

    private Collection<FieldPath> resolve(String exprString) {

        FormTree tree = tree();

        symbolTable = new FormTreeSymbolTable(tree);
        
        ExprNode expr = ExprParser.parse(exprString);
        Collection<FormTree.Node> nodes;
        if(expr instanceof SymbolExpr) {
            nodes = symbolTable.resolveSymbol((SymbolExpr) expr);
        } else if(expr instanceof CompoundExpr) {
            nodes = symbolTable.resolveCompoundExpr((CompoundExpr) expr);
        } else {
            throw new IllegalArgumentException(exprString);
        }
        List<FieldPath> fieldPaths = Lists.newArrayList();
        for (FormTree.Node node : nodes) {
            fieldPaths.add(node.getPath());
        }
        System.out.println("Resolved " + exprString + " to " + fieldPaths);

        return fieldPaths;
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