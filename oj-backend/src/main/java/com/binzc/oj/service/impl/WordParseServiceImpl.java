package com.binzc.oj.service.impl;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.binzc.oj.service.WordParseService;
import com.binzc.oj.common.ErrorCode;
import com.binzc.oj.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.*;

/**
 * Word 文档解析服务实现（支持公式、代码块、图片等）
 */
@Service
public class WordParseServiceImpl implements WordParseService {

    private static final Logger log = LoggerFactory.getLogger(WordParseServiceImpl.class);

    // OMML to LaTeX 转换器（使用内置 XSLT）
    private volatile Transformer ommlToLatexTransformer;

    @Override
    public String parseWordToMarkdown(MultipartFile file) {
        ParseResult result = parseWordToHtml(file);
        if (!result.isSuccess()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, result.getMessage());
        }
        // 简单地将 HTML 转换为 Markdown（保留原有行为）
        String html = result.getHtml();
        if (StringUtils.isBlank(html)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档内容为空");
        }
        // 简单的 HTML 到 Markdown 转换
        return htmlToMarkdown(html);
    }
    
    private String htmlToMarkdown(String html) {
        // 简单的 HTML 到 Markdown 转换
        
        // 数学公式已经是 $...$ 或 $$...$$ 格式，无需转换
        // 它们会在最后的标签清理中保留下来
        
        // 先处理图片标签，提取src和alt属性并转换为Markdown格式
        // 使用更灵活的正则表达式，支持各种img标签格式
        Pattern imgPattern = Pattern.compile("<img[^>]*\\s+src=\"([^\"]+)\"[^>]*/?>", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcher = imgPattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (imgMatcher.find()) {
            String imgTag = imgMatcher.group(0);
            String src = imgMatcher.group(1);
            
            // 尝试提取alt属性
            String alt = "图片";
            Pattern altPattern = Pattern.compile("alt=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
            Matcher altMatcher = altPattern.matcher(imgTag);
            if (altMatcher.find()) {
                alt = altMatcher.group(1);
                if (alt.isEmpty()) {
                    alt = "图片";
                }
            }
            
            // 转换为Markdown图片语法：![alt](src)
            imgMatcher.appendReplacement(sb, Matcher.quoteReplacement("![" + alt + "](" + src + ")"));
        }
        imgMatcher.appendTail(sb);
        html = sb.toString();
        
        // 处理代码块，保留语言标识
        Pattern codePattern = Pattern.compile("<pre><code class=\"language-([^\"]+)\">([\\s\\S]*?)</code></pre>");
        Matcher codeMatcher = codePattern.matcher(html);
        sb = new StringBuffer();
        while (codeMatcher.find()) {
            String lang = codeMatcher.group(1);
            String code = codeMatcher.group(2);
            // 转换为Markdown代码块：```language\ncode\n```
            codeMatcher.appendReplacement(sb, Matcher.quoteReplacement("```" + lang + "\n" + code + "\n```"));
        }
        codeMatcher.appendTail(sb);
        html = sb.toString();
        
        // 处理其他HTML标签
        String md = html
            .replaceAll("<h([1-6])>", "## ")
            .replaceAll("</h[1-6]>", "")
            .replaceAll("<p>", "")
            .replaceAll("</p>", "\n\n")
            .replaceAll("<strong>", "**")
            .replaceAll("</strong>", "**")
            .replaceAll("<em>", "*")
            .replaceAll("</em>", "*")
            .replaceAll("<code>", "`")
            .replaceAll("</code>", "`")
            .replaceAll("<pre><code[^>]*>", "```\n")
            .replaceAll("</code></pre>", "```\n")
            .replaceAll("<li>", "- ")
            .replaceAll("</li>", "")
            .replaceAll("<ul>|</ul>|<ol>|</ol>", "")
            .replaceAll("<table[^>]*>", "\n")
            .replaceAll("</table>", "\n")
            .replaceAll("<tr>", "")
            .replaceAll("</tr>", "\n")
            .replaceAll("<td>", "| ")
            .replaceAll("</td>", " ")
            .replaceAll("<[^>]+>", "")
            .replaceAll("&nbsp;", " ")
            .replaceAll("&amp;", "&")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">");
        return md.trim();
    }
    
    @Override
    public ParseResult parseWordToHtml(MultipartFile file) {
        return parseDocx(file);
    }

    /**
     * 解析 Word 文档
     */
    private ParseResult parseDocx(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ParseResult.fail("文件为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".docx") && !filename.endsWith(".doc"))) {
            return ParseResult.fail("仅支持 .docx 或 .doc 格式");
        }

        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            StringBuilder html = new StringBuilder();
            List<String> warnings = new ArrayList<>();

            // 遍历所有 body 元素（段落、表格等）
            // 跟踪当前是否在列表中
            boolean inOrderedList = false;
            boolean inUnorderedList = false;
            
            List<IBodyElement> elements = doc.getBodyElements();
            for (int i = 0; i < elements.size(); i++) {
                IBodyElement element = elements.get(i);
                
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph para = (XWPFParagraph) element;
                    BigInteger numId = para.getNumID();
                    
                    // 检查是否是编号列表项
                    if (numId != null) {
                        boolean isOrdered = isOrderedList(doc, numId);
                        
                        // 检查是否需要切换列表类型
                        if (isOrdered && !inOrderedList) {
                            // 需要开始有序列表
                            if (inUnorderedList) {
                                html.append("</ul>\n");
                                inUnorderedList = false;
                            }
                            // 获取起始编号
                            int startNum = getListStartNumber(doc, numId);
                            if (startNum > 1) {
                                html.append("<ol start=\"").append(startNum).append("\">\n");
                            } else {
                                html.append("<ol>\n");
                            }
                            inOrderedList = true;
                        } else if (!isOrdered && !inUnorderedList) {
                            // 需要开始无序列表
                            if (inOrderedList) {
                                html.append("</ol>\n");
                                inOrderedList = false;
                            }
                            html.append("<ul>\n");
                            inUnorderedList = true;
                        }
                        
                        // 输出列表项
                        String content = parseParagraph(para, warnings);
                        content = content.replaceAll("^<[^>]+>|</[^>]+>\\s*$", "").trim();
                        html.append("<li>").append(content).append("</li>\n");
                    } else {
                        // 关闭之前的列表
                        if (inOrderedList) {
                            html.append("</ol>\n");
                            inOrderedList = false;
                        }
                        if (inUnorderedList) {
                            html.append("</ul>\n");
                            inUnorderedList = false;
                        }
                        
                        String paraHtml = parseParagraph(para, warnings);
                        html.append(paraHtml);
                    }
                } else if (element instanceof XWPFTable) {
                    // 关闭之前的列表
                    if (inOrderedList) {
                        html.append("</ol>\n");
                        inOrderedList = false;
                    }
                    if (inUnorderedList) {
                        html.append("</ul>\n");
                        inUnorderedList = false;
                    }
                    
                    String tableHtml = parseTable((XWPFTable) element, warnings);
                    html.append(tableHtml);
                }
            }
            
            // 确保列表被关闭
            if (inOrderedList) html.append("</ol>\n");
            if (inUnorderedList) html.append("</ul>\n");

            ParseResult result = ParseResult.ok(html.toString());
            result.setWarnings(warnings);
            return result;

        } catch (Exception e) {
            log.error("解析 Word 文档失败: {}", e.getMessage(), e);
            return ParseResult.fail("解析失败: " + e.getMessage());
        }
    }

    // 代码相关的样式名称（Word中常见的代码样式）
    private static final Set<String> CODE_STYLE_NAMES = new HashSet<>(Arrays.asList(
        "code", "Code", "代码", "源代码", "SourceCode", "source code",
        "Consolas", "Courier", "monospace", "等宽", "程序代码",
        "HTMLCode", "PlainText", "纯文本"
    ));

    // 代码相关的字体名称
    private static final Set<String> CODE_FONT_NAMES = new HashSet<>(Arrays.asList(
        "Consolas", "Courier New", "Courier", "Monaco", "Menlo",
        "Source Code Pro", "Fira Code", "JetBrains Mono", "Lucida Console",
        "monospace", "等线", "宋体-等宽"
    ));

    /**
     * 检测段落是否为代码样式
     */
    private boolean isCodeParagraph(XWPFParagraph para) {
        // 检查段落样式
        String style = para.getStyle();
        if (style != null) {
            for (String codeName : CODE_STYLE_NAMES) {
                if (style.toLowerCase().contains(codeName.toLowerCase())) {
                    return true;
                }
            }
        }

        // 检查段落中的字体
        for (XWPFRun run : para.getRuns()) {
            String fontFamily = run.getFontFamily();
            if (fontFamily != null) {
                for (String codeFont : CODE_FONT_NAMES) {
                    if (fontFamily.toLowerCase().contains(codeFont.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检测编程语言（根据内容启发式判断）
     */
    private String detectLanguage(String content) {
        String trimmed = content.trim();
        
        // Python 特征
        if (trimmed.startsWith("def ") || trimmed.startsWith("class ") || 
            trimmed.startsWith("import ") || trimmed.startsWith("from ") ||
            trimmed.contains("if __name__") || trimmed.startsWith("print(")) {
            return "python";
        }
        
        // Java 特征
        if (trimmed.startsWith("public ") || trimmed.startsWith("private ") ||
            trimmed.startsWith("protected ") || trimmed.startsWith("package ") ||
            trimmed.contains("public static void main") || trimmed.startsWith("import java.")) {
            return "java";
        }
        
        // C/C++ 特征
        if (trimmed.startsWith("#include") || trimmed.startsWith("int main(") ||
            trimmed.contains("std::") || trimmed.contains("printf(") ||
            trimmed.contains("cout <<") || trimmed.contains("cin >>")) {
            return "cpp";
        }
        
        // JavaScript/TypeScript 特征
        if (trimmed.startsWith("function ") || trimmed.startsWith("const ") ||
            trimmed.startsWith("let ") || trimmed.startsWith("var ") ||
            trimmed.contains("console.log") || trimmed.contains("=>")) {
            return "javascript";
        }
        
        // SQL 特征
        if (trimmed.toUpperCase().startsWith("SELECT ") || 
            trimmed.toUpperCase().startsWith("INSERT ") ||
            trimmed.toUpperCase().startsWith("UPDATE ") ||
            trimmed.toUpperCase().startsWith("CREATE TABLE")) {
            return "sql";
        }
        
        return ""; // 无法确定
    }

    /**
     * 解析段落 - 按顺序处理文本和公式
     */
    private String parseParagraph(XWPFParagraph para, List<String> warnings) {
        StringBuilder sb = new StringBuilder();

        // 检测段落样式（标题等）
        String style = para.getStyle();
        String tag = "p";
        boolean isCode = isCodeParagraph(para);
        
        if (style != null) {
            if (style.startsWith("Heading") || style.contains("标题")) {
                // 提取标题级别
                String level = style.replaceAll("[^0-9]", "");
                if (!level.isEmpty()) {
                    int lvl = Math.min(Math.max(Integer.parseInt(level), 1), 6);
                    tag = "h" + lvl;
                }
            }
        }

        // 按顺序解析段落内容（混合文本和公式）
        String paraContent = parseParagraphMixed(para, warnings);

        // 空段落不输出
        if (paraContent.trim().isEmpty()) {
            return "";
        }

        // 如果是代码段落，使用 pre/code 标签
        if (isCode) {
            String lang = detectLanguage(paraContent);
            String langAttr = lang.isEmpty() ? "" : " class=\"language-" + lang + "\"";
            sb.append("<pre><code").append(langAttr).append(">")
              .append(paraContent)
              .append("</code></pre>\n");
        } else {
            sb.append("<").append(tag).append(">")
              .append(paraContent)
              .append("</").append(tag).append(">\n");
        }

        return sb.toString();
    }
    
    /**
     * 按顺序解析段落中的文本和公式
     */
    private String parseParagraphMixed(XWPFParagraph para, List<String> warnings) {
        StringBuilder result = new StringBuilder();
        
        try {
            CTP ctp = para.getCTP();
            XmlCursor cursor = ctp.newCursor();
            
            // 遍历段落的所有子元素
            if (cursor.toFirstChild()) {
                do {
                    XmlObject obj = cursor.getObject();
                    String localName = cursor.getName().getLocalPart();
                    String namespaceUri = cursor.getName().getNamespaceURI();
                    
                    if ("r".equals(localName) && namespaceUri.contains("wordprocessingml")) {
                        // 处理普通文本运行 <w:r>
                        String text = extractTextFromWRun(obj);
                        if (text != null && !text.isEmpty()) {
                            // 检查格式（粗体、斜体等）
                            boolean isBold = obj.xmlText().contains("<w:b") || obj.xmlText().contains("<w:b/>");
                            boolean isItalic = obj.xmlText().contains("<w:i") || obj.xmlText().contains("<w:i/>");
                            
                            String escaped = escapeHtml(text);
                            if (isBold) escaped = "<strong>" + escaped + "</strong>";
                            if (isItalic) escaped = "<em>" + escaped + "</em>";
                            
                            result.append(escaped);
                        }
                        
                        // 检查图片（支持多种嵌入方式）
                        String xmlText = obj.xmlText();
                        if (xmlText.contains("<a:blip") || xmlText.contains("<pic:pic") || xmlText.contains("w:drawing")) {
                            // 方式1：通过XWPFRun获取嵌入图片
                            boolean foundPic = false;
                            for (XWPFRun run : para.getRuns()) {
                                for (XWPFPicture pic : run.getEmbeddedPictures()) {
                                    result.append(parsePicture(pic, warnings));
                                    foundPic = true;
                                }
                            }
                            
                            // 方式2：通过XML解析获取图片ID
                            if (!foundPic) {
                                String imgHtml = extractImageFromXml(xmlText, para.getDocument(), warnings);
                                if (imgHtml != null && !imgHtml.isEmpty()) {
                                    result.append(imgHtml);
                                }
                            }
                        }
                    } else if ("oMath".equals(localName) || "oMathPara".equals(localName)) {
                        // 处理OMML公式
                        String ommlXml = obj.xmlText();
                        String latex = ommlToLatex(ommlXml, warnings);
                        if (latex != null && !latex.trim().isEmpty()) {
                            // 判断是行内公式还是块级公式
                            if ("oMathPara".equals(localName)) {
                                result.append("$$").append(latex).append("$$");
                            } else {
                                result.append("$").append(latex).append("$");
                            }
                        }
                    }
                } while (cursor.toNextSibling());
            }
            cursor.dispose();
            
        } catch (Exception e) {
            log.warn("解析段落异常，回退到简单解析: {}", e.getMessage());
            // 回退到简单解析
            StringBuilder fallback = new StringBuilder();
            for (XWPFRun run : para.getRuns()) {
                fallback.append(parseRun(run, warnings));
            }
            String omml = extractOmmlFromParagraph(para, warnings);
            if (!omml.isEmpty()) {
                fallback.append(omml);
            }
            return fallback.toString();
        }
        
        return result.toString();
    }
    
    /**
     * 从 w:r 元素中提取文本
     */
    private String extractTextFromWRun(XmlObject wRun) {
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("<w:t[^>]*>([^<]*)</w:t>");
        Matcher matcher = pattern.matcher(wRun.xmlText());
        while (matcher.find()) {
            sb.append(matcher.group(1));
        }
        return sb.toString();
    }

    /**
     * 解析 Run（文本片段）
     */
    private String parseRun(XWPFRun run, List<String> warnings) {
        StringBuilder sb = new StringBuilder();

        // 检查图片
        List<XWPFPicture> pictures = run.getEmbeddedPictures();
        for (XWPFPicture pic : pictures) {
            String imgHtml = parsePicture(pic, warnings);
            sb.append(imgHtml);
        }

        // 文本内容
        String text = run.getText(0);
        if (text != null && !text.isEmpty()) {
            // 处理格式
            boolean bold = run.isBold();
            boolean italic = run.isItalic();
            boolean underline = run.getUnderline() != UnderlinePatterns.NONE;
            boolean strike = run.isStrikeThrough();

            String escaped = escapeHtml(text);

            if (bold) escaped = "<strong>" + escaped + "</strong>";
            if (italic) escaped = "<em>" + escaped + "</em>";
            if (underline) escaped = "<u>" + escaped + "</u>";
            if (strike) escaped = "<del>" + escaped + "</del>";

            sb.append(escaped);
        }

        return sb.toString();
    }

    /**
     * 从段落中提取 OMML 公式并转换为 LaTeX
     * 改进版：保留文本并正确嵌入公式
     */
    private String extractOmmlFromParagraph(XWPFParagraph para, List<String> warnings) {
        StringBuilder result = new StringBuilder();

        try {
            CTP ctp = para.getCTP();
            
            // 使用更宽松的XPath选择OMML公式
            XmlCursor cursor = ctp.newCursor();
            
            // 尝试多种XPath来查找公式
            String[] xpaths = {
                "declare namespace m='http://schemas.openxmlformats.org/officeDocument/2006/math' .//m:oMath",
                "declare namespace m='http://schemas.openxmlformats.org/officeDocument/2006/math' .//m:oMathPara"
            };
            
            List<String> formulas = new ArrayList<>();
            
            for (String xpath : xpaths) {
                cursor = ctp.newCursor();
                cursor.selectPath(xpath);
                
                while (cursor.toNextSelection()) {
                    XmlObject obj = cursor.getObject();
                    String ommlXml = obj.xmlText();
                    
                    // 调试日志
                    log.debug("找到OMML公式: {}", ommlXml.substring(0, Math.min(200, ommlXml.length())));
                    
                    String latex = ommlToLatex(ommlXml, warnings);
                    if (latex != null && !latex.trim().isEmpty()) {
                        // 判断是行内公式还是块级公式
                        boolean isBlock = ommlXml.contains("oMathPara");
                        if (isBlock) {
                            formulas.add("\n$$" + latex + "$$\n");
                        } else {
                            formulas.add("$" + latex + "$");
                        }
                        log.debug("转换后的LaTeX: {}", latex);
                    } else {
                        // 如果转换失败，尝试提取纯文本
                        String fallback = extractTextFromOmml(ommlXml);
                        if (!fallback.trim().isEmpty()) {
                            formulas.add("$" + fallback + "$");
                            log.debug("使用fallback文本: {}", fallback);
                        }
                    }
                }
                cursor.dispose();
            }
            
            if (!formulas.isEmpty()) {
                // 只返回公式，文本部分由 parseRun 处理
                for (String formula : formulas) {
                    result.append(formula);
                }
            }
        } catch (Exception e) {
            log.warn("公式提取异常: {}", e.getMessage());
            warnings.add("公式提取警告: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * OMML XML 转 LaTeX
     */
    private String ommlToLatex(String ommlXml, List<String> warnings) {
        try {
            Transformer transformer = getOmmlToLatexTransformer();
            if (transformer == null) {
                log.debug("XSLT转换器未加载，使用简单解析");
                return decodeLatexEntities(simpleOmmlParse(ommlXml));
            }

            // 确保OMML片段包含命名空间声明
            String wrappedXml = ommlXml;
            if (!ommlXml.contains("xmlns:m=")) {
                // 添加OMML命名空间
                wrappedXml = ommlXml.replaceFirst("<m:oMath", 
                    "<m:oMath xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" " +
                    "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"");
            }

            StringWriter writer = new StringWriter();
            transformer.transform(
                new StreamSource(new StringReader(wrappedXml)),
                new StreamResult(writer)
            );
            String result = writer.toString().trim();
            log.debug("XSLT转换结果: '{}'", result.substring(0, Math.min(100, result.length())));
            
            // 如果XSLT返回空结果，使用简单解析作为fallback
            if (result.isEmpty()) {
                log.debug("XSLT返回空结果，使用简单解析");
                return decodeLatexEntities(simpleOmmlParse(ommlXml));
            }
            
            // 解码LaTeX中的HTML实体
            return decodeLatexEntities(result);
        } catch (Exception e) {
            log.warn("OMML XSLT转换异常: {}", e.getMessage());
            warnings.add("OMML 转换警告: " + e.getMessage());
            return decodeLatexEntities(simpleOmmlParse(ommlXml));
        }
    }
    
    /**
     * 解码LaTeX中的HTML实体
     * Word中的 < 和 > 存储为 &lt; 和 &gt;，需要转换回来
     */
    private String decodeLatexEntities(String latex) {
        if (latex == null) return null;
        return latex
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'");
    }

    /**
     * 简单的 OMML 解析（不使用 XSLT）
     * 递归处理OMML结构
     */
    private String simpleOmmlParse(String ommlXml) {
        StringBuilder result = new StringBuilder();
        
        // 递归处理OMML结构
        result.append(parseOmmlRecursive(ommlXml));
        
        String finalResult = result.toString();
        
        // 清理一些常见替换
        finalResult = finalResult.replace("≠", "\\neq ");
        finalResult = finalResult.replace("≤", "\\leq ");
        finalResult = finalResult.replace("≥", "\\geq ");
        finalResult = finalResult.replace("×", "\\times ");
        finalResult = finalResult.replace("÷", "\\div ");
        finalResult = finalResult.replace("±", "\\pm ");
        finalResult = finalResult.replace("∞", "\\infty ");
        finalResult = finalResult.replace("α", "\\alpha ");
        finalResult = finalResult.replace("β", "\\beta ");
        finalResult = finalResult.replace("γ", "\\gamma ");
        finalResult = finalResult.replace("δ", "\\delta ");
        finalResult = finalResult.replace("ϕ", "\\phi ");
        finalResult = finalResult.replace("φ", "\\varphi ");
        finalResult = finalResult.replace("π", "\\pi ");
        finalResult = finalResult.replace("σ", "\\sigma ");
        finalResult = finalResult.replace("θ", "\\theta ");
        finalResult = finalResult.replace("λ", "\\lambda ");
        finalResult = finalResult.replace("μ", "\\mu ");
        finalResult = finalResult.replace("ε", "\\epsilon ");
        finalResult = finalResult.replace("∈", "\\in ");
        finalResult = finalResult.replace("∉", "\\notin ");
        finalResult = finalResult.replace("⊂", "\\subset ");
        finalResult = finalResult.replace("⊃", "\\supset ");
        finalResult = finalResult.replace("∪", "\\cup ");
        finalResult = finalResult.replace("∩", "\\cap ");
        finalResult = finalResult.replace("≡", "\\equiv ");
        finalResult = finalResult.replace("≈", "\\approx ");
        finalResult = finalResult.replace("∑", "\\sum ");
        finalResult = finalResult.replace("∏", "\\prod ");
        finalResult = finalResult.replace("∫", "\\int ");
        
        return finalResult;
    }
    
    /**
     * 递归解析OMML结构
     */
    private String parseOmmlRecursive(String ommlXml) {
        StringBuilder result = new StringBuilder();
        
        // 处理分数 <m:f>
        if (ommlXml.contains("<m:f>") || ommlXml.contains("<m:f ")) {
            Pattern fracPattern = Pattern.compile("<m:f[^>]*>([\\s\\S]*?)</m:f>");
            Matcher fracMatcher = fracPattern.matcher(ommlXml);
            if (fracMatcher.find()) {
                String fracContent = fracMatcher.group(1);
                Pattern numDenPattern = Pattern.compile("<m:num>([\\s\\S]*?)</m:num>[\\s\\S]*?<m:den>([\\s\\S]*?)</m:den>");
                Matcher numDenMatcher = numDenPattern.matcher(fracContent);
                if (numDenMatcher.find()) {
                    String num = parseOmmlRecursive(numDenMatcher.group(1));
                    String den = parseOmmlRecursive(numDenMatcher.group(2));
                    return "\\frac{" + num + "}{" + den + "}";
                }
            }
        }
        
        // 处理括号/分隔符 <m:d> (delimiter)
        Pattern delimPattern = Pattern.compile("<m:d[^>]*>([\\s\\S]*?)</m:d>");
        Matcher delimMatcher = delimPattern.matcher(ommlXml);
        StringBuffer sb = new StringBuffer();
        while (delimMatcher.find()) {
            String delimContent = delimMatcher.group(1);
            // 提取左右括号字符
            String leftBracket = "(";
            String rightBracket = ")";
            
            // 检查 <m:dPr> 中的括号设置
            Pattern begChrPattern = Pattern.compile("<m:begChr[^>]*m:val=\"([^\"]*)\"/>");
            Matcher begChrMatcher = begChrPattern.matcher(delimContent);
            if (begChrMatcher.find()) {
                leftBracket = begChrMatcher.group(1);
            }
            
            Pattern endChrPattern = Pattern.compile("<m:endChr[^>]*m:val=\"([^\"]*)\"/>");
            Matcher endChrMatcher = endChrPattern.matcher(delimContent);
            if (endChrMatcher.find()) {
                rightBracket = endChrMatcher.group(1);
            }
            
            // 提取括号内的内容 <m:e>
            Pattern ePattern = Pattern.compile("<m:e>([\\s\\S]*?)</m:e>");
            Matcher eMatcher = ePattern.matcher(delimContent);
            StringBuilder innerContent = new StringBuilder();
            while (eMatcher.find()) {
                if (innerContent.length() > 0) innerContent.append(", ");
                innerContent.append(parseOmmlRecursive(eMatcher.group(1)));
            }
            
            // 转换特殊括号为LaTeX
            String latexLeft = convertBracket(leftBracket, true);
            String latexRight = convertBracket(rightBracket, false);
            
            delimMatcher.appendReplacement(sb, Matcher.quoteReplacement(latexLeft + innerContent.toString() + latexRight));
        }
        delimMatcher.appendTail(sb);
        ommlXml = sb.toString();
        
        // 处理上标 <m:sSup>
        Pattern sSupPattern = Pattern.compile("<m:sSup[^>]*>([\\s\\S]*?)</m:sSup>");
        Matcher sSupMatcher = sSupPattern.matcher(ommlXml);
        sb = new StringBuffer();
        while (sSupMatcher.find()) {
            String supContent = sSupMatcher.group(1);
            Pattern basePattern = Pattern.compile("<m:e>([\\s\\S]*?)</m:e>");
            Pattern expPattern = Pattern.compile("<m:sup>([\\s\\S]*?)</m:sup>");
            Matcher baseMatcher = basePattern.matcher(supContent);
            Matcher expMatcher = expPattern.matcher(supContent);
            String base = baseMatcher.find() ? parseOmmlRecursive(baseMatcher.group(1)) : "";
            String exp = expMatcher.find() ? parseOmmlRecursive(expMatcher.group(1)) : "";
            sSupMatcher.appendReplacement(sb, Matcher.quoteReplacement(base + "^{" + exp + "}"));
        }
        sSupMatcher.appendTail(sb);
        ommlXml = sb.toString();
        
        // 处理下标 <m:sSub>
        Pattern sSubPattern = Pattern.compile("<m:sSub[^>]*>([\\s\\S]*?)</m:sSub>");
        Matcher sSubMatcher = sSubPattern.matcher(ommlXml);
        sb = new StringBuffer();
        while (sSubMatcher.find()) {
            String subContent = sSubMatcher.group(1);
            Pattern basePattern = Pattern.compile("<m:e>([\\s\\S]*?)</m:e>");
            Pattern subIdxPattern = Pattern.compile("<m:sub>([\\s\\S]*?)</m:sub>");
            Matcher baseMatcher = basePattern.matcher(subContent);
            Matcher subIdxMatcher = subIdxPattern.matcher(subContent);
            String base = baseMatcher.find() ? parseOmmlRecursive(baseMatcher.group(1)) : "";
            String sub = subIdxMatcher.find() ? parseOmmlRecursive(subIdxMatcher.group(1)) : "";
            sSubMatcher.appendReplacement(sb, Matcher.quoteReplacement(base + "_{" + sub + "}"));
        }
        sSubMatcher.appendTail(sb);
        ommlXml = sb.toString();
        
        // 处理根号 <m:rad>
        Pattern radPattern = Pattern.compile("<m:rad[^>]*>([\\s\\S]*?)</m:rad>");
        Matcher radMatcher = radPattern.matcher(ommlXml);
        sb = new StringBuffer();
        while (radMatcher.find()) {
            String radContent = radMatcher.group(1);
            Pattern ePattern = Pattern.compile("<m:e>([\\s\\S]*?)</m:e>");
            Matcher eMatcher = ePattern.matcher(radContent);
            String content = eMatcher.find() ? parseOmmlRecursive(eMatcher.group(1)) : "";
            radMatcher.appendReplacement(sb, Matcher.quoteReplacement("\\sqrt{" + content + "}"));
        }
        radMatcher.appendTail(sb);
        ommlXml = sb.toString();
        
        // 处理 <m:r> 运行元素（提取文本）
        Pattern rPattern = Pattern.compile("<m:r[^>]*>([\\s\\S]*?)</m:r>");
        Matcher rMatcher = rPattern.matcher(ommlXml);
        sb = new StringBuffer();
        while (rMatcher.find()) {
            String rContent = rMatcher.group(1);
            String text = extractTextFromOmml(rContent);
            rMatcher.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        rMatcher.appendTail(sb);
        ommlXml = sb.toString();
        
        // 最后提取剩余的 <m:t> 文本
        Pattern tPattern = Pattern.compile("<m:t[^>]*>([^<]*)</m:t>");
        Matcher tMatcher = tPattern.matcher(ommlXml);
        sb = new StringBuffer();
        while (tMatcher.find()) {
            tMatcher.appendReplacement(sb, Matcher.quoteReplacement(tMatcher.group(1)));
        }
        tMatcher.appendTail(sb);
        ommlXml = sb.toString();
        
        // 清理剩余的XML标签
        ommlXml = ommlXml.replaceAll("<[^>]+>", "");
        
        return ommlXml.trim();
    }
    
    /**
     * 转换括号为LaTeX格式
     */
    private String convertBracket(String bracket, boolean isLeft) {
        if (bracket == null || bracket.isEmpty()) {
            return isLeft ? "(" : ")";
        }
        switch (bracket) {
            case "(": return "(";
            case ")": return ")";
            case "[": return "[";
            case "]": return "]";
            case "{": return "\\{";
            case "}": return "\\}";
            case "|": return "|";
            case "⌈": return "\\lceil ";
            case "⌉": return "\\rceil ";
            case "⌊": return "\\lfloor ";
            case "⌋": return "\\rfloor ";
            case "〈": case "⟨": return "\\langle ";
            case "〉": case "⟩": return "\\rangle ";
            default: return bracket;
        }
    }

    /**
     * 从 OMML 片段中提取纯文本
     */
    private String extractTextFromOmml(String ommlFragment) {
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("<m:t[^>]*>([^<]*)</m:t>");
        Matcher matcher = pattern.matcher(ommlFragment);
        while (matcher.find()) {
            sb.append(matcher.group(1));
        }
        return sb.toString();
    }

    /**
     * 获取 OMML to LaTeX 转换器
     */
    private Transformer getOmmlToLatexTransformer() {
        if (ommlToLatexTransformer == null) {
            synchronized (this) {
                if (ommlToLatexTransformer == null) {
                    try {
                        // 尝试加载 XSLT（如果存在）
                        InputStream xsltStream = getClass().getResourceAsStream("/xslt/omml2latex.xsl");
                        if (xsltStream != null) {
                            TransformerFactory factory = TransformerFactory.newInstance();
                            ommlToLatexTransformer = factory.newTransformer(new StreamSource(xsltStream));
                        }
                    } catch (Exception e) {
                        log.warn("无法加载 OMML to LaTeX XSLT: {}", e.getMessage());
                    }
                }
            }
        }
        return ommlToLatexTransformer;
    }

    /**
     * 保存图片到文件系统并返回URL
     */
    private String saveImageToFile(byte[] imageData, String mimeType, List<String> warnings) {
        try {
            // 创建图片目录（如果不存在）
            Path imageDir = Paths.get("static/images");
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            // 根据MIME类型确定文件扩展名
            String extension = getImageExtension(mimeType);
            
            // 生成唯一文件名（使用MD5哈希）
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(imageData);
            StringBuilder hashStr = new StringBuilder();
            for (byte b : hash) {
                hashStr.append(String.format("%02x", b));
            }
            String filename = hashStr.toString() + extension;
            
            // 保存文件
            Path imagePath = imageDir.resolve(filename);
            Files.write(imagePath, imageData);
            
            // 返回完整的URL路径（包括协议和域名，这样前端可以正确访问）
            // 使用环境变量或默认值
            String serverHost = System.getenv("SERVER_HOST");
            if (serverHost == null || serverHost.isEmpty()) {
                serverHost = "http://localhost:8121";
            }
            return serverHost + "/api/static/images/" + filename;
        } catch (Exception e) {
            warnings.add("图片保存警告: " + e.getMessage());
            log.warn("保存图片失败", e);
            // 如果保存失败，回退到base64
            try {
                String base64 = Base64.getEncoder().encodeToString(imageData);
                return "data:" + mimeType + ";base64," + base64;
            } catch (Exception e2) {
                return "";
            }
        }
    }
    
    /**
     * 根据MIME类型获取图片扩展名
     */
    private String getImageExtension(String mimeType) {
        if (mimeType == null) return ".jpg";
        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/bmp":
                return ".bmp";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg";
        }
    }

    /**
     * 解析图片
     */
    private String parsePicture(XWPFPicture pic, List<String> warnings) {
        try {
            XWPFPictureData picData = pic.getPictureData();
            if (picData == null) {
                return "";
            }

            byte[] data = picData.getData();
            String mimeType = getMimeType(picData.getPackagePart().getContentType());

            // 保存图片到文件系统并获取URL
            String src = saveImageToFile(data, mimeType, warnings);
            if (src.isEmpty()) {
                return "";
            }

            // 获取图片尺寸（如果可用）
            String style = "";

            return "<img src=\"" + src + "\" alt=\"图片\"" + style + " />\n";
        } catch (Exception e) {
            warnings.add("图片提取警告: " + e.getMessage());
            return "<span class=\"error\">[图片加载失败]</span>";
        }
    }
    
    /**
     * 从XML中提取图片（处理DrawingML格式，包括嵌入和外部链接）
     */
    private String extractImageFromXml(String xmlText, XWPFDocument doc, List<String> warnings) {
        StringBuilder result = new StringBuilder();
        try {
            Set<String> processedIds = new HashSet<>();
            
            // 方式1：处理嵌入的图片（r:embed）
            Pattern embedPattern = Pattern.compile("r:embed=\"([^\"]+)\"");
            Matcher embedMatcher = embedPattern.matcher(xmlText);
            
            while (embedMatcher.find()) {
                String embedId = embedMatcher.group(1);
                if (processedIds.contains(embedId)) continue;
                processedIds.add(embedId);
                
                // 通过关系ID获取图片数据
                XWPFPictureData picData = findPictureDataByRelId(doc, embedId);
                if (picData != null) {
                    byte[] data = picData.getData();
                    String mimeType = getMimeType(picData.getPackagePart().getContentType());
                    
                    // 保存图片到文件系统并获取URL
                    String src = saveImageToFile(data, mimeType, warnings);
                    if (!src.isEmpty()) {
                        result.append("<img src=\"").append(src).append("\" alt=\"图片\" />\n");
                    }
                }
            }
            
            // 方式2：处理外部链接的图片（r:link）
            Pattern linkPattern = Pattern.compile("r:link=\"([^\"]+)\"");
            Matcher linkMatcher = linkPattern.matcher(xmlText);
            
            while (linkMatcher.find()) {
                String linkId = linkMatcher.group(1);
                if (processedIds.contains(linkId)) continue;
                processedIds.add(linkId);
                
                // 尝试获取外部链接
                String externalUrl = findExternalLinkById(doc, linkId);
                if (externalUrl != null && !externalUrl.isEmpty()) {
                    // 直接使用外部链接
                    result.append("<img src=\"").append(externalUrl).append("\" alt=\"图片\" />\n");
                    log.debug("找到外部图片链接: {}", externalUrl);
                }
            }
        } catch (Exception e) {
            warnings.add("XML图片提取警告: " + e.getMessage());
        }
        return result.toString();
    }
    
    /**
     * 通过关系ID查找外部链接
     */
    private String findExternalLinkById(XWPFDocument doc, String linkId) {
        try {
            org.apache.poi.openxml4j.opc.PackagePart docPart = doc.getPackagePart();
            if (docPart != null) {
                org.apache.poi.openxml4j.opc.PackageRelationship rel = docPart.getRelationship(linkId);
                if (rel != null && rel.getTargetMode() == org.apache.poi.openxml4j.opc.TargetMode.EXTERNAL) {
                    return rel.getTargetURI().toString();
                }
            }
        } catch (Exception e) {
            log.debug("查找外部链接失败: linkId={}, error={}", linkId, e.getMessage());
        }
        return null;
    }
    
    /**
     * 通过关系ID查找图片数据
     */
    private XWPFPictureData findPictureDataByRelId(XWPFDocument doc, String relId) {
        try {
            // 从文档的所有图片中查找
            for (XWPFPictureData picData : doc.getAllPictures()) {
                // 检查是否匹配关系ID
                String picRelId = doc.getRelationId(picData);
                if (relId.equals(picRelId)) {
                    return picData;
                }
            }
            
            // 如果上面的方法不行，尝试通过包关系查找
            org.apache.poi.openxml4j.opc.PackagePart docPart = doc.getPackagePart();
            if (docPart != null) {
                try {
                    org.apache.poi.openxml4j.opc.PackageRelationship rel = docPart.getRelationship(relId);
                    if (rel != null) {
                        org.apache.poi.openxml4j.opc.PackagePart imagePart = docPart.getRelatedPart(rel);
                        if (imagePart != null) {
                            // 从包中读取图片数据
                            try (InputStream is = imagePart.getInputStream();
                             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    baos.write(buffer, 0, len);
                                }
                                byte[] data = baos.toByteArray();
                                String contentType = imagePart.getContentType();
                                // 创建临时的返回结构
                                for (XWPFPictureData picData : doc.getAllPictures()) {
                                    if (Arrays.equals(picData.getData(), data)) {
                                        return picData;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略关系查找错误
                }
            }
        } catch (Exception e) {
            log.warn("查找图片数据失败: relId={}, error={}", relId, e.getMessage());
        }
        return null;
    }

    /**
     * 解析表格
     */
    private String parseTable(XWPFTable table, List<String> warnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"word-table\">\n");

        for (XWPFTableRow row : table.getRows()) {
            sb.append("<tr>\n");
            for (XWPFTableCell cell : row.getTableCells()) {
                sb.append("<td>");
                List<XWPFParagraph> paragraphs = cell.getParagraphs();
                
                // 首先获取单元格的完整文本内容，检查是否为代码块
                StringBuilder cellText = new StringBuilder();
                for (XWPFParagraph para : paragraphs) {
                    cellText.append(para.getText()).append("\n");
                }
                String cellContent = cellText.toString().trim();
                
                // 检测是否为代码块（以语言名称开头）
                String codeLanguage = detectCodeBlockLanguage(cellContent);
                if (codeLanguage != null) {
                    // 这是一个代码块，提取代码内容
                    String codeContent = extractCodeFromCell(cellContent, codeLanguage);
                    sb.append("<pre><code class=\"language-").append(codeLanguage).append("\">");
                    sb.append(escapeHtml(codeContent));
                    sb.append("</code></pre>");
                } else {
                    // 正常处理
                    boolean inList = false;
                    
                    for (int i = 0; i < paragraphs.size(); i++) {
                        XWPFParagraph para = paragraphs.get(i);
                        String paraContent = parseParagraph(para, warnings);
                        // 移除外层标签
                        paraContent = paraContent.replaceAll("^<[^>]+>|</[^>]+>\\s*$", "").trim();
                        
                        if (paraContent.isEmpty()) continue;
                        
                        boolean isListItem = para.getNumID() != null || isListParagraph(para);
                        
                        if (isListItem) {
                            // 开始一个新列表
                            if (!inList) {
                                sb.append("<ul>");
                                inList = true;
                            }
                            // 移除项目符号字符（如果有）
                            paraContent = paraContent.replaceFirst("^[•\\-·●○◆]\\s*", "");
                            sb.append("<li>").append(paraContent).append("</li>");
                        } else {
                            // 关闭之前的列表
                            if (inList) {
                                sb.append("</ul>");
                                inList = false;
                            }
                            sb.append(paraContent);
                            // 在非列表段落之间添加换行
                            if (i < paragraphs.size() - 1 && !isListParagraph(paragraphs.get(i + 1)) && paragraphs.get(i + 1).getNumID() == null) {
                                sb.append("<br/>");
                            }
                        }
                    }
                    
                    // 确保列表正确关闭
                    if (inList) {
                        sb.append("</ul>");
                    }
                }
                
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }

        sb.append("</table>\n");
        return sb.toString();
    }
    
    /**
     * 检测段落是否为列表项（通过项目符号检测）
     */
    private boolean isListParagraph(XWPFParagraph para) {
        String text = para.getText();
        if (text == null) return false;
        text = text.trim();
        // 检测常见的项目符号开头
        return text.startsWith("•") || text.startsWith("-") || text.startsWith("·") ||
               text.startsWith("●") || text.startsWith("○") || text.startsWith("◆") ||
               text.matches("^\\d+[.、)].*"); // 数字列表
    }
    
    /**
     * 检测单元格内容是否为代码块，返回语言名称或null
     */
    private String detectCodeBlockLanguage(String content) {
        if (content == null || content.isEmpty()) return null;
        
        String trimmed = content.trim();
        String firstLine = trimmed.split("\n")[0].trim();
        
        // 检查是否以语言名称开头
        String[] languages = {
            "Python", "python", "PYTHON",
            "Java", "java", "JAVA",
            "JavaScript", "javascript", "JS", "js",
            "TypeScript", "typescript", "TS", "ts",
            "C++", "cpp", "CPP", "c++",
            "C#", "csharp", "CSharp",
            "C", "c",
            "Go", "go", "golang",
            "Rust", "rust",
            "Ruby", "ruby",
            "PHP", "php",
            "Swift", "swift",
            "Kotlin", "kotlin",
            "SQL", "sql",
            "Shell", "shell", "bash", "Bash",
            "HTML", "html",
            "CSS", "css",
            "XML", "xml",
            "JSON", "json",
            "YAML", "yaml"
        };
        
        for (String lang : languages) {
            if (firstLine.equals(lang) || firstLine.startsWith(lang + " ") || 
                firstLine.startsWith(lang + "\n") || firstLine.startsWith(lang + "def") ||
                firstLine.startsWith(lang + "class") || firstLine.startsWith(lang + "import") ||
                firstLine.startsWith(lang + "public") || firstLine.startsWith(lang + "private") ||
                firstLine.startsWith(lang + "function") || firstLine.startsWith(lang + "#")) {
                return lang.toLowerCase();
            }
        }
        
        // 检测代码特征
        if (trimmed.contains("def ") && trimmed.contains(":") && 
            (trimmed.contains("return ") || trimmed.contains("self"))) {
            return "python";
        }
        if (trimmed.contains("function ") && trimmed.contains("{")) {
            return "javascript";
        }
        if (trimmed.contains("public class ") || trimmed.contains("public static void main")) {
            return "java";
        }
        
        return null;
    }
    
    /**
     * 从单元格内容中提取代码
     */
    private String extractCodeFromCell(String content, String language) {
        if (content == null) return "";
        
        String trimmed = content.trim();
        
        // 移除开头的语言标识
        String[] languages = {"Python", "python", "Java", "java", "JavaScript", "javascript", 
                              "C++", "cpp", "C", "c", "Go", "go", "Rust", "rust", "Ruby", "ruby",
                              "PHP", "php", "Swift", "swift", "Kotlin", "kotlin", "SQL", "sql",
                              "Shell", "shell", "bash", "Bash", "HTML", "html", "CSS", "css"};
        
        for (String lang : languages) {
            if (trimmed.startsWith(lang)) {
                // 检查是否紧跟代码
                String afterLang = trimmed.substring(lang.length());
                if (afterLang.startsWith("def ") || afterLang.startsWith("class ") ||
                    afterLang.startsWith("import ") || afterLang.startsWith("public ") ||
                    afterLang.startsWith("private ") || afterLang.startsWith("function ") ||
                    afterLang.startsWith("#") || afterLang.startsWith("//") ||
                    afterLang.startsWith("\n") || afterLang.startsWith(" ")) {
                    trimmed = afterLang.trim();
                    break;
                }
            }
        }
        
        return trimmed;
    }
    
    /**
     * 获取抽象编号ID
     */
    private BigInteger getAbstractNumId(XWPFDocument doc, BigInteger numId) {
        try {
            XWPFNumbering numbering = doc.getNumbering();
            if (numbering == null) return null;
            
            XWPFNum num = numbering.getNum(numId);
            if (num == null) return null;
            
            return num.getCTNum().getAbstractNumId().getVal();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取列表的起始编号
     */
    private int getListStartNumber(XWPFDocument doc, BigInteger numId) {
        try {
            XWPFNumbering numbering = doc.getNumbering();
            if (numbering == null) return 1;
            
            XWPFNum num = numbering.getNum(numId);
            if (num == null) return 1;
            
            BigInteger abstractNumId = num.getCTNum().getAbstractNumId().getVal();
            XWPFAbstractNum abstractNum = numbering.getAbstractNum(abstractNumId);
            if (abstractNum == null) return 1;
            
            // 检查第一级的起始编号
            CTAbstractNum ctAbstractNum = abstractNum.getCTAbstractNum();
            if (ctAbstractNum.sizeOfLvlArray() > 0) {
                CTLvl lvl = ctAbstractNum.getLvlArray(0);
                if (lvl.getStart() != null) {
                    return lvl.getStart().getVal().intValue();
                }
            }
            return 1;
        } catch (Exception e) {
            log.debug("获取列表起始编号失败: {}", e.getMessage());
            return 1;
        }
    }
    
    /**
     * 检测编号列表是有序还是无序
     */
    private boolean isOrderedList(XWPFDocument doc, BigInteger numId) {
        try {
            XWPFNumbering numbering = doc.getNumbering();
            if (numbering == null) return true; // 默认有序
            
            XWPFNum num = numbering.getNum(numId);
            if (num == null) return true;
            
            BigInteger abstractNumId = num.getCTNum().getAbstractNumId().getVal();
            XWPFAbstractNum abstractNum = numbering.getAbstractNum(abstractNumId);
            if (abstractNum == null) return true;
            
            // 检查第一级的编号格式
            CTAbstractNum ctAbstractNum = abstractNum.getCTAbstractNum();
            if (ctAbstractNum.sizeOfLvlArray() > 0) {
                CTLvl lvl = ctAbstractNum.getLvlArray(0);
                if (lvl.getNumFmt() != null) {
                    String fmt = lvl.getNumFmt().getVal().toString();
                    // bullet 表示无序列表
                    if ("bullet".equalsIgnoreCase(fmt)) {
                        return false;
                    }
                }
            }
            return true; // 默认有序
        } catch (Exception e) {
            log.debug("检测列表类型失败: {}", e.getMessage());
            return true; // 默认有序
        }
    }

    /**
     * HTML 转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * 获取 MIME 类型
     */
    private String getMimeType(String contentType) {
        if (contentType == null) return "image/png";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return "image/jpeg";
        if (contentType.contains("gif")) return "image/gif";
        if (contentType.contains("png")) return "image/png";
        if (contentType.contains("webp")) return "image/webp";
        if (contentType.contains("bmp")) return "image/bmp";
        return "image/png";
    }
}
