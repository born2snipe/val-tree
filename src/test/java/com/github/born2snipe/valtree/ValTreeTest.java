package com.github.born2snipe.valtree;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    public void shouldNotWhipeOutExistingValuesWhenAddingAChildByQuery() {
        valTree.addChild("k1", "v1");
        valTree.addChild("k1.k2", "v2");

        assertEquals("v1", valTree.queryForString("k1"));
        assertEquals("v2", valTree.queryForString("k1.k2"));
    }

    @Test
    public void shouldAllowAddingAChildAtNestedPosition() {
        valTree.addChild("k1.k2.k3", "value");

        assertTrue(valTree.query("k1").isNull());
        assertTrue(valTree.query("k1.k2").isNull());
        assertEquals("value", valTree.queryForString("k1.k2.k3"));
    }

    @Test
    public void shouldAllowGettingTheChildren() {
        valTree.addChild("k1");
        valTree.addChild("k2");
        valTree.addChild("k3");

        Array<ValTree> children = valTree.getChildren();

        assertEquals("k1", children.get(0).getKey());
        assertEquals("k2", children.get(1).getKey());
        assertEquals("k3", children.get(2).getKey());
    }

    @Test
    public void shouldReturnAnEmptyArrayWhenTryingToGetSiblingsWhenThereIsNoParent() {
        assertEquals(0, valTree.getSiblings().size);
    }

    @Test
    public void shouldAllowGettingSiblings() {
        valTree.addChild("k1");
        valTree.addChild("k2");
        valTree.addChild("k3");

        Array<ValTree> siblings = valTree.getChild("k1").getSiblings();

        assertEquals("k1", siblings.get(0).getKey());
        assertEquals("k2", siblings.get(1).getKey());
        assertEquals("k3", siblings.get(2).getKey());
    }

    @Test
    public void shouldReturnNullQueryQueryingForValueAndTheKeyDoesNotExist() {
        assertNull(valTree.queryForString("does.not.exist"));
        assertNull(valTree.queryForInteger("does.not.exist"));
        assertNull(valTree.queryForFloat("does.not.exist"));
    }

    @Test
    public void shouldAllowQueryingForAFloatValue() {
        valTree.addChild("key", 10.1f);

        assertEquals(new Float(10.1), valTree.queryForFloat("key"));
    }

    @Test
    public void shouldAllowQueryingForAIntValue() {
        valTree.addChild("key", 1);

        assertEquals(new Integer(1), valTree.queryForInteger("key"));
    }

    @Test
    public void shouldAllowQueryingForAStringValue() {
        valTree.addChild("key", "value");

        assertEquals("value", valTree.queryForString("key"));
    }

    @Test
    public void shouldAllowAddingAChildWithoutAValue() {
        valTree.addChild("key");

        assertTrue(valTree.getChild("key").isNull());
    }

    @Test
    public void shouldAllowAddingChildrenWithFloatValues() {
        valTree.addChild("key", 100.0f);

        assertEquals("100.0", valTree.getChild("key").getString());
    }

    @Test
    public void shouldAllowAddingChildrenWithIntegerValues() {
        valTree.addChild("key", 100);

        assertEquals("100", valTree.getChild("key").getString());
    }

    @Test
    public void shouldNotCareIfTheLeadingWhitespaceIsNotMatchingBetweenSiblings() {
        valTree.parseData(" k1 1\n\tk2 2");

        assertEquals("1", valTree.getChild("k1").getString());
        assertEquals("2", valTree.getChild("k2").getString());
    }

    @Test
    public void shouldProperlyClearOutOldValuesWhenAttemptingToParseAnEmptyFileAfterAlreadyHavingValues() {
        valTree.parseData("key value");
        valTree.parseData("");

        assertNull(valTree.getKey());
        assertNull(valTree.getString());
        assertNull(valTree.getInteger());
        assertNull(valTree.getFloat());
        assertEquals(0, valTree.size());
    }

    @Test
    public void shouldAllowReusingAValTree() {
        valTree.parseData("key value");
        valTree.parseData("1 1");

        assertNull(valTree.getChild("key"));
        assertEquals("1", valTree.getChild("1").getString());
    }

    @Test
    public void shouldNotHaveAProblemWithWindowsLineEndings() {
        valTree.parseData("1 v1\r\n2 v2");

        assertEquals("v1", valTree.getChild("1").getString());
        assertEquals("v2", valTree.getChild("2").getString());
    }

    @Test
    public void shouldAllowWritingTheContentsToAnOutputStream() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        valTree.addChild("1", null);
        valTree.getChild("1").addChild("2", "2");
        valTree.addChild("3", "3");

        valTree.save(output);

        assertEquals("1\n 2 2\n3 3\n", new String(output.toByteArray()));
    }

    @Test
    public void shouldReturnNullWhenNothingIsFoundAtAnIndex() {
        assertNull(valTree.getIndex(2));
    }

    @Test
    public void shouldAllowAccessingChildrenByIndex() {
        valTree.addChild("1", "1");
        valTree.addChild("2", "2");

        assertEquals("1", valTree.getIndex(0).getString());
        assertEquals("2", valTree.getIndex(1).getString());
    }

    @Test
    public void shouldReturnNullWhenThereAreNoChildWhenTryingToGetTheFirstChild() {
        assertNull(valTree.getFirstChild());
    }

    @Test
    public void shouldAllowGettingTheFirstChild() {
        valTree.addChild("1", "1");
        valTree.addChild("2", "2");

        assertEquals("1", valTree.getFirstChild().getString());
    }

    @Test
    public void shouldAllowChangingTheKeyAndValueOfATree() {
        valTree.addChild("key", "value");

        valTree.getChild("key").set("key-1", "value-1");

        assertNull(valTree.getChild("key"));
        assertEquals("value-1", valTree.getChild("key-1").getString());
    }

    @Test
    public void shouldAllowTheTreeToBeIterable() {
        valTree.addChild("1", "1");
        valTree.addChild("2", "2");
        valTree.addChild("3", "3");

        ArrayList<String> actuals = new ArrayList<String>();
        for (ValTree tree : valTree) {
            actuals.add(tree.getKey());
        }

        assertEquals(Arrays.asList("1", "2", "3"), actuals);
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

        valTree.parse(new FileHandle(tmpFile));

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
