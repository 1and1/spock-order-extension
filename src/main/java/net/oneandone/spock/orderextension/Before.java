package net.oneandone.spock.orderextension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Before defines that this feature will run before the feature of the annotation.
 * <blockquote>
 * <pre>{@code
 * @Before('feature2')
 * def feature1() {}
 * }</pre>
 * </blockquote>
 *
 * Execution order is: {@code feature1 -> feature2}
 *
 * @see Order
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Before {
    String value();
}

