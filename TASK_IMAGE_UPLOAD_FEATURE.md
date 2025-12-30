# 任务凭证图片上传功能实现说明

## 修改概览

本次修改将任务提交功能从**手动输入URL**改为**直接上传图片文件**，图片将保存到服务器本地文件夹，并将URL记录到数据库。

## 修改内容

### 后端修改

#### 1. 新增文件：`FileUploadController.java`
- **位置**: `backend/src/main/java/com/esports/hotel/controller/FileUploadController.java`
- **功能**: 
  - 处理任务凭证图片上传
  - 验证文件类型（只允许图片）
  - 限制文件大小（最大5MB）
  - 按日期分类存储文件（格式：`uploads/task-proofs/yyyy-MM-dd/uuid.ext`）
  - 返回文件访问URL

#### 2. 新增文件：`FileAccessController.java`
- **位置**: `backend/src/main/java/com/esports/hotel/controller/FileAccessController.java`
- **功能**:
  - 提供文件访问接口 `/api/files/**`
  - 根据文件扩展名设置正确的Content-Type
  - 支持图片在浏览器中直接显示

#### 3. 修改文件：`application.yml`
- **新增配置**:
  ```yaml
  # Spring文件上传配置
  spring:
    servlet:
      multipart:
        enabled: true
        max-file-size: 10MB
        max-request-size: 10MB
  
  # 自定义文件上传配置
  file:
    upload:
      path: uploads/
      url-prefix: /api/files/
  ```

#### 4. 文件存储目录
- **位置**: `backend/uploads/`
- **结构**: 
  ```
  uploads/
  └── task-proofs/
      └── 2025-12-27/
          ├── uuid1.jpg
          ├── uuid2.png
          └── ...
  ```

### 前端修改

#### 1. 新增文件：`upload.js`
- **位置**: `frontend/src/api/upload.js`
- **功能**: 封装文件上传API调用

#### 2. 修改文件：`TasksAndPoints.vue`
- **主要变更**:
  - 将 `<el-input>` 改为 `<el-upload>` 组件
  - 添加文件上传前验证（文件类型和大小）
  - 添加上传成功/失败处理函数
  - 上传成功后自动填充proofImageUrl
  - 只有上传图片后才能提交任务

## 使用流程

### 用户操作流程
1. 用户点击"提交凭证"按钮
2. 在弹出对话框中点击上传区域
3. 选择本地图片文件（jpg/png/gif等）
4. 图片自动上传到服务器
5. 填写完成说明（可选）
6. 点击提交按钮完成任务提交

### 技术流程
1. 前端通过 `el-upload` 组件选择文件
2. 文件自动发送到 `/api/upload/task-proof` 接口
3. 后端验证文件类型和大小
4. 保存文件到 `uploads/task-proofs/日期/文件名`
5. 返回文件访问URL（如：`/api/files/task-proofs/2025-12-27/abc123.jpg`）
6. 前端将URL填充到表单
7. 用户提交任务时，URL保存到数据库的 `proof_image_url` 字段
8. 审核员可以通过URL查看图片

## API接口

### 文件上传接口
- **URL**: `POST /api/upload/task-proof`
- **Content-Type**: `multipart/form-data`
- **参数**: 
  - `file`: 图片文件（必填）
- **返回**:
  ```json
  {
    "code": 200,
    "msg": "文件上传成功",
    "data": {
      "url": "/api/files/task-proofs/2025-12-27/abc123.jpg",
      "filename": "abc123.jpg"
    }
  }
  ```

### 文件访问接口
- **URL**: `GET /api/files/**`
- **示例**: `GET /api/files/task-proofs/2025-12-27/abc123.jpg`
- **返回**: 图片文件（二进制流）

## 安全性考虑

1. **文件类型验证**: 只允许上传图片文件
2. **文件大小限制**: 最大5MB
3. **文件名随机化**: 使用UUID避免文件名冲突和路径遍历攻击
4. **Token验证**: 上传接口需要携带Authorization header
5. **按日期分类**: 避免单个目录文件过多

## 注意事项

1. **生产环境部署**:
   - 确保 `uploads/` 目录有写入权限
   - 考虑使用CDN或对象存储服务（如阿里云OSS）存储图片
   - 定期清理过期图片

2. **Docker部署**:
   - 需要将 `uploads/` 目录挂载为Docker volume
   - 确保容器重启后文件不丢失

3. **性能优化**:
   - 可以添加图片压缩功能
   - 考虑添加缩略图生成
   - 使用Nginx直接提供静态文件服务

## 数据库字段

数据库表 `tb_task_record` 中的字段无需修改：
- `proof_image_url` VARCHAR(255): 存储图片访问URL

## 测试建议

1. 测试上传不同格式的图片（jpg, png, gif）
2. 测试上传超过5MB的大文件（应该被拒绝）
3. 测试上传非图片文件（应该被拒绝）
4. 测试图片URL是否可以正常访问
5. 测试任务提交和审核流程

## 后续优化建议

1. 添加图片压缩功能，减少存储空间
2. 添加图片水印，防止滥用
3. 集成云存储服务（OSS/S3）
4. 添加图片预览功能
5. 支持批量上传多张图片
