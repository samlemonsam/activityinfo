package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;

import java.util.*;

/**
 * The key table is a large matrix with all records from the referenced form in the
 * rows, and all keys in the columns. For the "classic" case of a village lookup, the table would look like:
 *
 * <pre>
 *     id    | k1     | k2              | k3            |
 *     ------+--------+-----------------+---------------+
 *     s014  | Babu   | Rutshuru        | Nord Kivu     |
 *     s323  | Nuru   | Beni            | Nord Kivu     |
 *     ..
 *     etc
 * </pre>
 *
 * Where k1 = the village name, k2 = the territory name, and k3 = province name.
 *
 */
class KeyMatrix {

    private static final String ID_COLUMN = "id";

    private final LookupKeySet lookupKeySet;
    private final Observable<ColumnSet> keyColumns;
    private final ResourceId formId;

    public KeyMatrix(FormSource formSource, LookupKeySet lookupKeySet) {
        this.formId = lookupKeySet.getLeafKeys().get(0).getFormId();
        this.lookupKeySet = lookupKeySet;

        keyColumns = formSource.query(keyMatrixQuery());
    }

    /**
     * Composes a query for all the records in the referenced form along with the full
     * hierarchy of keys.
     */
    private QueryModel keyMatrixQuery() {

        LookupKey leafKey = lookupKeySet.getLeafKeys().get(0);
        Map<LookupKey, ExprNode> keyFormulas = leafKey.getKeyFormulas();

        QueryModel queryModel = new QueryModel(formId);
        queryModel.selectResourceId().as(ID_COLUMN);
        for (Map.Entry<LookupKey, ExprNode> entry : keyFormulas.entrySet()) {
            queryModel.selectExpr(entry.getValue()).as(keyColumn(entry.getKey()));
        }
        return queryModel;
    }

    /**
     * Maps a {@link LookupKey} to its column identifier in {@link #keyColumns}
     */
    private String keyColumn(LookupKey lookupKey) {
        return "k" + lookupKey.getKeyIndex();
    }

    /**
     * For a given (observable) record reference, find the string labels for each of its
     * {@link LookupKey}s.
     *
     */
    public Observable<Map<LookupKey, String>> findKeyLabels(Observable<RecordRef> recordRef) {
        return Observable.transform(keyColumns, recordRef, (columns, ref) -> {
            Map<LookupKey, String> labels = new HashMap<>();
            int rowIndex = findRowIndex(columns, ref);
            if(rowIndex != -1) {
                for (LookupKey lookupKey : lookupKeySet.getLookupKeys()) {
                    ColumnView keyColumn = columns.getColumnView(keyColumn(lookupKey));
                    labels.put(lookupKey, keyColumn.getString(rowIndex));
                }
            }
            return labels;
        });
    }

    /**
     * Finds the index of the row in the matrix for the given Record {@code ref}.
     */
    private int findRowIndex(ColumnSet keyColumns, RecordRef ref) {
        String recordId = ref.getRecordId().asString();
        ColumnView id = keyColumns.getColumnView("id");

        for (int i = 0; i < id.numRows(); i++) {
            if(recordId.equals(id.getString(i))) {
                return i;
            }
        }
        return -1;
    }

    private Observable<ColumnView> getKeyColumn(LookupKey lookupKey) {
        return keyColumns.transform(columns -> columns.getColumnView(keyColumn(lookupKey)));
    }

    private Observable<ColumnView> getIdColumn() {
        return keyColumns.transform(columns -> columns.getColumnView(ID_COLUMN));
    }

    /**
     * Compute the {@code BitSet} of rows whose value in the given {@code column} is equal to the given
     * {@code keySelection}. If the {@code keySelection} is absent, an empty {@code BitSet} is returned.
     */
    public Observable<BitSet> getMatchingRows(LookupKey key, Observable<Optional<String>> keySelection) {
        return Observable.transform(getKeyColumn(key), keySelection, this::match);
    }

    /**
     * Compute the {@code BitSet} of rows whose value in the given {@code column} is equal to the given
     * {@code selection}. If the {@code selection} is absent, an empty {@code BitSet} is returned.
     */
    private BitSet match(ColumnView column, Optional<String> selection) {
        BitSet bitSet = new BitSet();
        if (selection.isPresent()) {
            String stringValue = selection.get();
            for (int i = 0; i < column.numRows(); i++) {
                bitSet.set(i, stringValue.equals(column.getString(i)));
            }
        }
        return bitSet;
    }

    /**
     * Finds all distinct key values for the given {@code lookupKey}.
     */
    public Observable<List<String>> getDistinctKeys(LookupKey lookupKey) {
        return getKeyColumn(lookupKey).transform(column -> {
            Set<String> set = new HashSet<>();
            for (int i = 0; i < column.numRows(); i++) {
                set.add(column.getString(i));
            }
            return sorted(set);
        });
    }

    /**
     * Finds the distinct key values for the given {@code lookupKey} in the rows matching
     * the given {@code filter}.
     */
    public Observable<List<String>> getDistinctKeys(LookupKey lookupKey, Observable<BitSet> filter) {
        return Observable.transform(getKeyColumn(lookupKey), filter, (column, matching) -> {
            if(matching.isEmpty()) {
                return Collections.emptyList();
            }

            Set<String> set = new HashSet<>();
            for (int i = 0; i < column.numRows(); i++) {
                if(matching.get(i)) {
                    set.add(column.getString(i));
                }
            }
            return sorted(set);
        });
    }


    /**
     * Finds the (first) RecordRef which matches the given {@code filter}.
     */
    public Observable<Optional<RecordRef>> getMatchingRecordRef(Observable<BitSet> filter) {
        return Observable.transform(getIdColumn(), filter, (column, f) -> {
            int rowIndex = f.nextSetBit(0);
            if(rowIndex == -1) {
                return Optional.absent();
            } else {
                return Optional.of(new RecordRef(formId, ResourceId.valueOf(column.getString(rowIndex))));
            }
        });
    }

    private List<String> sorted(Set<String> set) {
        List<String> sortedList = Lists.newArrayList(set);
        Collections.sort(sortedList);
        return sortedList;
    }

}