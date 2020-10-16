package net.oneandone.spock.orderextension

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 *
 * Between defines that this feature will run between the features of the annotation.
 * <blockquote>
 * <pre>{@code
 * @Between(['feature1', 'feature3'])
 * def feature2() {}
 * }</pre>
 * </blockquote>
 *
 * Execution order is: {@code feature1 -> feature2 -> feature3}
 *
 * @see Order
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Between {
    String[] value()
}

