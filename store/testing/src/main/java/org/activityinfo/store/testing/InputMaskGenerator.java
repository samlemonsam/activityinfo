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

import com.google.common.base.Supplier;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.InputMask;
import org.activityinfo.model.type.primitive.TextValue;

import java.util.*;

public class InputMaskGenerator implements Supplier<FieldValue> {

    private final InputMask inputMask;
    private double probabilityMissing;
    private double probabilityDuplicate;
    private final Random random = new Random(335L);
    private final List<TextValue> previous = new ArrayList<>();

    public InputMaskGenerator(InputMask inputMask, double probabilityMissing, double probabilityDuplicate) {
        this.inputMask = inputMask;
        this.probabilityMissing = probabilityMissing;
        this.probabilityDuplicate = probabilityDuplicate;
    }

    @Override
    public FieldValue get() {

        if(random.nextDouble() < probabilityMissing) {
            return null;
        }

        if(random.nextDouble() < probabilityDuplicate) {
            return previous.get(random.nextInt(previous.size()));
        }

        StringBuilder s = new StringBuilder();
        for (InputMask.Atom atom : inputMask.getAtoms()) {
            atom.appendRandom(random, s);
        }

        TextValue textValue = TextValue.valueOf(s.toString());

        previous.add(textValue);

        return textValue;
    }
}
