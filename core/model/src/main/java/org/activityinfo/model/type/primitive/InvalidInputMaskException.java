package org.activityinfo.model.type.primitive;

/**
 * Thrown when
 */
@SuppressWarnings("GwtInconsistentSerializableClass")
public class InvalidInputMaskException extends RuntimeException {


    private final String mask;
    private final int characterIndex;

    public InvalidInputMaskException(String mask, int characterIndex) {
        super("Invalid input mask: " + mask.charAt(characterIndex));
        this.mask = mask;
        this.characterIndex = characterIndex;
    }
}
