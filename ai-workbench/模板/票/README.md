# 起票模板

票库规则：产品车道先选 [examples/project-status/README.md](../../../examples/project-status/README.md)；笔记车道 [体系](../../体系/README.md)。节点：[票代号与节点.md](票代号与节点.md)。

## 步骤

1. **定车道**：体系 / android / windows  
2. **定票号**：该车道阶段 README「下一号」（Windows 见该车道 README）  
3. **定节点**：B / V / P  
4. **复制模板** → 产品 `examples/project-status/<端>/票库/<分区>/票号-简述.md`，或笔记 `ai-workbench/体系/票库/<分区>/票号-简述.md`  
5. **登记**：分区 README 加一行；该车道当前 `待对应.md` 加 ☐  
6. 接票：[`prompts/票_接票开发.md`](../../../prompts/票_接票开发.md)

产品例子默认 android（阶段4）。笔记层用体系。Windows 见 `examples/project-status/windows/`（阶段①）。

## 模板

| 类型 | 模板 |
|---|---|
| Bug | [bug票.md](bug票.md) |
| 式样变更 | [式样变更票.md](式样变更票.md) |
| 调查 | [调查票.md](调查票.md) |
| 功能 / 体验 | [功能体验票.md](功能体验票.md) |
| 节点约定 | [票代号与节点.md](票代号与节点.md) |
