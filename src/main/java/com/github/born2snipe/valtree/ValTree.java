package com.github.born2snipe.valtree;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Stack;

public class ValTree {
    private HashMap<String, ValTree> children = new HashMap<String, ValTree>();
    private String key;
    private String value;
    private Float floatValue;
    private Integer intValue;
    private int depth;

    public void parse(File file) {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            parse(input);
        } catch (IOException e) {
            throw new ProblemReadingFileException(e);
        } finally {
            close(input);
        }
    }

    public void parse(InputStream inputStream) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                output.write(buf, 0, len);
            }
            parseData(new String(output.toByteArray()));
        } catch (IOException e) {
            throw new ProblemReadingFileException(e);
        } finally {
            close(inputStream);
        }
    }

    public void parseData(String content) {
        String[] lines = content.split("\n");
        Stack<ValTree> parentStack = new Stack<ValTree>();
        parentStack.push(this);

        for (String line : lines) {
            line = line.replaceAll("//.+", "");

            if (line.trim().length() == 0) {
                continue;
            }

            ValTree child = new ValTree();
            child.parseLine(line);
            int childDepth = child.depth;

            while (parentStack.size() > 1) {
                ValTree currentParent = parentStack.peek();
                if (childDepth <= currentParent.depth) {
                    parentStack.pop();
                } else {
                    break;
                }
            }

            parentStack.peek().children.put(child.key, child);
            parentStack.push(child);
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
        return "ValTree{" +
                "depth=" + depth +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public boolean isNull() {
        return value == null;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {

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

    public int size() {
        return children.size();
    }

    public void clear() {
        key = null;
        value = null;
        floatValue = null;
        intValue = null;
        children.clear();
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    private class ProblemReadingFileException extends RuntimeException {
        public ProblemReadingFileException(Throwable cause) {
            super("A problem occurred trying to read your file", cause);
        }
    }
}
