/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.spock.orderextension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spockframework.runtime.extension.ExtensionAnnotation;

/**
 * Indicates that a spec's feature methods should be run sequentially
 * in their declared order like {@code @Stepwise} but in addition to that
 * effects the complete spec hierarchy and gives the opportunity to define
 * dependencies between child and parent specs with the annotations:
 * {@code @After, @Before, @Between}.
 * Features of the child spec must be annotated so that order in relation
 * to the base specs is in a defined state.
 * <blockquote>
 * <pre>{@code
 * @Order(skip = ['baseFeature1', 'baseFeature2'])
 * class Test extends Specification { }
 * }</pre>
 * </blockquote>
 *
 * Execution order is: {@code feature1 -> feature2}
 *
 * @see After
 * @see Before
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(OrderExtension.class)
@interface Order {
    /**
     * The listed features of the base specs will be skipped.
     */
    String[] skip() default {};
}

