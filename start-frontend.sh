#!/bin/bash

# 启动前端服务脚本

set -e

echo "=========================================="
echo "启动 OJ 前端服务"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}错误: 未找到 Node.js，请先运行 setup-wsl.sh 配置环境${NC}"
    exit 1
fi

# 检查 npm
if ! command -v npm &> /dev/null; then
    echo -e "${RED}错误: 未找到 npm，请先运行 setup-wsl.sh 配置环境${NC}"
    exit 1
fi

# 进入前端目录
cd oj-frontend

# 检查 node_modules
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}检测到 node_modules 不存在，正在安装依赖...${NC}"
    npm install
    echo -e "${GREEN}依赖安装完成${NC}"
fi

# 检查 generated 目录
if [ ! -d "generated" ]; then
    echo ""
    echo -e "${YELLOW}⚠️  警告: 未找到 generated 目录（API 客户端代码）${NC}"
    echo -e "${YELLOW}前端需要 API 客户端代码才能正常运行${NC}"
    echo ""
    echo "请先："
    echo "  1. 确保后端服务已启动 (http://localhost:8121)"
    echo "  2. 运行以下命令生成 API 客户端代码："
    echo "     cd oj-frontend"
    echo "     ./generate-api.sh"
    echo ""
    echo "或者从项目根目录运行："
    echo "     ./generate-api.sh"
    echo ""
    
    # 检查是否在交互式终端
    if [ -t 0 ]; then
        read -p "是否现在尝试生成 API 客户端代码？(y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            if [ -f "generate-api.sh" ]; then
                chmod +x generate-api.sh
                ./generate-api.sh
            else
                echo -e "${RED}错误: 未找到 generate-api.sh 脚本${NC}"
                exit 1
            fi
        else
            echo -e "${YELLOW}跳过 API 客户端代码生成，前端可能无法正常运行${NC}"
            echo "按 Ctrl+C 停止，或按 Enter 继续（可能会失败）..."
            read
        fi
    else
        echo -e "${RED}非交互式终端，跳过自动生成${NC}"
        echo "请手动运行生成脚本后再启动前端"
        exit 1
    fi
fi

# 启动服务
echo ""
echo -e "${GREEN}正在启动前端开发服务器...${NC}"
echo "前端地址: http://localhost:8080"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

npm run serve

