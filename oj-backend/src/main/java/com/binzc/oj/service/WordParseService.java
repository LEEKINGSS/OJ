package com.binzc.oj.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Word 文档解析服务
 *
 * @author binzc
 */
public interface WordParseService {
    /**
     * 解析 Word 文档为 Markdown 格式文本
     *
     * @param file Word 文档文件
     * @return Markdown 格式的文本内容
     */
    String parseWordToMarkdown(MultipartFile file);
}

