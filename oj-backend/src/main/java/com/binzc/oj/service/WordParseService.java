package com.binzc.oj.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * 解析 Word 文档为 HTML 格式
     *
     * @param file Word 文档文件
     * @return 解析结果
     */
    ParseResult parseWordToHtml(MultipartFile file);
    
    /**
     * 解析结果
     */
    class ParseResult {
        private boolean success;
        private String html;
        private String message;
        private List<String> warnings = new ArrayList<>();

        // Getters
        public boolean isSuccess() { return success; }
        public String getHtml() { return html; }
        public String getMessage() { return message; }
        public List<String> getWarnings() { return warnings; }

        // Setters
        public void setSuccess(boolean success) { this.success = success; }
        public void setHtml(String html) { this.html = html; }
        public void setMessage(String message) { this.message = message; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }

        public static ParseResult ok(String html) {
            ParseResult r = new ParseResult();
            r.success = true;
            r.html = html;
            r.message = "解析成功";
            return r;
        }

        public static ParseResult fail(String message) {
            ParseResult r = new ParseResult();
            r.success = false;
            r.message = message;
            return r;
        }
    }
}

