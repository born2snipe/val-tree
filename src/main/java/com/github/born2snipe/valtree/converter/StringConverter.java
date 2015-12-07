package com.github.born2snipe.valtree.converter;

public class StringConverter extends ValueConverter<String> {
    @Override
    protected String fromText(String text) {
        return text;
    }

    @Override
    protected String toText(String value) {
        return value;
    }
}
