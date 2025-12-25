#!/bin/bash

###############################################################################
# 智慧电竞酒店管理系统 - 一键部署脚本
# 使用方法: sudo bash deploy.sh
###############################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用root权限运行此脚本: sudo bash deploy.sh"
        exit 1
    fi
}

# 检查系统
check_system() {
    log_info "检查操作系统..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
        log_info "检测到系统: $OS $VER"
    else
        log_error "无法检测操作系统"
        exit 1
    fi
}

# 安装Docker
install_docker() {
    if command -v docker &> /dev/null; then
        log_info "Docker已安装: $(docker --version)"
        return
    fi

    log_info "开始安装Docker..."
    
    if [[ "$OS" == *"Ubuntu"* ]] || [[ "$OS" == *"Debian"* ]]; then
        apt-get update
        apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
        echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
        apt-get update
        apt-get install -y docker-ce docker-ce-cli containerd.io
    elif [[ "$OS" == *"CentOS"* ]] || [[ "$OS" == *"Red Hat"* ]]; then
        yum install -y yum-utils
        yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        yum install -y docker-ce docker-ce-cli containerd.io
    else
        log_error "不支持的操作系统"
        exit 1
    fi

    systemctl start docker
    systemctl enable docker
    log_info "Docker安装完成"
}

# 安装Docker Compose
install_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        log_info "Docker Compose已安装: $(docker-compose --version)"
        return
    fi

    log_info "开始安装Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    log_info "Docker Compose安装完成"
}

# 配置防火墙
configure_firewall() {
    log_info "配置防火墙..."
    
    if command -v ufw &> /dev/null; then
        # Ubuntu UFW
        ufw --force enable
        ufw allow 22/tcp
        ufw allow 80/tcp
        ufw allow 443/tcp
        ufw allow 8080/tcp
        ufw allow 8081/tcp
        log_info "UFW防火墙配置完成"
    elif command -v firewall-cmd &> /dev/null; then
        # CentOS firewalld
        systemctl start firewalld
        systemctl enable firewalld
        firewall-cmd --permanent --add-port=22/tcp
        firewall-cmd --permanent --add-port=80/tcp
        firewall-cmd --permanent --add-port=443/tcp
        firewall-cmd --permanent --add-port=8080/tcp
        firewall-cmd --permanent --add-port=8081/tcp
        firewall-cmd --reload
        log_info "firewalld防火墙配置完成"
    else
        log_warn "未检测到防火墙，请手动配置"
    fi
}

# 创建环境变量文件
create_env_file() {
    log_info "创建环境变量文件..."
    
    if [ -f .env ]; then
        log_warn ".env文件已存在，跳过创建"
        return
    fi

    # 生成随机密码
    MYSQL_ROOT_PASS=$(openssl rand -base64 32)
    MYSQL_PASS=$(openssl rand -base64 32)
    REDIS_PASS=$(openssl rand -base64 32)
    JWT_SECRET=$(openssl rand -base64 32)

    cat > .env << EOF
# 数据库配置
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASS}
MYSQL_DATABASE=esports_hotel
MYSQL_USER=esports_user
MYSQL_PASSWORD=${MYSQL_PASS}

# Redis配置
REDIS_PASSWORD=${REDIS_PASS}

# 应用配置
SPRING_PROFILES_ACTIVE=docker
JWT_SECRET=${JWT_SECRET}
BACKEND_PORT=8080
FRONTEND_PORT=8081

# 时区
TZ=Asia/Shanghai
EOF

    chmod 600 .env
    log_info ".env文件创建完成"
    log_info "请妥善保管以下密码信息："
    echo "----------------------------------------"
    echo "MySQL Root密码: ${MYSQL_ROOT_PASS}"
    echo "MySQL用户密码: ${MYSQL_PASS}"
    echo "Redis密码: ${REDIS_PASS}"
    echo "JWT密钥: ${JWT_SECRET}"
    echo "----------------------------------------"
    echo "以上信息已保存到 .env 文件"
}

# 构建并启动服务
deploy_services() {
    log_info "开始构建Docker镜像..."
    docker-compose build

    log_info "启动服务..."
    docker-compose up -d

    log_info "等待服务启动..."
    sleep 10

    log_info "检查服务状态..."
    docker-compose ps
}

# 验证部署
verify_deployment() {
    log_info "验证部署..."
    
    # 检查容器状态
    RUNNING=$(docker-compose ps | grep -c "Up")
    if [ "$RUNNING" -ge 4 ]; then
        log_info "✓ 所有容器运行正常"
    else
        log_error "✗ 部分容器未正常运行"
        docker-compose ps
    fi

    # 检查端口
    if netstat -tuln | grep -q ":8081"; then
        log_info "✓ 前端端口8081已开放"
    else
        log_warn "✗ 前端端口8081未开放"
    fi

    if netstat -tuln | grep -q ":8080"; then
        log_info "✓ 后端端口8080已开放"
    else
        log_warn "✗ 后端端口8080未开放"
    fi
}

# 显示访问信息
show_access_info() {
    SERVER_IP=$(curl -s ifconfig.me || echo "请手动获取服务器IP")
    
    echo ""
    echo "========================================"
    echo "  部署完成！"
    echo "========================================"
    echo ""
    echo "访问地址："
    echo "  前端: http://${SERVER_IP}:8081"
    echo "  后端: http://${SERVER_IP}:8080"
    echo ""
    echo "默认账号："
    echo "  管理员: admin / 123456"
    echo "  前台: staff001 / 123456"
    echo "  住客: 13800138000 / 123456"
    echo ""
    echo "常用命令："
    echo "  查看日志: docker-compose logs -f"
    echo "  重启服务: docker-compose restart"
    echo "  停止服务: docker-compose down"
    echo "  更新服务: docker-compose up -d --build"
    echo ""
    echo "详细文档: DEPLOYMENT_GUIDE.md"
    echo "========================================"
}

# 主流程
main() {
    echo ""
    echo "========================================"
    echo "  智慧电竞酒店管理系统"
    echo "  一键部署脚本"
    echo "========================================"
    echo ""

    check_root
    check_system
    install_docker
    install_docker_compose
    configure_firewall
    create_env_file
    deploy_services
    verify_deployment
    show_access_info
}

# 执行主流程
main
