#!/usr/bin/env node
/**
 * 校验「同一份壳」在 shared 源与三个工具目录间逐字一致。
 *
 * 统一源：shared/rules/<name>.md · shared/skills/<name>/SKILL.md
 * 工具目录：.codebuddy/ · .cursor/ · .dsh/
 *
 * 口径：
 *   默认（无参数）        校验工作区文件——人工核对与 pre-push 主门禁都用这个
 *   --worktree            同上（显式写法）
 *   --root <目录>         把该目录当仓库根来校验（pre-push 用它比对 REF^ 的已提交内容）
 *
 * 只读文件、不 spawn 任何子进程，因此 Windows / macOS / Linux 与受限沙箱下结果一致。
 * 退出码：0 = 一致；1 = 缺失 / 内容漂移 / 同名技能重复。
 */

import { createHash } from 'node:crypto'
import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const argv = process.argv.slice(2)
const rootFlag = argv.indexOf('--root')
const repoRoot =
  rootFlag >= 0 && argv[rootFlag + 1] ? resolve(argv[rootFlag + 1]) : resolve(dirname(fileURLToPath(import.meta.url)), '..')
/** 快照模式下不做全仓技能扫描（快照里只有壳文件） */
const scanRepo = repoRoot === resolve(dirname(fileURLToPath(import.meta.url)), '..')

const rules = ['dev-convention', 'git-convention', 'sync-convention']
const skills = ['create-screen-doc', 'implement-screen', 'sync-docs']

/** @type {{ label: string, paths: string[] }[]} */
const units = []
for (const name of rules) {
  units.push({
    label: `rules ${name}`,
    paths: [
      `shared/rules/${name}.md`,
      `.codebuddy/rules/${name}/RULE.mdc`,
      `.cursor/rules/${name}.mdc`,
      `.dsh/rules/${name}/RULE.mdc`,
    ],
  })
}
for (const name of skills) {
  units.push({
    label: `skills ${name}`,
    paths: [
      `shared/skills/${name}/SKILL.md`,
      `.codebuddy/skills/${name}/SKILL.md`,
      `.cursor/skills/${name}/SKILL.md`,
      `.dsh/skills/${name}/SKILL.md`,
    ],
  })
}

/** @param {string} text @returns {string} */
function sha256(text) {
  return createHash('sha256').update(Buffer.from(text, 'utf8')).digest('hex')
}

/**
 * 行尾归一化后再比对。
 * 本仓 .gitattributes/autocrlf 下工作区是 CRLF、git blob 是 LF；
 * 壳同步要防的是「内容漂移」，不是行尾风格差异。
 * @param {string} text @returns {string}
 */
function normalize(text) {
  return text.replace(/\r\n/g, '\n')
}

/** @type {Map<string, string | null>} */
const contentCache = new Map()

/**
 * @param {string} rel 仓库根相对路径
 * @returns {string | null} 取不到返回 null
 */
function readShell(rel) {
  if (contentCache.has(rel)) return contentCache.get(rel)
  const abs = join(repoRoot, rel)
  const content = existsSync(abs) ? normalize(readFileSync(abs, 'utf8')) : null
  contentCache.set(rel, content)
  return content
}

const problems = []
const notes = []

for (const unit of units) {
  /** @type {Map<string, string[]>} */
  const groups = new Map()
  const missing = []
  for (const rel of unit.paths) {
    const content = readShell(rel)
    if (content === null) {
      missing.push(rel)
      continue
    }
    const hash = sha256(content)
    if (!groups.has(hash)) groups.set(hash, [])
    groups.get(hash).push(rel)
  }
  if (missing.length > 0) problems.push(`${unit.label}：缺失 ${missing.join('、')}`)
  if (groups.size > 1) {
    problems.push(
      `${unit.label}：内容漂移\n` +
        [...groups.values()].map((group) => `      · ${group.join('  ==  ')}`).join('\n'),
    )
  }
}

if (scanRepo) {
  const skipDirs = new Set(['.git', 'node_modules', 'build', '.tmp'])
  const allowedDotDirs = new Set(['.codebuddy', '.cursor', '.dsh', '.agents'])
  const mirrors = new Set([
    join(repoRoot, 'shared', 'skills'),
    join(repoRoot, '.codebuddy', 'skills'),
    join(repoRoot, '.cursor', 'skills'),
    join(repoRoot, '.dsh', 'skills'),
  ])

  /** @type {string[]} */
  const found = []
  /** @param {string} dir @param {number} depth */
  const walk = (dir, depth) => {
    if (depth > 6) return
    let entries
    try {
      entries = readdirSync(dir, { withFileTypes: true })
    } catch {
      return
    }
    for (const entry of entries) {
      if (!entry.isDirectory()) continue
      if (skipDirs.has(entry.name)) continue
      if (entry.name.startsWith('.') && !allowedDotDirs.has(entry.name)) continue
      const child = join(dir, entry.name)
      if (existsSync(join(child, 'SKILL.md'))) {
        found.push(child)
        continue
      }
      walk(child, depth + 1)
    }
  }
  walk(repoRoot, 0)

  for (const dir of found) {
    if (mirrors.has(dirname(dir))) continue
    const rel = dir.slice(repoRoot.length + 1).replaceAll('\\', '/')
    const name = dir.split(/[\\/]/).pop()
    if (skills.includes(name)) {
      problems.push(`同名技能散落在 ${rel}：DSH 会用 .dsh/skills 遮蔽它，两处内容会悄悄分裂`)
    } else {
      notes.push(`发现未登记技能目录 ${rel}（不在壳同步清单内，请确认是否有意为之）`)
    }
  }
}

for (const note of notes) console.warn(`[check-shell-sync] 提示：${note}`)

if (problems.length > 0) {
  const scope = rootFlag >= 0 ? `快照 ${repoRoot}` : '工作区'
  console.error(`[check-shell-sync] 未通过（口径：${scope}）：\n`)
  for (const problem of problems) console.error(`  - ${problem}`)
  console.error('\n  修法：先改 shared/（细则正文在 ai-workbench/），再整份复制到 .codebuddy/ · .cursor/ · .dsh/')
  process.exit(1)
}

const checked = units.reduce((sum, unit) => sum + unit.paths.length, 0)
console.log(`[check-shell-sync] OK：${units.length} 组壳 / ${checked} 份文件逐字一致（${repoRoot}）`)
