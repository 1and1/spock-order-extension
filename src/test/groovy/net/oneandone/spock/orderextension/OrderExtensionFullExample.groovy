package net.oneandone.spock.orderextension


import spock.lang.Specification

@Order(skip = 'base 5')
class OrderExtensionFullExampleTest extends Base {
    @After("base 1")
    def "child 1"() {
        expect: true
    }

    def "child 2"() {
        expect: true
    }

    @Before("base 2")
    def "child 3"() {
        expect: true
    }

    @After("base 3") @Before("base 4")
    def "child 4"() {
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