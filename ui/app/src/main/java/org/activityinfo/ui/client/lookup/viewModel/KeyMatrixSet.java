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
package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;

import java.util.*;
import java.util.logging.Logger;

/**
 * A set of {@link KeyMatrix}, one for each form in the reference field's range.
 *
 * For each of the possibly referenced forms, we need a separate matrix.
 *
 * For example, if a field can reference _either_ a PROVINCE, TERRITORY, or SECTOR,
 * we need three matrices:
 * <pre>
 *
 * PROVINCE                       TERRITORY
 *
 * id | k0: Province.Name |      id | k0: Province.Name | k1: Territory.Name |
 * ---+-------------------+      ---+-------------------+--------------------+
 * p0 | Nord Kivu         |      t0 | Nord Kivu         | Beni               |
 * p1 | Sud Kivu          |      t1 | Nord Kivu         | Goma               |
 * p2 | Katanga           |      t2 | Sud Kivu          | Penekusu           |
 *
 * SECTOR
 *
 * id | k0: Province.Name | k1: Territory.Name | k2: Sector.Name |
 * ---+-------------------+--------------------+-----------------+
 * s0 | Nord Kivu         | Beni               | Bungulu         |
 * s1 | Nord Kivu         | Beni               | Ruwenzori       |
 * s2 | Sud Kivu          | Penekusu           | Misombo         |
 *
 * </pre>
 */
public class KeyMatrixSet {

    private static final Logger LOGGER = Logger.getLogger(KeyMatrixSet.class.getName());

    /**
     * Maps a form by id to its corresponding key matrix.
     */
    private final Map<ResourceId, KeyMatrix> map = new HashMap<>();
    private final ReferenceType referenceType;
    private final LookupKeySet lookupKeySet;

    public KeyMatrixSet(
            FormSource formSource,
            ReferenceType referenceType,
            LookupKeySet lookupKeySet,
            Observable<Optional<FormulaNode>> filter) {
        this.referenceType = referenceType;
        this.lookupKeySet = lookupKeySet;

        for (ResourceId referencedFormId : referenceType.getRange()) {
            if (!lookupKeySet.hasLeafKey(referencedFormId)) {
                continue;
            }
            LookupKey leafKey = lookupKeySet.getLeafKey(referencedFormId);
            KeyMatrix keyMatrix = new KeyMatrix(formSource, lookupKeySet, referencedFormId, leafKey, filter);
            map.put(referencedFormId, keyMatrix);
        }
    }

    public Observable<Map<LookupKey, String>> findKeyLabels(Observable<Set<RecordRef>> initialSelection) {
        return initialSelection.join(refs -> {
            List<Observable<Map<LookupKey, String>>> labels = new ArrayList<>();
            for (RecordRef ref : refs) {
                KeyMatrix matrix = map.get(ref.getFormId());
                if(matrix == null) {
                    LOGGER.warning("Invalid selection for field. " + ref.getFormId() + " not in " + referenceType.getRange());
                } else {
                    labels.add(matrix.findKeyLabels(ref));
                }
            }
            return Observable.flatten(labels).transform(maps -> {
                Map<LookupKey, String> merged = new HashMap<>();
                for (Map<LookupKey, String> map : maps) {
                    merged.putAll(map);
                }
                return merged;
            });
        });
    }

    public LookupKeySet getLookupKeySet() {
        return lookupKeySet;
    }

    public Collection<KeyMatrix> getMatrices() {
        return map.values();
    }
}
