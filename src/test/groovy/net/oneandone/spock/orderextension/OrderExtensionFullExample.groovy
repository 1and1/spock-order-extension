package net.oneandone.spock.orderextension

import spock.lang.Specification

@Order(skip = 'base 5')
class OrderExtensionFullExampleTest extends Base {
    @After("base 1")
    def "step 1"() {
        expect: true
    }

    def "step 2"() {
        expect: true
    }

    @Before("base 2")
    def "step 3"() {
        expect: true
    }

    @Between(["base 3", "base 4"])
    def "step 4"() {
        expect: true
    }
}

class Base extends Specification {
    def "base 1"() {
        expect: true
    }

    def "base 2"() {
        expect: true
    }

    def "base 3"() {
        expect: true
    }

    def "base 4"() {
        expect: true
    }

    def "base 5"() {
        expect: true
    }
}