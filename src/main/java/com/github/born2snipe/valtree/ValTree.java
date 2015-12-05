package com.github.born2snipe.valtree;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class ValTree implements Iterable<ValTree> {
    private ObjectMap<String, ValTree> children = new OrderedMap<String, ValTree>();
    private ValTree parent;
    private String key;
    private String value;
    private Float floatValue;
    private Integer intValue;
    private int depth;

    public ValTree() {
    }

    public ValTree(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void parse(FileHandle file) {
        InputStream input = null;
        try {
            input = file.read(1024);
            parse(input);
        } finally {
            close(input);
        }
    }

    public void parse(InputStream inputStream) {
        parseData(readEntirely(inputStream));
    }

    public void parseData(String content) {
        String[] lines = content.split("\n");
        Array<ValTree> parentStack = new Array<ValTree>();
        parentStack.add(this);

        for (String line : lines) {
            line = line.replaceAll("//.+", "");

            if (line.trim().length() == 0) {
                continue;
            }

            ValTree child = new ValTree();
            child.parseLine(line);
            int childDepth = child.depth;

            while (parentStack.size > 1) {
                ValTree currentParent = parentStack.peek();
                if (childDepth <= currentParent.depth) {
                    parentStack.pop();
                } else {
                    break;
                }
            }

            ValTree parent = parentStack.peek();
            child.parent = parent;
            parent.addChild(child);
            parentStack.add(child);
        }
    }

    public ValTree getChild(String key) {
        return children.get(key);
    }

    public String getString() {
        return value;
    }

    public Integer getInteger() {
        if (intValue == null && value != null) {
            intValue = Integer.valueOf(value);
        }
        return intValue;
    }

    public Float getFloat() {
        if (floatValue == null && value != null) {
            floatValue = Float.valueOf(value);
        }
        return floatValue;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output);
        log(printStream);
        return new String(output.toByteArray());
    }

    public boolean isNull() {
        return value == null;
    }

    public int size() {
        return children.size;
    }

    public void clear() {
        key = null;
        value = null;
        floatValue = null;
        intValue = null;
        children.clear();
    }

    public boolean hasChildren() {
        return children.size > 0;
    }

    public ValTree query(String query) {
        String[] queryKeys = query.split("\\.");
        ValTree current = this;
        for (String queryKey : queryKeys) {
            if (!current.children.containsKey(queryKey)) {
                return null;
            }
            current = current.children.get(queryKey);
        }

        return current;
    }

    public void addChild(String key, String value) {
        addChild(new ValTree(key, value));
    }

    public void addChild(ValTree tree) {
        tree.parent = this;
        children.put(tree.key, tree);
    }

    @Override
    public Iterator<ValTree> iterator() {
        return children.values().iterator();
    }

    private String readEntirely(InputStream inputStream) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                output.write(buf, 0, len);
            }
            return new String(output.toByteArray());
        } catch (IOException e) {
            throw new ProblemReadingFileException(e);
        } finally {
            close(inputStream);
        }
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new ProblemReadingFileException(e);
            }
        }
    }

    private void parseLine(String line) {
        depth = determineDepthOf(line);
        String sanitized = line.trim().replaceFirst("\\s+", " ");
        int firstSpaceIndex = sanitized.indexOf(' ');
        if (firstSpaceIndex == -1) {
            key = sanitized;
        } else {
            key = sanitized.substring(0, firstSpaceIndex);
            value = sanitized.substring(firstSpaceIndex + 1);
        }
    }

    private int determineDepthOf(String line) {
        return line.replaceAll("\t", " ").replaceAll("(^\\s*).+$", "$1").length();
    }

    public void set(String key, String value) {
        if (parent != null) {
            parent.children.remove(this.key);
            parent.children.put(key, this);
        }
        this.key = key;
        this.value = value;
        this.intValue = null;
        this.floatValue = null;
    }

    public ValTree getFirstChild() {
        if (hasChildren()) {
            return children.values().iterator().next();
        }
        return null;
    }

    public ValTree getIndex(int index) {
        int i = 0;
        for (ValTree valTree : this) {
            if (i == index) {
                return valTree;
            }
            i++;
        }
        return null;
    }

    public void save(OutputStream output) {
        save(output, ' ');
    }

    public void save(OutputStream output, char padding) {
        PrintStream printStream = new PrintStream(output);
        try {
            log(printStream, padding);
        } finally {
            printStream.close();
        }
    }

    public void log(PrintStream printStream) {
        log(printStream, ' ');
    }

    public void log(PrintStream printStream, char padding) {
        try {
            for (ValTree tree : this) {
                saveTree(printStream, tree, 0, padding);
            }
        } finally {
            printStream.flush();
        }
    }

    private void saveTree(PrintStream printStream, ValTree tree, int depth, char padding) {
        for (int i = 0; i < depth; i++) {
            printStream.print(padding);
        }

        printStream.print(tree.key);
        if (tree.value != null) {
            printStream.print(" ");
            printStream.print(tree.value);
        }
        printStream.println();

        if (tree.hasChildren()) {
            for (ValTree valTree : tree) {
                saveTree(printStream, valTree, depth + 1, padding);
            }
        }
    }

    private class ProblemReadingFileException extends RuntimeException {
        public ProblemReadingFileException(Throwable cause) {
            super("A problem occurred trying to read your file", cause);
        }
    }
}
