#!/bin/bash
# setup.sh - 解决 Gradle 下载问题的安装脚本
# 用法: chmod +x setup.sh && ./setup.sh

set -e

GRADLE_VERSION="8.5"
GRADLE_DIR="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
DIST_DIR=$(find "$GRADLE_DIR" -maxdepth 1 -type d 2>/dev/null | head -1)

echo "========================================="
echo "  Gradle $GRADLE_VERSION 离线安装脚本"
echo "========================================="

# 检查是否已安装
if [ -n "$DIST_DIR" ] && [ -f "$DIST_DIR/gradle-${GRADLE_VERSION}/bin/gradle" ]; then
    echo "✅ Gradle $GRADLE_VERSION 已安装，跳过下载"
    exit 0
fi

# 创建目录
mkdir -p "$GRADLE_DIR/download"
cd "$GRADLE_DIR/download"

ZIP_FILE="gradle-${GRADLE_VERSION}-bin.zip"
DOWNLOAD_URL="https://services.gradle.org/distributions/${ZIP_FILE}"

echo "📥 尝试下载 Gradle $GRADLE_VERSION ..."

# 方法1: curl (尝试不同 TLS 设置)
if command -v curl &> /dev/null; then
    echo "  使用 curl 下载..."
    if curl -L --tlsv1.2 --connect-timeout 30 -o "$ZIP_FILE" "$DOWNLOAD_URL" 2>/dev/null; then
        echo "  ✅ curl 下载成功"
    elif curl -L --tlsv1.1 --connect-timeout 30 -o "$ZIP_FILE" "$DOWNLOAD_URL" 2>/dev/null; then
        echo "  ✅ curl (TLS 1.1) 下载成功"
    elif curl -L -k --connect-timeout 30 -o "$ZIP_FILE" "$DOWNLOAD_URL" 2>/dev/null; then
        echo "  ✅ curl (忽略证书) 下载成功"
    else
        echo "  ❌ curl 下载失败"
    fi
fi

# 方法2: wget
if [ ! -f "$ZIP_FILE" ] && command -v wget &> /dev/null; then
    echo "  使用 wget 下载..."
    if wget --no-check-certificate -q -O "$ZIP_FILE" "$DOWNLOAD_URL" 2>/dev/null; then
        echo "  ✅ wget 下载成功"
    else
        echo "  ❌ wget 下载失败"
        rm -f "$ZIP_FILE"
    fi
fi

# 方法3: 使用 Python (如果有的话)
if [ ! -f "$ZIP_FILE" ] && command -v python3 &> /dev/null; then
    echo "  使用 Python 下载..."
    python3 -c "
import urllib.request, ssl
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE
urllib.request.urlretrieve('$DOWNLOAD_URL', '$ZIP_FILE')
print('  ✅ Python 下载成功')
" 2>/dev/null || echo "  ❌ Python 下载失败"
fi

# 检查下载结果
if [ ! -f "$ZIP_FILE" ]; then
    echo ""
    echo "❌ 自动下载失败！请手动下载："
    echo ""
    echo "   1. 在浏览器中打开: $DOWNLOAD_URL"
    echo "   2. 将下载的文件保存到: $GRADLE_DIR/download/$ZIP_FILE"
    echo "   3. 然后重新运行此脚本"
    echo ""
    echo "   或者直接安装 Gradle:"
    echo "   - macOS:   brew install gradle"
    echo "   - Ubuntu:  sdk install gradle $GRADLE_VERSION"
    echo "   - 手动:    解压到任意目录，设置 GRADLE_HOME 环境变量"
    exit 1
fi

# 解压
echo "📦 解压 Gradle..."
unzip -q -o "$ZIP_FILE" -d "$GRADLE_DIR/"
chmod +x "$GRADLE_DIR/gradle-${GRADLE_VERSION}/bin/gradle"

echo "✅ Gradle $GRADLE_VERSION 安装成功！"
echo ""
echo "验证安装:"
"$GRADLE_DIR/gradle-${GRADLE_VERSION}/bin/gradle" --version 2>&1 | head -5
echo ""
echo "现在可以运行: ./gradlew run --args=\"Douban\""
