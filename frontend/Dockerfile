# 阶段1: 构建阶段
FROM node:22-alpine AS build

WORKDIR /app

# 复制package文件并安装所有依赖（包括devDependencies用于构建）
COPY package*.json ./
RUN npm ci --silent

# 复制源代码并构建
COPY . .
RUN npm run build

# 阶段2: 生产环境 - 使用nginx提供静态文件
FROM nginx:1.27-alpine

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
