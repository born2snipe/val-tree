package com.github.born2snipe.valtree.converter;

public abstract class ValueConverter<T> {
    protected abstract T fromText(String text);

    protected abstract String toText(T value);

    public T convertFromText(String text) throws UnableToConvertFromTextException {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        return fromText(text);
    }

    public String convertToText(T value) {
        if (value == null) {
            return "";
        }

        return toText(value);
    }
}
