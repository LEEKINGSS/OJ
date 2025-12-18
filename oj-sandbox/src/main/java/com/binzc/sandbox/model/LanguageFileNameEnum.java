package com.binzc.sandbox.model;

import com.binzc.sandbox.common.ErrorCode;
import com.binzc.sandbox.exception.BusinessException;

public enum LanguageFileNameEnum {
    JAVA("java","Main.java"),
    PYTHON("python","Main.py"),
    CPP("cpp","Main.cpp");
    private final String fileName;
    private final String language;
    LanguageFileNameEnum( String language,String fileName) {
        this.fileName = fileName;
        this.language = language;
    }
    public String getFileName() {
        return fileName;
    }
    public String getLanguage() {
        return language;
    }
    public static LanguageFileNameEnum getEnumFromLanguage(String language){
        if (language == null || language.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"语言不存在");
        }
        for (LanguageFileNameEnum langEnum : values()) {
            if (langEnum.language.equalsIgnoreCase(language)) {
                return langEnum;
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"语言不存在");
    }
}
