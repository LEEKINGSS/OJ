#!/bin/bash

# OJ 项目 WSL 环境配置脚本
# 用于在 WSL 上配置项目运行环境

set -e  # 遇到错误立即退出

echo "=========================================="
echo "OJ 项目 WSL 环境配置脚本"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否在 WSL 环境
if ! grep -qEi "(Microsoft|WSL)" /proc/version &> /dev/null ; then
    echo -e "${YELLOW}警告: 未检测到 WSL 环境，但将继续执行...${NC}"
fi

# 1. 更新系统包
echo -e "\n${GREEN}[1/8] 更新系统包...${NC}"
sudo apt update
sudo apt upgrade -y

# 2. 安装 Java 8
echo -e "\n${GREEN}[2/8] 检查并安装 Java 8...${NC}"
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "1.8"; then
    echo "安装 Java 8..."
    sudo apt install -y openjdk-8-jdk
    echo "设置 JAVA_HOME..."
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
    echo "export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64" >> ~/.bashrc
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
else
    echo "Java 8 已安装"
    java -version
fi

# 3. 安装 Maven
echo -e "\n${GREEN}[3/8] 检查并安装 Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    echo "安装 Maven..."
    sudo apt install -y maven
else
    echo "Maven 已安装"
    mvn -version
fi

# 4. 安装 Node.js 和 npm
echo -e "\n${GREEN}[4/8] 检查并安装 Node.js 和 npm...${NC}"
if ! command -v node &> /dev/null; then
    echo "安装 Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt install -y nodejs
else
    echo "Node.js 已安装"
    node -v
fi

if ! command -v npm &> /dev/null; then
    echo "npm 未找到，但通常随 Node.js 一起安装"
else
    echo "npm 已安装"
    npm -v
fi

# 5. 安装 Vue CLI
echo -e "\n${GREEN}[5/8] 检查并安装 Vue CLI...${NC}"
if ! command -v vue &> /dev/null; then
    echo "全局安装 Vue CLI..."
    # 临时禁用 set -e，允许 Vue CLI 安装失败
    set +e
    # 先尝试不使用 sudo 安装（如果 npm 配置了用户目录）
    if npm install -g @vue/cli 2>/dev/null; then
        echo "Vue CLI 安装成功（用户目录）"
    else
        # 如果失败，使用 sudo 并传递 PATH 环境变量
        echo "尝试使用 sudo 安装（需要传递 PATH 环境变量）..."
        if sudo env PATH="$PATH" npm install -g @vue/cli 2>/dev/null; then
            echo "Vue CLI 安装成功"
        else
            echo -e "${YELLOW}警告: Vue CLI 安装失败，但可以继续。${NC}"
            echo -e "${YELLOW}提示: 项目可以使用 npx vue-cli-service，不一定需要全局安装 Vue CLI${NC}"
            echo -e "${YELLOW}如需手动安装，可以运行: npm install -g @vue/cli${NC}"
            echo -e "${YELLOW}或者配置 npm 使用用户目录: mkdir -p ~/.npm-global && npm config set prefix '~/.npm-global' && export PATH=~/.npm-global/bin:\$PATH${NC}"
        fi
    fi
    # 重新启用 set -e
    set -e
else
    echo "Vue CLI 已安装"
    vue --version
fi

# 6. 配置环境变量
echo -e "\n${GREEN}[6/8] 配置环境变量...${NC}"
ENV_FILE="$HOME/.oj_env"
cat > "$ENV_FILE" << 'EOF'
# OJ 项目环境变量配置
# 数据库配置
export MYSQL_URL="jdbc:mysql://172.23.216.95:3306/oj-database?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
export MYSQL_USER="root"
export MYSQL_PASSWORD="123.com"

# 代码沙箱配置
export CODESANDBOX_TYPE="remote"
export CODESANDBOX_URL="http://172.22.232.42:8122/api/code/executeCode"

# 后端服务端口
export SERVER_PORT=8121
EOF

# 将环境变量添加到 .bashrc
if ! grep -q "source $ENV_FILE" ~/.bashrc; then
    echo "" >> ~/.bashrc
    echo "# OJ 项目环境变量" >> ~/.bashrc
    echo "source $ENV_FILE" >> ~/.bashrc
    echo "环境变量文件已添加到 ~/.bashrc"
else
    echo "环境变量已存在于 ~/.bashrc"
fi

# 立即加载环境变量
source "$ENV_FILE"

echo -e "${GREEN}环境变量配置完成！${NC}"
echo "环境变量文件位置: $ENV_FILE"

# 7. 安装后端依赖
echo -e "\n${GREEN}[7/8] 安装后端依赖...${NC}"
cd oj-backend
if [ -f "pom.xml" ]; then
    echo "正在下载 Maven 依赖，这可能需要一些时间..."
    mvn clean install -DskipTests
    echo -e "${GREEN}后端依赖安装完成！${NC}"
else
    echo -e "${RED}错误: 未找到 pom.xml 文件${NC}"
    exit 1
fi
cd ..

# 8. 安装前端依赖
echo -e "\n${GREEN}[8/8] 安装前端依赖...${NC}"
cd oj-frontend
if [ -f "package.json" ]; then
    echo "正在安装 npm 依赖，这可能需要一些时间..."
    npm install
    echo -e "${GREEN}前端依赖安装完成！${NC}"
else
    echo -e "${RED}错误: 未找到 package.json 文件${NC}"
    exit 1
fi
cd ..

# 完成
echo -e "\n${GREEN}=========================================="
echo "环境配置完成！"
echo "==========================================${NC}"
echo ""
echo "已安装的软件:"
echo "  - Java 8"
echo "  - Maven"
echo "  - Node.js"
echo "  - npm"
echo "  - Vue CLI"
echo ""
echo "环境变量配置:"
echo "  - 数据库: 172.23.216.95:3306"
echo "  - 代码沙箱: 172.22.232.42:8122"
echo ""
echo -e "${YELLOW}提示: 请重新打开终端或运行 'source ~/.bashrc' 以加载环境变量${NC}"
echo ""
echo "启动命令:"
echo "  后端: cd oj-backend && mvn spring-boot:run"
echo "  前端: cd oj-frontend && npm run serve"
echo ""

