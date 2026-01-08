#!/bin/bash

# 生成前端 API 客户端代码脚本

set -e

echo "=========================================="
echo "生成前端 API 客户端代码"
echo "=========================================="

# 后端 API 地址（可以通过环境变量覆盖）
BACKEND_URL="${BACKEND_URL:-http://localhost:8121}"
API_DOCS_URL="${BACKEND_URL}/api/v2/api-docs"
API_DOCS_FILE="api-docs.json"

echo "后端 API 地址: $BACKEND_URL"
echo "API 文档地址: $API_DOCS_URL"
echo ""

# 检查后端服务是否运行
echo "检查后端服务是否运行..."
if ! curl -s --connect-timeout 5 "$API_DOCS_URL" > /dev/null 2>&1; then
    echo "错误: 无法连接到后端服务 ($API_DOCS_URL)"
    echo "请确保后端服务已启动，然后重试"
    exit 1
fi

echo "后端服务连接正常"

# 下载 API 文档
echo "正在下载 API 文档..."
if curl -s --connect-timeout 10 "$API_DOCS_URL" -o "$API_DOCS_FILE"; then
    echo "API 文档下载成功"
else
    echo "错误: 无法下载 API 文档"
    echo "请检查："
    echo "  1. 后端服务是否正在运行"
    echo "  2. 后端地址是否正确: $BACKEND_URL"
    exit 1
fi

# 生成 API 客户端代码
echo "正在生成 API 客户端代码..."
if [ -d "generated" ]; then
    echo "删除旧的 generated 目录..."
    rm -rf generated
fi

# 使用 openapi-typescript-codegen 生成代码
# 根据 获得接口.txt，使用 openapi 命令
npx openapi --input "$API_DOCS_FILE" --output ./generated --client axios

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "API 客户端代码生成成功！"
    echo "=========================================="
    echo "生成目录: ./generated"
    
    # 清理临时文件
    if [ -f "$API_DOCS_FILE" ]; then
        rm "$API_DOCS_FILE"
        echo "已清理临时文件"
    fi
else
    echo "错误: API 客户端代码生成失败"
    exit 1
fi

