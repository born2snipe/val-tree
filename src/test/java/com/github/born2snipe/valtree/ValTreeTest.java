package com.github.born2snipe.valtree;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class ValTreeTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private ValTree valTree;

    @Before
    public void setUp() throws Exception {
        valTree = new ValTree();
    }

    @Test
    public void shouldAllowAddingChildrenTrees() {
        valTree.addChild(new ValTree("key", "value"));

        assertEquals("value", valTree.getChild("key").getString());
        assertEquals(1, valTree.size());
    }

    @Test
    public void shouldAllowAddingChildren() {
        valTree.addChild("key", "value");

        assertEquals("value", valTree.getChild("key").getString());
        assertEquals(1, valTree.size());
    }

    @Test
    public void shouldReturnNullIfNothingMatchesTheQuery() {
        valTree.parseData("key value");

        assertNull(valTree.query("does.not.exist"));
    }

    @Test
    public void shouldAllowQuerying() {
        valTree.parseData("key value");

        assertEquals("value", valTree.query("key").getString());
    }

    @Test
    public void shouldAllowDetermineIfThereAreChildrenPresent() {
        valTree.parseData("key-1\n key-2");

        assertTrue(valTree.getChild("key-1").hasChildren());
        assertFalse(valTree.getChild("key-1").getChild("key-2").hasChildren());
    }

    @Test
    public void shouldAllowClearingEverythingBelowAChild() {
        valTree.parseData("key 1\n key-1 value-1");
        ValTree child = valTree.getChild("key");
        child.clear();


        assertEquals(0, child.size());
        assertNull(child.getChild("key-1"));
        assertNull(child.getKey());
        assertNull(child.getString());
        assertNull(child.getInteger());
        assertNull(child.getFloat());
    }

    @Test
    public void shouldAllowClearingEverything() {
        valTree.parseData("key value");
        valTree.clear();

        assertEquals(0, valTree.size());
        assertNull(valTree.getChild("key"));
        assertNull(valTree.getKey());
        assertNull(valTree.getString());
    }

    @Test
    public void shouldSupportReadingFromAFile() throws IOException {
        File tmpFile = tmp.newFile();
        IOUtils.write("key value", new FileOutputStream(tmpFile));

        valTree.parse(tmpFile);

        assertEquals("value", valTree.getChild("key").getString());
    }

    @Test
    public void shouldIgnoreTrailingComments() {
        valTree.parseData("key value // ignore me");

        assertEquals("value", valTree.getChild("key").getString());
        assertEquals(1, valTree.size());
    }

    @Test
    public void shouldIgnoreCommentLines() {
        valTree.parseData("// ignore me\n\t  // ignore me too\nkey value");

        assertEquals("value", valTree.getChild("key").getString());
        assertEquals(1, valTree.size());
    }

    @Test
    public void shouldAllowALotOfNestingOfKeys() {
        valTree.parseData(
                "a\n" +
                        " b 1");

        assertEquals("1", valTree.getChild("a").getChild("b").getString());
    }

    @Test
    public void shouldAllowSpacesInValues() {
        valTree.parseData("key value can have spaces");

        assertEquals("value can have spaces", valTree.getChild("key").getString());
    }

    @Test
    public void shouldAllowProvidingMultipleKeysAndValues() {
        valTree.parseData("k1 v1\nk2 v2");

        assertEquals("v1", valTree.getChild("k1").getString());
        assertEquals("v2", valTree.getChild("k2").getString());
    }

    @Test
    public void shouldNotCareIfWeUseTabsToDelimitAKeyAndValue() {
        valTree.parseData("key\tvalue");
        assertEquals("key", valTree.getChild("key").getKey());
        assertEquals("value", valTree.getChild("key").getString());
    }

    @Test
    public void shouldAllowSpacesBetweenAKeyAndValue() {
        valTree.parseData("key value");
        assertEquals("value", valTree.getChild("key").getString());
    }

    @Test
    public void shouldAllowManySpacesBetweenAKeyAndValue() {
        valTree.parseData("key        value");
        assertEquals("value", valTree.getChild("key").getString());
    }

    @Test
    public void shouldAllowLeadingWhiteSpaceBeforeAKey() {
        valTree.parseData("  \t   key        value");
        assertEquals("value", valTree.getChild("key").getString());
    }

    @Test
    public void shouldAllowGettingAnIntegerValue() {
        valTree.parseData("key 111");
        assertEquals(new Integer(111), valTree.getChild("key").getInteger());
    }

    @Test
    public void shouldAllowGettingAFloatValue() {
        valTree.parseData("key 2.33");
        assertEquals(2.33f, valTree.getChild("key").getFloat(), 0.1);
    }
}
