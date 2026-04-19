#!/bin/bash
# run.sh - 使用本地 Gradle 或 gradlew 运行
# 用法: ./run.sh [Spider名称] [extend参数]

SPIDER="${1:-}"
EXTEND="${2:-}"

echo "========================================="
echo "  CatVod Spider 测试运行器"
echo "========================================="

# 优先使用 gradlew
if [ -x "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
else
    # 回退到系统 gradle
    if command -v gradle &> /dev/null; then
        GRADLE_CMD="gradle"
    else
        echo "❌ 未找到 Gradle。请先运行: ./setup.sh"
        exit 1
    fi
fi

if [ -n "$SPIDER" ]; then
    if [ -n "$EXTEND" ]; then
        echo "Spider: $SPIDER | Extend: $EXTEND"
        $GRADLE_CMD run --args="$SPIDER $EXTEND" --no-daemon --quiet
    else
        echo "Spider: $SPIDER"
        $GRADLE_CMD run --args="$SPIDER" --no-daemon --quiet
    fi
else
    echo "交互式模式"
    $GRADLE_CMD run --no-daemon --quiet
fi
