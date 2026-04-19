#!/bin/bash
# 快速启动脚本
# 用法: ./test.sh [Spider名称] [extend参数]

SPIDER="${1:-AppYsV2}"
EXTEND="${2:-}"

echo "========================================="
echo "  CatVod Spider 测试环境"
echo "========================================="
echo "Spider: $SPIDER"
if [ -n "$EXTEND" ]; then
  echo "Extend: $EXTEND"
fi
echo "========================================="
echo ""

if [ -n "$EXTEND" ]; then
  ./gradlew run --args="$SPIDER $EXTEND" --no-daemon --quiet
else
  ./gradlew run --args="$SPIDER" --no-daemon --quiet
fi
