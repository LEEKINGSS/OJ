#!/bin/bash

# 启动后端服务脚本

set -e

echo "=========================================="
echo "启动 OJ 后端服务"
echo "=========================================="

# 加载环境变量
if [ -f "$HOME/.oj_env" ]; then
    source "$HOME/.oj_env"
    echo "环境变量已加载"
else
    echo "警告: 未找到环境变量文件 ~/.oj_env"
    echo "使用默认配置或环境变量"
fi

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请先运行 setup-wsl.sh 配置环境"
    exit 1
fi

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到 Maven，请先运行 setup-wsl.sh 配置环境"
    exit 1
fi

# 进入后端目录
cd oj-backend

# 显示配置信息
echo ""
echo "当前配置:"
echo "  数据库: ${MYSQL_URL:-使用默认配置}"
echo "  代码沙箱: ${CODESANDBOX_URL:-使用默认配置}"
echo "  服务端口: ${SERVER_PORT:-8121}"
echo ""

# 启动服务
echo "正在启动后端服务..."
echo "服务地址: http://localhost:${SERVER_PORT:-8121}/api"
echo "API 文档: http://localhost:${SERVER_PORT:-8121}/api/doc.html"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

mvn spring-boot:run

