package org.activityinfo.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a separate builder class should be generated for the annotated
 * model.
 *
 * <p>Correctly immutable model classes should have:</p>
 * <ul>
 *     <li>A package-private no-args constructor</li>
 *     <li>Package-private fields.</li>
 *     <li>Public getters</li>
 * </ul>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoBuilder {

}
