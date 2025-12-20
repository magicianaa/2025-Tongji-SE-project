# 重建后端脚本
Write-Host "停止并删除后端容器..."
docker-compose stop backend
docker-compose rm -f backend

Write-Host "`n清理Docker构建缓存..."
docker builder prune -f

Write-Host "`n重新构建后端（无缓存）..."
docker-compose build --no-cache --pull backend

Write-Host "`n启动后端..."
docker-compose up -d backend

Write-Host "`n等待后端启动..."
Start-Sleep -Seconds 10

Write-Host "`n查看后端日志..."
docker-compose logs --tail=30 backend
