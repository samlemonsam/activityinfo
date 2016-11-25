package org.activityinfo.core.shared.importing.schema;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.shared.importing.source.SourceColumn;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.source.SourceTable;
import org.activityinfo.i18n.shared.I18N;

import java.util.List;
import java.util.Set;

public abstract class SchemaImporter {


    protected final WarningTemplates templates;
    protected final Set<SafeHtml> warnings = Sets.newHashSet();
    protected final List<String> missingColumns = Lists.newArrayList();

    protected ProgressListener listener;
    protected SourceTable source;
    protected boolean fatalError;
    protected boolean hasSkippedAttributes = false;

    public void clearWarnings() {
        warnings.clear();
        hasSkippedAttributes = false;
        fatalError = false;
    }

    public abstract void persist(AsyncCallback<Void> callback);


    public interface WarningTemplates extends SafeHtmlTemplates {

        @Template("<li>Truncated <code>{0}<strike>{1}</strike></code> (Maximum length: {2} characters)</li>")
        SafeHtml truncatedValue(String retained, String truncated, int maxLen);

        @Template("<li>There is no LocationType named <code>{0}</code>, using default <code>{1}</code></li>")
        SafeHtml invalidLocationType(String name, String defaultValue);

        @Template("<li>Using default LocationType <code>{0}</code>, for activity <code>{1}</code></li>")
        SafeHtml defaultLocationType(String defaultLocationType, String activityName);

        @Template("<li>You didn't provide a column named <code>{0}</code>, " +
                  "so we'll default to <code>{1}</code>.</li>")
        SafeHtml missingColumn(String columnName, String defaultValue);

        @Template("<li>You didn't provide an attribute name in AttributeValue column. Row: <code>{0}</code>, " +
                "so we'll skip it.</li>")
        SafeHtml missingAttribueValue(int rowCount);
    }

    public interface ProgressListener {
        void submittingBatch(int batchNumber, int batchCount);
    }

    public class Column {
        private int index;
        private String name;
        private int maxLength;
        private String defaultValue;

        public Column(int index, String name, String defaultValue, int maxLength) {
            super();
            this.index = index;
            this.name = name;
            this.defaultValue = defaultValue;
            this.maxLength = maxLength;
        }

        public String get(SourceRow row) {
            if (index < 0) {
                return Strings.emptyToNull(defaultValue);
            }
            String value = row.getColumnValue(index);
            if (value.length() <= maxLength) {
                return Strings.emptyToNull(value.trim());

            } else {
                String retainedValue = value.substring(0, maxLength);
                String truncatedValue = value.substring(maxLength);
                warnings.add(templates.truncatedValue(retainedValue, truncatedValue, maxLength));
                return retainedValue;
            }
        }


        public String getOrThrow(SourceRow row) {
            String value = get(row);
            if(Strings.isNullOrEmpty(value)) {
                throw new UnableToParseRowException(I18N.MESSAGES.requiredFieldMissing(name, row.getRowIndex()+1));
            }
            return value;
        }

        public boolean isMissing() {
            return index < 0;
        }

        public String getName() {
            return name;
        }

        public int getMaxLength() {
            return maxLength;
        }

    }


    public SchemaImporter(WarningTemplates templates) {
        this.templates = templates;
    }

    public final void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    public final List<String> getMissingColumns() {
        return missingColumns;
    }

    protected final boolean isTruthy(String columnValue) {
        if (columnValue == null) {
            return false;
        }
        String loweredValue = columnValue.toLowerCase().trim();
        return loweredValue.equals("1") ||
                loweredValue.startsWith("t") || // true
                loweredValue.startsWith("y");   // yes

    }

    protected final int findColumnIndex(String name) {
        for (SourceColumn col : source.getColumns()) {
            if (col.getHeader().equalsIgnoreCase(name)) {
                return col.getIndex();
            }
        }
        return -1;
    }

    protected final Column findColumn(String name) {
        return findColumn(name, null, Integer.MAX_VALUE);
    }

    protected final Column findColumn(String name, String defaultValue) {
        return findColumn(name, defaultValue, Integer.MAX_VALUE);
    }

    protected final Column findColumn(String name, int maxLength) {
        return findColumn(name, null, maxLength);
    }

    protected final Column findColumn(String name, String defaultValue, int maxLength) {
        int col = findColumnIndex(name);
        if (col == -1) {
            if (defaultValue == null) {
                missingColumns.add(name);
            } else if(!Strings.isNullOrEmpty(defaultValue)) {
                warnings.add(SafeHtmlUtils.fromString(I18N.MESSAGES.missingWithDefault(name, defaultValue)));
            }
        }
        return new Column(col, col == -1 ? name : source.getColumnHeader(col), defaultValue, maxLength);
    }


    public final boolean parseColumns(SourceTable source) {
        this.source = source;
        this.source.parseAllRows();
        missingColumns.clear();
        findColumns();
        return missingColumns.isEmpty();
    }

    public final Set<SafeHtml> getWarnings() {
        return warnings;
    }

    protected abstract void findColumns();

    public abstract boolean processRows();
}
