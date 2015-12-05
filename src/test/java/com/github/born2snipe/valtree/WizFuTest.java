package com.github.born2snipe.valtree;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WizFuTest {
    private ValTree valTree;

    @Before
    public void setUp() throws Exception {
        valTree = new ValTree();
    }

    @Test
    public void shouldHandleExampleFile() {
        valTree.parse(testFile("test2.txt"));

        ValTree key1 = valTree.getChild("key1");
        assertEquals("val1", key1.getString());
        assertEquals("test", key1.getChild("key2").getString());
        assertEquals("should have no problem with this indentation", key1.getChild("key2").getChild("key3").getChild("key4-1").getChild("key5-1").getString());
    }

    @Test
    public void shouldHandleExampleFileFromReadMe() {
        valTree.parse(testFile("test.txt"));

        assertTrue(valTree.getChild("a").isNull());
        assertTrue(valTree.getChild("a").getChild("b").isNull());
        assertTrue(valTree.getChild("a").getChild("b").getChild("c").isNull());
        assertEquals("1", valTree.getChild("a").getChild("b").getChild("d").getString());
        assertEquals("2.01", valTree.getChild("a").getChild("b").getChild("e").getString());
        assertEquals("something", valTree.getChild("a").getChild("b").getChild("f").getString());
        assertTrue(valTree.getChild("g-is-long").isNull());
        assertEquals("h is a cool letter", valTree.getChild("g-is-long").getChild("h").getString());
        assertTrue(valTree.getChild("g-is-long").getChild("i").isNull());
    }

    private InputStream testFile(String filename) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    }
}
