package io.github.jcarvalho;

public class TestType {

    private final boolean invoked;

    public TestType() {
        this.invoked = true;
    }

    public boolean wasInvoked() {
        return invoked;
    }

}
