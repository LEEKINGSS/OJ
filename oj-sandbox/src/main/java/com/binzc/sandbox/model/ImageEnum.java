package com.binzc.sandbox.model;

public enum ImageEnum {
    JavaImage(LanguageEnum.JAVA, "openjdk:17-jdk-slim"),
    PythonImage(LanguageEnum.PYTHON, "python:3.10-slim"),
    CppImage(LanguageEnum.CPP, "gcc:12.2.0");
    private final LanguageEnum languageEnum;
    private final String image;
    private ImageEnum(LanguageEnum languageEnum, String image) {
        this.languageEnum = languageEnum;
        this.image = image;
    }

    public String getImageName() {
        return image;
    }
}
