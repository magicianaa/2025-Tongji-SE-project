# 服务器部署快速参考

## 🚀 快速开始（5分钟部署）

### 方式1: 一键脚本部署
```bash
# 1. 上传项目到服务器
scp -r ./2025-Tongji-SE-project root@your_server_ip:/opt/

# 2. 运行部署脚本
ssh root@your_server_ip
cd /opt/2025-Tongji-SE-project
chmod +x deploy.sh
./deploy.sh
```

### 方式2: 手动部署
```bash
# 1. 安装Docker和Docker Compose
curl -fsSL https://get.docker.com | sh
curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 2. 克隆或上传项目
cd /opt
git clone <your-repo-url> esports-hotel
cd esports-hotel

# 3. 修改密码（重要！）
nano docker-compose.yml
# 修改 MYSQL_ROOT_PASSWORD、MYSQL_PASSWORD、Redis密码、JWT_SECRET

# 4. 启动服务
docker-compose up -d
```

## 📋 必备配置清单

- [ ] 服务器准备（2核4G以上）
- [ ] Docker安装
- [ ] Docker Compose安装
- [ ] 防火墙配置（开放80,443,8080,8081端口）
- [ ] **修改 docker-compose.yml 中的默认密码**
- [ ] 域名解析（可选）
- [ ] SSL证书（可选）

## 🔑 关键文件

| 文件 | 说明 |
|------|------|
| `docker-compose.yml` | Docker部署配置（开发+生产） |
| `deploy.sh` | 一键部署脚本 |
| `nginx.conf.example` | Nginx配置示例 |
| `DEPLOYMENT_GUIDE.md` | 完整部署文档 |

## 🛠️ 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
docker-compose logs -f backend  # 只看后端日志

# 重启服务
docker-compose restart
docker-compose restart backend  # 只重启后端

# 停止服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build

# 进入容器
docker exec -it esports-hotel-backend bash
docker exec -it esports-hotel-mysql mysql -uroot -p

# 查看资源占用
docker stats

# 清理未使用的资源
docker system prune -f
```

## 🔐 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 前台 | staff001 | 123456 |
| 住客 | 13800138000 | 123456 |

**⚠️ 生产环境请立即修改默认密码！**

## 🌐 访问地址

- **前端**: http://your_server_ip:8081
- **后端API**: http://your_server_ip:8080
- **健康检查**: http://your_server_ip:8080/actuator/health

## 📊 端口说明

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | HTTP | Nginx（可选） |
| 443 | HTTPS | Nginx SSL（可选） |
| 3307 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 8080 | Backend | Spring Boot后端 |
| 8081 | Frontend | Nginx前端 |

## 💾 数据备份

```bash
# 手动备份数据库
docker exec esports-hotel-mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} esports_hotel > backup.sql

# 手动备份Redis
docker exec esports-hotel-redis redis-cli -a ${REDIS_PASSWORD} SAVE
docker cp esports-hotel-redis:/data/dump.rdb ./redis_backup.rdb

# 自动备份（设置定时任务）
0 2 * * * /opt/esports-hotel/backup.sh
```

## 🔄 更新部署

```bash
# 更新代码
git pull

# 重新构建并部署
docker-compose down
docker-compose build
docker-compose up -d

# 查看新版本日志
docker-compose logs -f
```

## ⚠️ 故障排查

### 问题1: 容器无法启动
```bash
# 查看详细日志
docker-compose logs backend

# 检查端口占用
netstat -tuln | grep 8080

# 重启Docker服务
systemctl restart docker
```

### 问题2: 数据库连接失败
```bash
# 测试MySQL连接
docker exec -it esports-hotel-mysql mysql -uroot -p

# 检查网络
docker network ls
docker network inspect esports-network
```

### 问题3: 内存不足
```bash
# 查看内存使用
free -h

# 增加swap
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## 📱 监控检查

```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health

# 查看容器资源使用
docker stats

# 查看磁盘空间
df -h

# 查看日志大小
du -sh backend/logs/
```

## 🔒 安全建议

1. ✅ 修改所有默认密码
2. ✅ 配置防火墙规则
3. ✅ 启用HTTPS（Let's Encrypt）
4. ✅ 限制数据库外网访问
5. ✅ 定期备份数据
6. ✅ 定期更新系统和Docker
7. ✅ 配置日志轮转
8. ✅ 监控异常访问

## 📞 技术支持

- 📖 完整文档: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- 🐛 问题反馈: GitHub Issues
- 📧 联系方式: 系统管理员

---

**快速启动命令总结：**
```bash
# 首次部署
./deploy.sh

# 日常运维
docker-compose up -d      # 启动
docker-compose logs -f    # 查看日志
docker-compose restart    # 重启
docker-compose down       # 停止
```
