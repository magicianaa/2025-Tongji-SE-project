# AI财报生成功能实现说明

## 功能概述

为电竞酒店管理系统的财报功能添加了AI分析能力，通过调用阿里云千问API，生成专业的财务分析报告和经营建议。

## 实现内容

### 1. 后端实现

#### 1.1 添加依赖 (pom.xml)
- 添加了OpenAI Java SDK (v0.7.2)
- 添加了OkHttp依赖 (v4.12.0)

#### 1.2 创建AI服务
- **QianWenService** 接口: 定义AI财报分析接口
- **QianWenServiceImpl** 实现类: 
  - 使用千问API Key: `sk-bd89293ecbbf4cb7a291bfb5d83496e0`
  - 使用模型: `qwen-plus`
  - 构建详细的提示词，包含所有财务数据
  - 要求AI生成8个维度的专业分析：
    1. 财务状况总结
    2. 收入分析
    3. 成本分析
    4. 利润率分析
    5. 运营效率分析
    6. 风险预警
    7. 改进建议（收入提升、成本控制、运营优化）
    8. 未来展望

#### 1.3 扩展ReportController
- 新增 `/reports/financial/ai-analysis` 接口
- 接受参数：reportType (DAILY/MONTHLY) 和 date
- 先获取财报数据，再调用AI生成分析

### 2. 前端实现

#### 2.1 API调用 (report.js)
- 新增 `generateAIAnalysis(reportType, date)` 方法
- 调用后端AI分析接口

#### 2.2 财报页面更新 (FinancialReport.vue)
- 添加 "AI生成财报分析" 按钮
  - 位置：查询和导出按钮旁边
  - 样式：警告色（橙色）
  - 状态：仅在有报表数据时可点击
- 集成markdown-it库用于渲染AI返回的Markdown格式分析
- 新增AI分析展示卡片
  - 美化的Markdown样式
  - 标题层级样式
  - 列表和引用样式
  - 响应式设计

#### 2.3 添加依赖 (package.json)
- 添加 markdown-it (v14.0.0) 用于Markdown渲染

## 使用流程

1. Admin用户进入"财务报表"页面
2. 选择报表类型（日报/月报）和日期
3. 点击"查询"加载财报数据
4. 点击"AI生成财报分析"按钮
5. 等待AI处理（显示加载状态）
6. 查看生成的专业财报分析
   - 包含财务状况总结
   - 各维度详细分析
   - 具体改进建议
   - 未来经营展望

## 技术特点

1. **智能分析**: AI基于真实财务数据生成专业分析
2. **多维度评估**: 涵盖收入、成本、利润、运营效率等8个方面
3. **可操作建议**: 提供具体的改进措施和优化方案
4. **格式优美**: Markdown格式化输出，易读性强
5. **用户友好**: 简单点击即可获取专业财报分析

## API密钥配置

当前使用的千问API Key已硬编码在 `QianWenServiceImpl.java` 中：
```java
private static final String API_KEY = "sk-bd89293ecbbf4cb7a291bfb5d83496e0";
```

**建议**: 在生产环境中，应将API Key配置到application.yml或环境变量中，避免泄露。

## 测试建议

1. 启动后端服务
2. 安装前端依赖: `npm install`
3. 启动前端: `npm run dev`
4. 登录Admin账号
5. 访问财务报表页面
6. 测试AI生成功能

## 注意事项

1. 需要网络连接访问千问API
2. API调用可能需要几秒钟时间
3. 确保有有效的财报数据才能生成有意义的分析
4. 前端需要安装markdown-it依赖才能正确渲染

## 文件变更清单

### 后端
- `backend/pom.xml` - 添加依赖
- `backend/src/main/java/com/esports/hotel/service/QianWenService.java` - 新建
- `backend/src/main/java/com/esports/hotel/service/impl/QianWenServiceImpl.java` - 新建
- `backend/src/main/java/com/esports/hotel/controller/ReportController.java` - 修改

### 前端
- `frontend/package.json` - 添加依赖
- `frontend/src/api/report.js` - 添加API方法
- `frontend/src/views/admin/FinancialReport.vue` - 添加UI和逻辑
