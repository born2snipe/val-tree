package com.github.born2snipe.valtree.converter;

import com.badlogic.gdx.math.Vector3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vector3Converter extends ValueConverter<Vector3> {
    private static final Pattern PATTERN = Pattern.compile("([0-9]+(\\.[0-9]*)?)\\s*?,\\s*?([0-9]+(\\.[0-9]*)?)\\s*?,\\s*?([0-9]+(\\.[0-9]*)?)");

    @Override
    protected Vector3 fromText(String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.find()) {
            float x = Float.valueOf(matcher.group(1));
            float y = Float.valueOf(matcher.group(3));
            float z = Float.valueOf(matcher.group(5));
            return new Vector3(x, y, z);
        }
        throw new UnableToConvertFromTextException(text, Vector3.class);
    }

    @Override
    protected String toText(Vector3 value) {
        return "(" + value.x + ", " + value.y + ", " + value.z + ")";
    }
}
