package com.github.born2snipe.valtree;

import com.github.born2snipe.valtree.converter.EnumConverter;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ExampleTest {
    @Test
    public void exampleUsage() {
        // configure custom converter
        ValTree.registerConverter(Abilities.class, new EnumConverter(Abilities.class));

        // read your file
        ValTree valTree = new ValTree();
        valTree.parse(testFile("example.txt"));

        // query the results
        assertEquals(new Integer(100), valTree.queryFor("orc.grunt.health", Integer.class));
        assertEquals(new Integer(50), valTree.queryFor("orc.wizard.health", Integer.class));
        assertEquals(Abilities.FLATULENCE, valTree.queryFor("orc.grunt.ability", Abilities.class));
    }

    private InputStream testFile(String filename) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    }

    public enum Abilities {
        MIND_READING, EXPLOSIVE_ARROWS, MIND_CONTROL, FLATULENCE
    }
}
