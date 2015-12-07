package com.github.born2snipe.valtree.converter;

public class FloatConverter extends ValueConverter<Float> {

    @Override
    protected Float fromText(String text) {
        return Float.valueOf(text);
    }

    @Override
    protected String toText(Float value) {
        return value.toString();
    }
}
