#!/bin/bash

echo "======================================"
echo "重新部署WebSocket修复"
echo "======================================"

# 重新构建后端
echo ""
echo "1. 重新构建后端..."
docker compose build backend

# 重新构建前端
echo ""
echo "2. 重新构建前端..."
docker compose build frontend

# 重启所有服务
echo ""
echo "3. 重启服务..."
docker compose up -d

# 等待服务启动
echo ""
echo "4. 等待服务启动..."
sleep 10

# 查看后端日志
echo ""
echo "5. 查看后端启动日志..."
docker compose logs --tail=50 backend

echo ""
echo "======================================"
echo "部署完成！"
echo "======================================"
echo ""
echo "测试步骤："
echo "1. 打开两个浏览器（或隐私窗口），分别用两个账号登录"
echo "2. 打开浏览器控制台 (F12)"
echo "3. 进入'组队招募'页面"
echo "4. 一边发布招募，另一边点击申请"
echo "5. 观察控制台和弹窗"
echo ""
echo "查看WebSocket调试日志："
echo "  docker compose logs -f backend | grep -i 'websocket\\|推送\\|订阅\\|MESSAGE'"
echo ""
