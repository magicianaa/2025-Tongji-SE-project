# 🐳 Docker部署指南

智慧电竞酒店管理系统 - Docker容器化部署文档

## 📋 前置要求

- Docker Desktop 20.10+
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

## 🚀 快速启动

### 一键启动所有服务

```powershell
# 构建并启动所有容器（首次运行）
docker-compose up -d --build

# 仅启动（已构建过）
docker-compose up -d
```

启动后服务地址：
- **前端界面**: http://localhost
- **后端API**: http://localhost:8080/api
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

### 查看服务状态

```powershell
# 查看所有容器状态
docker-compose ps

# 查看容器日志
docker-compose logs -f              # 所有服务
docker-compose logs -f backend      # 仅后端
docker-compose logs -f frontend     # 仅前端
docker-compose logs -f mysql        # 仅数据库
```

### 停止和清理

```powershell
# 停止所有容器
docker-compose stop

# 停止并删除容器
docker-compose down

# 完全清理（包括数据卷）
docker-compose down -v
```

## 📦 容器架构

```
┌─────────────────────────────────────────────────────┐
│  Frontend (Nginx)       :80                         │
│  ├── Vue 3 SPA                                      │
│  └── API Proxy → Backend                            │
└─────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────┐
│  Backend (Spring Boot)  :8080                       │
│  ├── RESTful API                                    │
│  ├── WebSocket (STOMP)                              │
│  └── JWT Authentication                             │
└─────────────────────────────────────────────────────┘
           │                           │
    ┌──────┴──────┐           ┌───────┴────────┐
┌───────────────────┐     ┌──────────────────────┐
│  MySQL :3306      │     │  Redis :6379         │
│  ├── 26 Tables    │     │  ├── Session Cache   │
│  └── Persistent   │     │  └── Data Cache      │
└───────────────────┘     └──────────────────────┘
```

## 🔧 服务详情

### Frontend（前端）
- **基础镜像**: nginx:1.27-alpine
- **构建**: 多阶段构建（Node.js → Nginx）
- **端口**: 80
- **功能**: 
  - 提供静态文件服务
  - API请求代理到后端
  - WebSocket代理
  - Gzip压缩
  - SPA路由支持

### Backend（后端）
- **基础镜像**: eclipse-temurin:21-jre
- **构建**: Maven多阶段构建
- **端口**: 8080
- **配置**: Spring Profile `docker`
- **健康检查**: `/api/health` 端点
- **JVM参数**: `-Xms512m -Xmx1024m -XX:+UseG1GC`

### MySQL（数据库）
- **镜像**: mysql:8.0
- **端口**: 3306
- **凭据**: 
  - Root密码: `root`
  - 用户: `hotel_user`
  - 密码: `hotel_pass`
  - 数据库: `esports_hotel_db`
- **持久化**: Docker卷 `mysql_data`
- **初始化**: 自动执行 `database/schema.sql`

### Redis（缓存）
- **镜像**: redis:8.0-alpine
- **端口**: 6379
- **持久化**: AOF + Docker卷 `redis_data`

## 🔐 环境变量

可以创建 `.env` 文件自定义配置：

```env
# MySQL配置
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=esports_hotel_db
MYSQL_USER=hotel_user
MYSQL_PASSWORD=your_password

# 应用端口
FRONTEND_PORT=80
BACKEND_PORT=8080
MYSQL_PORT=3306
REDIS_PORT=6379

# JVM参数
JAVA_OPTS=-Xms512m -Xmx1024m
```

## 🛠️ 常用命令

### 构建相关

```powershell
# 重新构建单个服务
docker-compose build backend
docker-compose build frontend

# 强制重新构建（无缓存）
docker-compose build --no-cache

# 拉取最新基础镜像
docker-compose pull
```

### 容器管理

```powershell
# 进入后端容器
docker-compose exec backend sh

# 进入MySQL容器
docker-compose exec mysql mysql -uroot -proot esports_hotel_db

# 进入Redis容器
docker-compose exec redis redis-cli

# 重启单个服务
docker-compose restart backend
```

### 数据管理

```powershell
# 备份MySQL数据
docker-compose exec mysql mysqldump -uroot -proot esports_hotel_db > backup.sql

# 恢复MySQL数据
docker-compose exec -T mysql mysql -uroot -proot esports_hotel_db < backup.sql

# 查看数据卷
docker volume ls | grep java_project2
```

## 📊 监控和调试

### 查看资源使用

```powershell
# 实时资源监控
docker stats

# 查看容器详细信息
docker-compose exec backend java -XshowSettings:vm -version
```

### 日志分析

```powershell
# 实时日志（彩色输出）
docker-compose logs -f --tail=100

# 保存日志到文件
docker-compose logs > logs.txt

# 查看最近50条错误日志
docker-compose logs backend | grep -i error | tail -50
```

### 健康检查

```powershell
# 查看健康状态
docker-compose ps

# 手动测试后端健康
curl http://localhost:8080/api/health

# 测试MySQL连接
docker-compose exec mysql mysqladmin ping -h localhost -uroot -proot
```

## 🚨 故障排查

### 问题1: 容器启动失败

```powershell
# 查看详细错误信息
docker-compose logs backend

# 检查端口占用
netstat -ano | findstr "8080"
netstat -ano | findstr "3306"

# 清理并重启
docker-compose down
docker-compose up -d
```

### 问题2: 数据库连接失败

```powershell
# 确认MySQL已启动并健康
docker-compose ps mysql

# 测试连接
docker-compose exec mysql mysql -uroot -proot -e "SELECT 1"

# 查看后端日志
docker-compose logs backend | grep -i "datasource"
```

### 问题3: 前端无法访问后端

```powershell
# 检查网络连通性
docker-compose exec frontend ping backend

# 检查nginx配置
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# 重启前端
docker-compose restart frontend
```

### 问题4: 构建失败

```powershell
# 清理Docker缓存
docker system prune -a

# 检查磁盘空间
docker system df

# 手动构建查看详细信息
cd backend
docker build -t esports-backend .
```

## 🔄 更新部署

### 代码更新后重新部署

```powershell
# 1. 停止容器
docker-compose down

# 2. 重新构建
docker-compose build

# 3. 启动新版本
docker-compose up -d

# 或者一步完成
docker-compose up -d --build
```

### 仅更新后端

```powershell
docker-compose up -d --build --no-deps backend
```

### 仅更新前端

```powershell
docker-compose up -d --build --no-deps frontend
```

## 📈 性能优化

### 1. 调整JVM内存

编辑 `docker-compose.yml`:
```yaml
backend:
  environment:
    JAVA_OPTS: "-Xms1g -Xmx2g -XX:+UseG1GC"
```

### 2. MySQL优化

```sql
-- 进入MySQL容器
docker-compose exec mysql mysql -uroot -proot

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
```

### 3. Redis监控

```powershell
# 进入Redis CLI
docker-compose exec redis redis-cli

# 查看内存使用
INFO memory

# 查看命中率
INFO stats
```

## 🌐 生产环境部署

### 安全加固

1. **修改默认密码** - 编辑 `docker-compose.yml` 中的数据库密码
2. **使用HTTPS** - 配置nginx SSL证书
3. **关闭调试日志** - 后端使用 `application-docker.yml` 配置
4. **限制端口暴露** - 仅暴露必要的80/443端口

### 推荐配置

```yaml
# docker-compose.prod.yml
services:
  frontend:
    restart: always
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
  
  backend:
    restart: always
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
  
  mysql:
    restart: always
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
```

使用生产配置启动：
```powershell
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 📝 注意事项

1. **首次启动时间**: 约2-3分钟（需要下载镜像、构建、初始化数据库）
2. **数据持久化**: MySQL和Redis数据保存在Docker卷中，`docker-compose down -v` 会删除数据
3. **端口占用**: 确保80、8080、3306、6379端口未被占用
4. **Windows路径**: 如果遇到路径问题，检查Docker Desktop的文件共享设置
5. **内存要求**: 建议Docker Desktop分配至少4GB内存

## 🆘 获取帮助

- 查看容器状态: `docker-compose ps`
- 查看日志: `docker-compose logs -f [service_name]`
- 进入容器: `docker-compose exec [service_name] sh`
- 重启服务: `docker-compose restart [service_name]`

---

**提示**: 首次启动需要等待MySQL完成初始化和后端健康检查，整个过程约需2-3分钟。
