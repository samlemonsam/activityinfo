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
