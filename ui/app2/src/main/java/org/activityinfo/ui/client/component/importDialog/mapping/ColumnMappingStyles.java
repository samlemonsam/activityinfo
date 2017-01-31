package org.activityinfo.ui.client.component.importDialog.mapping;

import com.bedatadriven.rebar.style.client.Source;
import com.bedatadriven.rebar.style.client.Stylesheet;
import com.google.gwt.core.client.GWT;

/**
 * Placeholder until we have a better way to integrate LESS and gwt styles
 */
@Source("ColumnMapping.less")
public interface ColumnMappingStyles extends Stylesheet {

    public static final ColumnMappingStyles INSTANCE = GWT.create(ColumnMappingStyles.class);

    @ClassName("cm-datagrid")
    String grid();

    @ClassName("source-column")
    String sourceColumnHeader();

    @ClassName("mapping")
    String mappingHeader();

    @ClassName("state-ignored")
    String stateIgnored();

    @ClassName("state-bound")
    String stateBound();

    @ClassName("state-unset")
    String stateUnset();

    @ClassName("selected")
    String selected();

    @ClassName("cm-field-selector")
    String fieldSelector();

    @ClassName("incomplete")
    String incomplete();

    @ClassName("type-matched")
    String typeMatched();

    @ClassName("type-not-matched")
    String typeNotMatched();

}
