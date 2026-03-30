---
name: "claude-agents"
description: "基于 https://github.com/wshobson/agents 的智能代理系统，提供112个专业代理、146个技能和72个插件，用于代码开发、智能自动化和多代理编排。Invoke when needing specialized AI agents for code development, architecture design, or workflow orchestration."
---

# Claude Agents 智能代理系统

基于 [wshobson/agents](https://github.com/wshobson/agents) 仓库的生产级智能代理系统，提供112个专业AI代理、16个多代理工作流程编排器、146个代理技能和79个开发工具，组织成72个专注的单一用途插件。

## 核心特性

- **72个专注插件**：粒度化、单一用途插件，优化了token使用和可组合性
- **112个专业代理**：涵盖架构、语言、基础设施、质量、数据/AI、文档、业务运营和SEO等领域的深度专家
- **146个代理技能**：模块化知识包，支持渐进式披露，提供专业知识
- **16个工作流程编排器**：用于复杂操作的多代理协调系统，如全栈开发、安全加固、ML管道和事件响应
- **79个开发工具**：优化的实用工具，包括项目脚手架、安全扫描、测试自动化和基础设施设置

## 快速开始

### 1. 添加市场

将此市场添加到Claude Code：
```
/plugin marketplace add wshobson/agents
```

### 2. 安装插件

浏览可用插件：
```
/plugin
```

安装所需插件：
```
# 基本开发插件
/plugin install python-development          # Python with 16 specialized skills
/plugin install javascript-typescript       # JS/TS with 4 specialized skills
/plugin install backend-development         # Backend APIs with 3 architecture skills

# 基础设施与运维
/plugin install kubernetes-operations       # K8s with 4 deployment skills
/plugin install cloud-infrastructure        # AWS/Azure/GCP with 4 cloud skills

# 安全与质量
/plugin install security-scanning           # SAST with security skill
/plugin install comprehensive-review       # Multi-perspective code analysis

# 全栈编排
/plugin install full-stack-orchestration   # Multi-agent workflows
```

## 插件与代理关系

你安装插件，插件捆绑代理：

| 插件 | 代理 |
|------|------|
| comprehensive-review | architect-review, code-reviewer, security-auditor |
| javascript-typescript | javascript-pro, typescript-pro |
| python-development | python-pro, django-pro, fastapi-pro |
| blockchain-web3 | blockchain-developer |

**错误用法**：不能直接安装代理
```
# ❌ 错误
/plugin install typescript-pro

# ✅ 正确
/plugin install javascript-typescript@claude-code-workflows
```

## 工作原理

每个插件完全隔离，拥有自己的代理、命令和技能：

- **仅安装所需内容**：每个插件仅加载其特定的代理、命令和技能
- **最小化token使用**：不会将不必要的资源加载到上下文中
- **灵活组合**：组合多个插件以实现复杂工作流程
- **清晰边界**：每个插件都有单一、专注的用途
- **渐进式披露**：技能仅在激活时加载知识

## 故障排除

**"Plugin not found"** → 使用插件名称，而不是代理名称。添加 `@claude-code-workflows` 后缀。

**插件未加载** → 清除缓存并重新安装：
```
rm -rf ~/.claude/plugins/cache/claude-code-workflows && rm ~/.claude/plugins/installed_plugins.json
```

## 核心文档

- **插件参考**：所有72个插件的完整目录
- **代理参考**：所有112个代理按类别组织
- **代理技能**：146个专业技能，支持渐进式披露
- **使用指南**：命令、工作流程和最佳实践
- **架构**：设计原则和模式
- **PluginEval**：质量评估框架（层次、维度、评分）

## 模型配置

- **Opus 4.6**：用于复杂决策和架构设计
- **Sonnet 4.6**：用于一般开发和代码生成
- **Haiku 4.5**：用于快速任务和简单操作

## 三层模型策略

系统采用三层模型策略以实现最佳性能：
1. **Opus**：处理最复杂的任务和决策
2. **Sonnet**：处理中等复杂度的开发工作
3. **Haiku**：处理快速、简单的任务和查询