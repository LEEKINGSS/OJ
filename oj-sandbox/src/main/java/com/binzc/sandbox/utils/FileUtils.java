package com.binzc.sandbox.utils;


import cn.hutool.core.io.FileUtil;
import com.binzc.sandbox.model.LanguageFileNameEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
@Slf4j
public class FileUtils {
    public static final String GLOBAL_CODE_DIR="temp";
    public static String saveFile(String code,String language){
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator +language+File.separator+  UUID.randomUUID();
        String fileName= LanguageFileNameEnum.getEnumFromLanguage(language).getFileName();
        String userCodePath = userCodeParentPath + File.separator + fileName;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        log.info("用户代码文件路径:{}",userCodeParentPath);
        return userCodeParentPath;
    }

    public static boolean deleteFile(String path){
        return FileUtil.del(path);
    }

    public static String MapToPhysicalPath(String mountPath, String virtualPath) {
        if(virtualPath==null){
            throw new RuntimeException("路径错误");
        }
        if (mountPath == null) {
            return virtualPath;
        }

        // 标准化为统一分隔符 /
        String normalizedVirtualPath = virtualPath.replace("\\", "/");
        String normalizedMountPath = mountPath.replace("\\", "/");

        // 找到 /temp 的位置
        int tempIndex = normalizedVirtualPath.indexOf("/temp");
        if (tempIndex == -1) {
            // 没有 /temp，返回 null 或原始路径
            return null;
        }

        // 截取 /temp 后的部分，不包括 /temp 本身
        String relativePath = normalizedVirtualPath.substring(tempIndex + "/temp".length());

        // 避免双斜杠
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // 拼接 mountPath 和相对路径
        String fullPath = normalizedMountPath + "/" + relativePath;

        // 转换为系统默认分隔符
        return fullPath.replace("/", java.io.File.separator);
    }


}
