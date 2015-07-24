package io.github.jcarvalho;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objenesis.Objenesis;

@RunWith(JUnit4.class)
public class MagicObjenesisTest {

    @Test
    public void testMagicObjenesis() {
        Objenesis objenesis = new MagicObjenesis();
        TestType type = objenesis.newInstance(TestType.class);
        Assert.assertFalse(type.wasInvoked());
    }

}
