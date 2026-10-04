# `.agents/skills/` —— 请勿放技能

本目录**不放**项目技能，只留这份说明。

原因：DSH（DeepSeek Harness）解析项目技能的顺序是

```
<项目根>/.dsh/skills/     ← 本仓实际生效的一份
<项目根>/.agents/skills/  ← 排在后面，同名会被上面遮蔽
```

在本目录放一份同名 `SKILL.md`，DSH 不会用它，改了也不生效、也不会报错——只会让两边内容悄悄分裂。

统一源是 [`shared/skills/`](../../shared/skills/)；同步目标只有三处：`.codebuddy/skills/`、`.cursor/skills/`、`.dsh/skills/`（见 [`shared/skills/README.md`](../../shared/skills/README.md)）。

散落在本目录的技能副本已被 `.gitignore` 挡住，`node scripts/check-shell-sync.mjs` 也会报出来。
