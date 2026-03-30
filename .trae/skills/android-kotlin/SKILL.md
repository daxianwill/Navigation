---
name: "android-kotlin"
description: "基于 https://github.com/alinaqi/claude-bootstrap 的Android Kotlin项目初始化系统，安全优先、规范驱动、AI原生。Invoke when initializing Android Kotlin projects or setting up AI-native development workflows."
---

# Android Kotlin 项目初始化系统

基于 [alinaqi/claude-bootstrap](https://github.com/alinaqi/claude-bootstrap) 仓库的生产级Android Kotlin项目初始化系统，提供安全优先、规范驱动、AI原生的开发工作流程。

## 核心特性

- **代理团队协作**：每个项目作为协调的AI代理团队运行
- **严格TDD流程**：规范 > 测试 > 失败 > 实现 > 通过 > 审查 > 安全 > PR
- **多层代码图**：持久化知识图谱，支持符号查找、依赖分析和影响范围分析
- **安全默认配置**：无代码中的密钥、依赖项扫描、预提交钩子、CI强制执行
- **强制性代码审查**：每次提交都需要 /code-review 后才能推送

## 核心代理团队

| 代理 | 职责 |
|------|------|
| **Team Lead** | 协调所有代理，管理工作流程 |
| **Quality Agent** | 强制执行TDD，确保测试覆盖率 |
| **Security Agent** | 扫描漏洞，确保安全合规 |
| **Code Review Agent** | 运行多引擎代码审查 |
| **Merger Agent** | 创建PR，管理合并流程 |
| **Feature Agents** | 并行实现各个功能 |

## 不可变开发管道

所有项目都遵循严格的开发管道：

```
Spec → Tests → Fail → Implement → Pass → Review → Security → PR
```

### 功能开发流程
1. **编写规范**：定义功能需求
2. **编写测试**：创建测试用例
3. **观察失败**：运行测试，确认失败
4. **实现功能**：编写代码使测试通过
5. **验证通过**：确保所有测试通过
6. **代码审查**：执行多引擎审查
7. **安全扫描**：检查安全漏洞
8. **创建PR**：提交合并请求

### 漏洞修复流程
1. **发现测试缺口**：识别未覆盖的场景
2. **编写失败测试**：创建复现问题的测试
3. **修复问题**：编写代码修复漏洞
4. **验证通过**：确保测试通过

## 核心哲学

### 迭代循环（默认）
每个任务在自引用循环中运行，直到测试通过。Claude自主迭代，你只需描述"什么"，而不是"如何"。

### 测试优先，始终如一
- 功能：编写测试 → 观察失败 → 实现 → 通过
- 漏洞：发现测试缺口 → 编写失败测试 → 修复 → 通过
- 没有代码可以在没有先失败的测试的情况下发布

### 简洁性不可协商
- 每个函数20行
- 每个文件200行
- 最多3个参数
- 如果你不能在一个会话中理解整个系统，那它就太复杂了

### 默认安全
- 代码中无密钥
- 客户端环境变量中无密钥
- 依赖项扫描
- 预提交钩子
- CI强制执行

### 强制性代码审查
- 每次提交在推送前都需要 /code-review
- 🔴 严重 + 🟠 高 = 阻止
- 🟡 中 + 🟢 低 = 可以发布
- AI发现人类遗漏的，人类发现AI遗漏的

### 默认代理团队
每个项目作为协调的AI代理团队运行：
- Team Lead + Quality + Security + Review + Merger + Features
- 严格的管道执行

## 快速开始

### 1. 初始化项目

```bash
# 使用claude-bootstrap初始化新项目
npx claude-bootstrap init my-android-app --template android-kotlin
```

### 2. 配置代理团队

在项目根目录创建 `.claude/agents.json`：

```json
{
  "team": {
    "lead": "enabled",
    "quality": "enabled",
    "security": "enabled",
    "review": "enabled",
    "merger": "enabled"
  },
  "pipeline": {
    "specFirst": true,
    "testFirst": true,
    "reviewMandatory": true,
    "securityScan": true
  }
}
```

### 3. 开始开发

```bash
# 开始新功能开发
/claude feature start "用户认证功能"

# 运行测试
/claude test run

# 执行代码审查
/claude code-review

# 安全扫描
/claude security scan

# 创建PR
/claude pr create
```

## 代码图系统

### 层级代码图（Tiered Code Graph）

每个项目通过MCP获得持久化知识图谱：
- **符号查找**：亚毫秒级符号解析
- **依赖分析**：实时依赖关系图
- **影响范围分析**：评估变更影响
- **CPG分析**：可选的Joern + CodeQL分析
  - AST（抽象语法树）
  - CFG（控制流图）
  - PDG（程序依赖图）
  - 深度数据流和安全审计

## 配置选项

### 项目配置

在 `claude-bootstrap.json` 中配置：

```json
{
  "project": {
    "name": "my-android-app",
    "type": "android-kotlin",
    "version": "1.0.0"
  },
  "quality": {
    "testCoverage": 80,
    "maxFunctionLines": 20,
    "maxFileLines": 200,
    "maxParams": 3
  },
  "security": {
    "dependencyScan": true,
    "secretDetection": true,
    "preCommitHooks": true,
    "ciEnforcement": true
  },
  "review": {
    "multiEngine": true,
    "blockCritical": true,
    "blockHigh": true
  }
}
```

## 常用命令

```bash
# 项目管理
/claude project init           # 初始化新项目
/claude project info           # 显示项目信息
/claude project config         # 配置项目

# 开发工作流
/claude feature start <name>   # 开始新功能
/claude feature status         # 查看功能状态
/claude feature complete       # 完成功能

# 质量保证
/claude test run               # 运行测试
/claude test coverage          # 查看测试覆盖率
/claude lint run               # 运行代码检查

# 安全
/claude security scan          # 安全扫描
/claude secrets check          # 密钥检查
/claude dependencies audit     # 依赖审计

# 代码审查
/claude code-review            # 执行代码审查
/claude review status          # 审查状态

# Git工作流
/claude pr create              # 创建PR
/claude pr review              # 审查PR
/claude pr merge               # 合并PR
```

## 最佳实践

### 1. 规范驱动开发
- 在编写代码前，先编写清晰的规范
- 使用 `/claude spec create` 创建规范文档
- 确保规范包含验收标准

### 2. 测试优先
- 始终先编写测试
- 观察测试失败，确认测试有效
- 然后实现代码使测试通过
- 使用 `/claude test generate` 生成测试

### 3. 保持简洁
- 遵循20/200/3规则
- 定期重构复杂的函数
- 使用 `/claude refactor suggest` 获取重构建议

### 4. 安全第一
- 永远不要在代码中提交密钥
- 使用环境变量管理敏感信息
- 定期运行安全扫描

### 5. 代码审查
- 每次提交都进行审查
- 认真对待审查反馈
- 使用AI辅助审查，但不要完全依赖

## 故障排除

### 代理未响应
```bash
# 重启代理团队
/claude agents restart

# 查看代理日志
/claude agents logs
```

### 测试失败
```bash
# 查看详细测试输出
/claude test run --verbose

# 生成测试报告
/claude test report
```

### 安全问题
```bash
# 查看安全报告
/claude security report

# 修复依赖漏洞
/claude dependencies fix
```

## 集成工具

- **测试框架**：JUnit, Espresso
- **代码质量**：ktlint, detekt
- **安全扫描**： dependency-check, gitleaks
- **CI/CD**：GitHub Actions, GitLab CI
- **代码审查**：多引擎审查系统

## 版本兼容性

- **Android Gradle Plugin**: 7.0+
- **Kotlin**: 1.8+
- **Java**: 11+
- **Gradle**: 7.0+