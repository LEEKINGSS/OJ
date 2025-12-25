#!/bin/bash

# 同时启动前后端服务脚本

set -e

echo "=========================================="
echo "启动 OJ 前后端服务"
echo "=========================================="

# 加载环境变量
if [ -f "$HOME/.oj_env" ]; then
    source "$HOME/.oj_env"
fi

# 创建日志目录
LOG_DIR="./logs"
mkdir -p "$LOG_DIR"

# 定义清理函数
cleanup() {
    echo ""
    echo "正在停止所有服务..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
    wait $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
    echo "所有服务已停止"
    exit 0
}

# 注册清理函数
trap cleanup SIGINT SIGTERM

# 启动后端
echo "启动后端服务..."
cd oj-backend
mvn spring-boot:run > "../$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
cd ..
echo "后端服务已启动 (PID: $BACKEND_PID)"
echo "后端日志: $LOG_DIR/backend.log"

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 启动前端
echo "启动前端服务..."
cd oj-frontend
npm run serve > "../$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
cd ..
echo "前端服务已启动 (PID: $FRONTEND_PID)"
echo "前端日志: $LOG_DIR/frontend.log"

echo ""
echo "=========================================="
echo "服务启动完成！"
echo "=========================================="
echo ""
echo "后端地址: http://localhost:${SERVER_PORT:-8121}/api"
echo "API 文档: http://localhost:${SERVER_PORT:-8121}/api/doc.html"
echo "前端地址: http://localhost:8080"
echo ""
echo "日志文件:"
echo "  后端: $LOG_DIR/backend.log"
echo "  前端: $LOG_DIR/frontend.log"
echo ""
echo "按 Ctrl+C 停止所有服务"
echo ""

# 等待进程
wait $BACKEND_PID $FRONTEND_PID

