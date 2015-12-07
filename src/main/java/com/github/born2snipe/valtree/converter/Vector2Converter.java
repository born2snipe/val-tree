package com.github.born2snipe.valtree.converter;

import com.badlogic.gdx.math.Vector2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vector2Converter extends ValueConverter<Vector2> {
    private static final Pattern PATTERN = Pattern.compile("([0-9]+(\\.[0-9]+)?)\\s*?,\\s*?([0-9]+(\\.[0-9]+)?)");

    protected Vector2 fromText(String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.find()) {
            float x = Float.valueOf(matcher.group(1));
            float y = Float.valueOf(matcher.group(3));
            return new Vector2(x, y);
        }

        throw new UnableToConvertFromTextException(text, Vector2.class);
    }

    @Override
    protected String toText(Vector2 value) {
        return "(" + value.x + ", " + value.y + ")";
    }
}
