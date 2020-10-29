package net.oneandone.spock.orderextension

import org.intellij.lang.annotations.Language
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.RunListener

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

class OrderExtensionTest extends Specification {
    /** captures executed features to see order */
    static class ExecutionListener extends RunListener {
        List<String> finished = []
        List<String> ignored = []

        @Override
        void testFinished(Description description) throws Exception {
            finished << description.methodName
        }

        @Override
        void testIgnored(Description description) throws Exception {
            ignored << description.methodName
        }
    }

    EmbeddedSpecRunner runner = new EmbeddedSpecRunner(throwFailure: false)

    @Shared
    ExecutionListener listener

    Result runOrderSpecBody(@Language(value = 'Groovy', prefix = '@Order class ASpec extends spock.lang.Specification { ', suffix = '\n }')
                                    String source, List<String> skip = []) {
        runner.addClassImport(Order)
        runner.addClassImport(After)
        runner.addClassImport(Before)
        runner.addClassImport(Ignore)

        listener = new ExecutionListener()
        runner.listeners << listener

        runner.runWithImports """
        abstract class BaseSpec extends  Specification {
            def "base1"() { expect: true }
            def "base2"() { expect: true }
            def "base3"() { expect: true }
        }
        
        @Order(skip = [${skip.collect { "'$it'" }.join(",")}])
        class ASpec extends BaseSpec { 
            ${source.trim()} 
        }
        """
    }

    def "@After fails for missing feature"() {
        when:
        runOrderSpecBody"""
        @After("missing")
        def "feature1"() { expect: true }
        """

        then:
        IllegalArgumentException e = thrown()
        e.message == 'missing is not a valid feature name or skipped'
    }

    def "@Before fails for missing feature"() {
        when:
        runOrderSpecBody"""
        @Before("missing")
        def "feature1"() { expect: true }
        """

        then:
        IllegalArgumentException e = thrown()
        e.message == 'missing is not a valid feature name or skipped'
    }

    def "@Before must lead to defined order"() {
        when:
        runOrderSpecBody"""
        @Before("base2")
        def "feature1"() { expect: true }
        
        @Before("base1")
        def "feature2"() { expect: true }
        """

        then:
        IllegalArgumentException e = thrown()
        e.message.startsWith('Unclear execution order.')
    }

    def "@After must lead to defined order"() {
        when:
        runOrderSpecBody"""
        @After("base2")
        def "feature1"() { expect: true }
        
        @After("base1")
        def "feature2"() { expect: true }
        """

        then:
        IllegalArgumentException e = thrown()
        e.message.startsWith('Unclear execution order.')
    }

    def "Features in Order specs must be annotated"() {
        when:
        runOrderSpecBody"""
        def "feature1"() { expect: true }
        """

        then:
        IllegalArgumentException e = thrown()
        e.message == "Found not annotated feature 'feature1' in @Order spec. This is only between @After and @Before features allowed."
    }

    def "child feature can be executed first"() {
        when:
        Result result = runOrderSpecBody"""
        @Before("base1")
        def "feature1"() { expect: true }
        """

        then:
        result.runCount == 4
        listener.finished == ['feature1', 'base1', 'base2', 'base3']
    }

    def "child feature can be executed last"() {
        when:
        Result result = runOrderSpecBody"""
        @After("base3")
        def "feature1"() { expect: true }
        """

        then:
        result.runCount == 4
        listener.finished == ['base1', 'base2', 'base3', 'feature1']
    }

    def "child feature can be executed between"() {
        when:
        Result result = runOrderSpecBody"""
        @After("base2")
        @Before("base3")
        def "feature1"() { expect: true }
        """

        then:
        result.runCount == 4
        listener.finished == ['base1', 'base2', 'feature1', 'base3']
    }

    def "multiple child features can be executed between"() {
        when:
        Result result = runOrderSpecBody"""
        @After("base2")
        def "feature1"() { expect: true }
        
        def "feature2"() { expect: true }
        
        @Before("base3")
        def "feature3"() { expect: true }
        """

        then:
        result.runCount == 6
        listener.finished == ['base1', 'base2', 'feature1', 'feature2', 'feature3', 'base3']
    }

    def "features after a failing one are skipped"() {
        when:
        Result result = runOrderSpecBody"""
        @After("base2")
        def "feature1"() { expect: true }
        
        def "feature2"() { expect: false }
        
        @Before("base3")
        def "feature3"() { expect: true }
        """

        then:
        result.runCount == 4
        result.failureCount == 1
        result.ignoreCount == 2

        listener.finished == ['base1', 'base2', 'feature1', 'feature2']
        listener.ignored == ['feature3', 'base3']
    }

    def "@Order can be combined with @Ignore"() {
        when:
        Result result = runOrderSpecBody"""
        @After("base2")
        def "feature1"() { expect: true }
        
        @Ignore
        def "feature2"() { expect: true }
        
        @Before("base3")
        def "feature3"() { expect: true }
        """

        then:
        result.runCount == 5
        result.ignoreCount == 1

        listener.finished == ['base1', 'base2', 'feature1', 'feature3', 'base3']
        listener.ignored == ['feature2']
    }

    def "base features can be skipped"() {
        when:
        Result result = runOrderSpecBody"""
        @After("base2")
        def "feature1"() { expect: true }
        def "feature2"() { expect: true }
        """, ['base1', 'base3']

        then:
        result.runCount == 3
        result.ignoreCount == 2

        listener.finished == ['base2', 'feature1', 'feature2']
        listener.ignored == ['base1', 'base3']
    }

}