package io.github.jcarvalho;

import org.objenesis.ObjenesisBase;

public class MagicObjenesis extends ObjenesisBase {

    public MagicObjenesis() {
        super(new MagicInstantiatorStrategy(Thread.currentThread().getContextClassLoader()));
    }

    public MagicObjenesis(ClassLoader classLoader) {
        super(new MagicInstantiatorStrategy(classLoader));
    }

}
