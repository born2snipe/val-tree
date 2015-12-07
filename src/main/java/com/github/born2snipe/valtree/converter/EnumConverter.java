package com.github.born2snipe.valtree.converter;

public class EnumConverter extends ValueConverter<Object> {
    private final Class<? extends Enum> enumClass;

    public EnumConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    protected Object fromText(String text) {
        try {
            String sanitize = text.toUpperCase().replace(" ", "_");
            return Enum.valueOf(enumClass, sanitize);
        } catch (IllegalArgumentException e) {
            throw new UnableToConvertFromTextException(text, enumClass, e);
        }
    }

    @Override
    protected String toText(Object value) {
        Enum e = (Enum) value;
        return e.name().replace("_", " ");
    }
}
