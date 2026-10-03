#!/usr/bin/env bash
# 图片整理 Android 例子：编 debug APK → 挂到 tag v1.0.0 的 GitHub Release。
# 用法（本文件所在目录，或任意处写绝对路径）：
#   ./examples/android/scripts/publish-v1.0.0-github-release.sh
# 覆盖仓库：GITHUB_REPO=owner/name ./...
set -euo pipefail

AND_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$AND_ROOT/../.." && pwd)"
REPO="${GITHUB_REPO:-mengkj2021/ai-dev-notes}"
TAG="v1.0.0"
APK_NAME="picture-organizer-1.0.0-debug.apk"
OUT_DIR="$REPO_ROOT/.tmp"
BUILT="$AND_ROOT/app/build/outputs/apk/debug/app-debug.apk"
STAGED="$OUT_DIR/$APK_NAME"

if ! command -v gh >/dev/null 2>&1; then
  echo "缺少 gh。安装后执行: brew install gh && gh auth login" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "gh 未登录。先执行: gh auth login" >&2
  exit 1
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  for cand in \
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
    "/Applications/Android Studio 2.app/Contents/jbr/Contents/Home"
  do
    if [[ -x "$cand/bin/java" ]]; then
      export JAVA_HOME="$cand"
      break
    fi
  done
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}$PATH"

echo "==> assembleDebug (JAVA_HOME=${JAVA_HOME:-system})"
(
  cd "$AND_ROOT"
  if [[ -f ./gradlew ]]; then
    ./gradlew assembleDebug
  else
    ./gradlew.bat assembleDebug
  fi
)

mkdir -p "$OUT_DIR"
cp -f "$BUILT" "$STAGED"
echo "==> APK: $STAGED"

if gh release view "$TAG" --repo "$REPO" >/dev/null 2>&1; then
  echo "==> Release $TAG 已存在，上传/覆盖 $APK_NAME"
  gh release upload "$TAG" "$STAGED" --repo "$REPO" --clobber
else
  gh release create "$TAG" "$STAGED" \
    --repo "$REPO" \
    --title "v1.0.0 · 图片整理（例子）" \
    --notes "$(cat <<'EOF'
图片整理 Android 例子的 debug APK。笔记仓见根 README。
Open `examples/android/` 自行 assembleDebug。
EOF
)"
fi

gh release view "$TAG" --repo "$REPO"
