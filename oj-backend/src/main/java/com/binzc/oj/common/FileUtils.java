package com.binzc.oj.common;

public class FileUtils {
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        int lastSeparatorIndex = Math.max(
                fileName.lastIndexOf('/'),  // Unix路径分隔符
                fileName.lastIndexOf('\\')  // Windows路径分隔符
        );
        // 确保点号在路径分隔符之后，且不是文件名的第一个字符
        if (dotIndex > lastSeparatorIndex && dotIndex > 0) {
            return fileName.substring(dotIndex);
        }
        return "";
    }
    public static void main(String[] args) {
        System.out.println(FileUtils.getFileExtension("image.jpg"));     // .jpg
        System.out.println(FileUtils.getFileExtension("dir/file.txt"));  // .txt
        System.out.println(FileUtils.getFileExtension("noExtension"));   // ""
        System.out.println(FileUtils.getFileExtension(".hiddenFile"));   // ""
        System.out.println(FileUtils.getFileExtension("path/.ext"));     // .ext
        System.out.println(FileUtils.getFileExtension("file.jpg.zip"));  // .zip
        System.out.println(FileUtils.getFileExtension("C:\\temp\\file.doc")); // .doc
    }
}
