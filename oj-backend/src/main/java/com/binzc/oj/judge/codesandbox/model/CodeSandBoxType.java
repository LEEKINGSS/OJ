package com.binzc.oj.judge.codesandbox.model;

public enum CodeSandBoxType {
    REMOTE("remote"),
    EXAMPLE("example"),
    THIRDPARTY("thirdparty");
    private final String value;
    CodeSandBoxType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    public static CodeSandBoxType getEnumByValue(String value) {
        for (CodeSandBoxType type : CodeSandBoxType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return EXAMPLE;
    }
}
