package com.binzc.oj.service.impl;

import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.exception.BusinessException;
import com.binzc.oj.service.WordParseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Word 文档解析服务实现
 *
 * @author binzc
 */
@Service
@Slf4j
public class WordParseServiceImpl implements WordParseService {

    @Override
    public String parseWordToMarkdown(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        // 检查文件扩展名
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!"doc".equals(extension) && !"docx".equals(extension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 .doc 和 .docx 格式的 Word 文档");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String content;
            
            if ("docx".equals(extension)) {
                // 解析 .docx 文件
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                content = extractor.getText();
                extractor.close();
                document.close();
            } else {
                // 解析 .doc 文件
                HWPFDocument document = new HWPFDocument(inputStream);
                WordExtractor extractor = new WordExtractor(document);
                content = extractor.getText();
                extractor.close();
                document.close();
            }

            if (StringUtils.isBlank(content)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档内容为空");
            }

            // 将文本转换为 Markdown 格式
            // 简单的转换：保留换行，将多个连续换行转换为段落分隔
            String markdown = convertToMarkdown(content);
            
            log.info("Word 文档解析成功，文件名: {}, 内容长度: {}", originalFilename, markdown.length());
            return markdown;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Word 文档解析失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Word 文档解析失败: " + e.getMessage());
        }
    }

    /**
     * 将纯文本转换为 Markdown 格式
     * 
     * @param text 原始文本
     * @return Markdown 格式文本
     */
    private String convertToMarkdown(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        // 按行分割
        String[] lines = text.split("\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            
            // 跳过空行
            if (StringUtils.isBlank(line)) {
                markdown.append("\n");
                continue;
            }

            // 检查是否是标题（简单判断：行长度较短且以数字开头或包含特定关键词）
            if (isHeading(line)) {
                markdown.append("## ").append(line).append("\n\n");
            } else {
                // 普通段落
                markdown.append(line).append("\n\n");
            }
        }

        return markdown.toString().trim();
    }

    /**
     * 判断是否是标题
     * 
     * @param line 文本行
     * @return 是否是标题
     */
    private boolean isHeading(String line) {
        // 简单判断：行长度小于 50 且以数字、中文数字或特定关键词开头
        if (line.length() > 50) {
            return false;
        }
        
        // 检查是否以数字开头（如 "1. 标题"、"一、标题"）
        if (line.matches("^[0-9一二三四五六七八九十]+[.、].*")) {
            return true;
        }
        
        // 检查是否包含标题关键词
        String[] headingKeywords = {"题目", "问题", "要求", "说明", "描述", "示例", "输入", "输出", "限制"};
        for (String keyword : headingKeywords) {
            if (line.contains(keyword) && line.length() < 30) {
                return true;
            }
        }
        
        return false;
    }
}

