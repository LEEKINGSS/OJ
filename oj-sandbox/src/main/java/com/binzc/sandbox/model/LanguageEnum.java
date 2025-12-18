package com.binzc.sandbox.model;

public enum LanguageEnum {
    JAVA("java"),
    PYTHON("python"),
    CPP("cpp");

    private final String value;

    LanguageEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LanguageEnum fromValue(String input) {
        for (LanguageEnum lang : values()) {
            if (lang.value.equalsIgnoreCase(input)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unsupported language: " + input);
    }
}
