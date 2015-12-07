package com.github.born2snipe.valtree;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.born2snipe.valtree.converter.FloatConverter;
import com.github.born2snipe.valtree.converter.IntegerConverter;
import com.github.born2snipe.valtree.converter.StringConverter;
import com.github.born2snipe.valtree.converter.ValueConverter;
import com.github.born2snipe.valtree.converter.Vector2Converter;
import com.github.born2snipe.valtree.converter.Vector3Converter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class ValTree implements Iterable<ValTree> {
    private static final ObjectMap<Class, ValueConverter> converters = new ObjectMap<Class, ValueConverter>();

    static {
        registerConverter(String.class, new StringConverter());
        registerConverter(Float.class, new FloatConverter());
        registerConverter(Integer.class, new IntegerConverter());
        registerConverter(Vector2.class, new Vector2Converter());
        registerConverter(Vector3.class, new Vector3Converter());
    }

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

    public static void registerConverter(Class clazz, ValueConverter converter) {
        converters.put(clazz, converter);
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
        BufferedReader reader = null;
        try {
            clear();
            Array<ValTree> parentStack = new Array<ValTree>();
            parentStack.add(this);

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;

            while ((line = reader.readLine()) != null) {
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
        } catch (IOException e) {
            throw new ProblemReadingFileException(e);
        } finally {
            close(reader);
        }
    }

    public void parseData(String content) {
        parse(new ByteArrayInputStream(content.getBytes()));
    }

    public Array<ValTree> getSiblings() {
        if (parent == null) {
            return new Array<ValTree>(0);
        }
        return parent.children.values().toArray();
    }

    public Array<ValTree> getChildren() {
        return children.values().toArray();
    }

    public ValTree getChild(String key) {
        return children.get(key);
    }

    public String getString() {
        return value;
    }

    public Integer getInteger() {
        if (intValue == null && value != null) {
            intValue = getValueAs(Integer.class);
        }
        return intValue;
    }

    public <T> T getValueAs(Class<T> expectedReturnType) {
        return (T) findConverter(expectedReturnType).convertFromText(value);
    }

    public void setValue(Object value) {
        if (value == null) {
            this.value = null;
            this.floatValue = null;
            this.intValue = null;
        } else {
            this.value = findConverter(value.getClass()).convertToText(value);
        }
    }

    public Float getFloat() {
        if (floatValue == null && value != null) {
            floatValue = getValueAs(Float.class);
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
        setValue(null);
        children.clear();
    }

    public boolean hasChildren() {
        return children.size > 0;
    }

    public String queryForString(String query) {
        ValTree result = query(query);
        if (result == null) {
            return null;
        }
        return result.getString();
    }

    public Integer queryForInteger(String query) {
        ValTree result = query(query);
        if (result == null) {
            return null;
        }
        return result.getInteger();
    }

    public Float queryForFloat(String query) {
        ValTree result = query(query);
        if (result == null) {
            return null;
        }
        return result.getFloat();
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

    public void addChild(String key) {
        addChild(key, null);
    }

    public void addChild(String key, float value) {
        addChild(key, String.valueOf(value));
    }

    public void addChild(String key, int value) {
        addChild(key, String.valueOf(value));
    }

    public void addChild(String key, String value) {
        String[] keyPath = key.split("\\.");

        ValTree parent = this;
        for (String currentKey : keyPath) {
            String childValue = value;
            if (!key.endsWith(currentKey)) {
                childValue = null;
            }

            ValTree child = parent.getChild(currentKey);
            if (child == null) {
                child = new ValTree(currentKey, childValue);
            }

            parent.addChild(child);
            parent = child;
        }
    }

    public void addChild(ValTree tree) {
        tree.parent = this;
        children.put(tree.key, tree);
    }

    @Override
    public Iterator<ValTree> iterator() {
        return children.values().iterator();
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

    public void set(String key, Object value) {
        if (parent != null) {
            parent.children.remove(this.key);
            parent.children.put(key, this);
        }
        this.key = key;
        this.value = findConverter(value.getClass()).convertToText(value);
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
        PrintStream printStream = new PrintStream(new BufferedOutputStream(output));
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
            for (ValTree tree : getChildren()) {
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

    public void addChild(String key, Object value) {
        addChild(key, findConverter(value.getClass()).convertToText(value));
    }

    private ValueConverter findConverter(Class<?> clazz) {
        ValueConverter converter = converters.get(clazz);
        if (converter == null) {
            throw new IllegalArgumentException("No registered ValueConverter found for type: " + clazz.getName());
        }
        return converter;
    }

    public <T> T queryFor(String query, Class<T> expectedReturnType) {
        ValTree result = query(query);
        if (result == null) {
            return null;
        }
        return result.getValueAs(expectedReturnType);
    }

    private class ProblemReadingFileException extends RuntimeException {
        public ProblemReadingFileException(Throwable cause) {
            super("A problem occurred trying to read your file", cause);
        }
    }
}
