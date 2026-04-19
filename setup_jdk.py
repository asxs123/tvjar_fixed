#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
setup_jdk.py - 为 tvjar_fixed 项目下载并配置便携式 JDK (Windows x64)
完全独立运行，不修改系统环境，JDK 仅在本项目内可用。

用法: python setup_jdk.py
兼容: Python 2.7+ / Python 3.x / Windows / Linux / macOS
"""
from __future__ import print_function
import os, sys, tarfile, zipfile, platform, shutil

# ========== 自动检测系统 ==========
SYSTEM = platform.system().lower()  # 'windows', 'linux', 'darwin'
ARCH = platform.machine().lower()   # 'amd64', 'x86_64', 'aarch64'

# ========== 配置 ==========
JDK_VERSION = "21"
JDK_UPDATE = "10"
JDK_BUILD = "7"
PROJECT_DIR = os.path.dirname(os.path.abspath(__file__))
JDK_DIR = os.path.join(PROJECT_DIR, "jdk")

# 根据系统选择下载包
if SYSTEM == "windows":
    JDK_FILE = "OpenJDK21U-jdk_x64_windows_hotspot_21.0.{upd}_{bld}.zip".format(upd=JDK_UPDATE, bld=JDK_BUILD)
    ARCHIVE_TYPE = "zip"
elif SYSTEM == "darwin":
    JDK_FILE = "OpenJDK21U-jdk_x64_mac_hotspot_21.0.{upd}_{bld}.tar.gz".format(upd=JDK_UPDATE, bld=JDK_BUILD)
    ARCHIVE_TYPE = "tar.gz"
else:
    JDK_FILE = "OpenJDK21U-jdk_x64_linux_hotspot_21.0.{upd}_{bld}.tar.gz".format(upd=JDK_UPDATE, bld=JDK_BUILD)
    ARCHIVE_TYPE = "tar.gz"

GITHUB_URL = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.{upd}%2B{bld}/{file}".format(
    upd=JDK_UPDATE, bld=JDK_BUILD, file=JDK_FILE
)

BUFFER_SIZE = 8192

def print_banner():
    print("=" * 55)
    print("  tvjar_fixed - 便携式 JDK 安装工具 (Windows)")
    print("=" * 55)
    print("  系统: " + SYSTEM + " " + ARCH)
    print("  JDK: Adoptium Temurin 21.0." + JDK_UPDATE)
    print()

def check_jdk():
    """检查 JDK 是否已安装"""
    if SYSTEM == "windows":
        java_bin = os.path.join(JDK_DIR, "current", "bin", "java.exe")
    else:
        java_bin = os.path.join(JDK_DIR, "current", "bin", "java")

    if os.path.isfile(java_bin):
        print("[OK] JDK 已安装: " + JDK_DIR)
        try:
            import subprocess
            out = subprocess.check_output([java_bin, "-version"], stderr=subprocess.STDOUT)
            ver = out.decode("utf-8", errors="replace").strip().split("\n")[0]
            print("     " + ver)
        except Exception as e:
            # Python 2 兼容
            try:
                import subprocess
                p = subprocess.Popen([java_bin, "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                out, err = p.communicate()
                ver = (out or err).decode("utf-8", errors="replace").strip().split("\n")[0]
                print("     " + ver)
            except:
                print("     (版本检测跳过)")
        return True
    return False

def download_file(url, dest):
    """使用 Python 内置库下载文件，兼容 Python 2/3"""
    print("[下载] " + url)
    print("[保存] " + dest)

    # 方法1: Python 3 urllib
    if sys.version_info[0] >= 3:
        try:
            import urllib.request, ssl
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE

            def reporthook(blocknum, blocksize, totalsize):
                if totalsize > 0:
                    pct = min(100, int(blocknum * blocksize * 100 / totalsize))
                    mb = blocknum * blocksize / (1024 * 1024)
                    total_mb = totalsize / (1024 * 1024)
                    sys.stdout.write("\r  进度: %d%% (%.0f/%.0f MB) " % (pct, mb, total_mb))
                    sys.stdout.flush()

            urllib.request.urlretrieve(url, dest, reporthook)
            print("\n[OK] 下载完成")
            return True
        except Exception as e:
            print("\n  尝试方法1失败: " + str(e))

    # 方法2: http.client (Python 3, 不依赖 urllib)
    if sys.version_info[0] >= 3:
        try:
            from urllib.parse import urlparse
            import http.client, ssl
            parsed = urlparse(url)
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE
            conn = http.client.HTTPSConnection(parsed.hostname, timeout=300, context=ctx)
            conn.request("GET", parsed.path + ("?" + parsed.query if parsed.query else ""),
                        headers={"User-Agent": "Mozilla/5.0"})
            resp = conn.getresponse()
            if resp.status in (301, 302, 303, 307, 308):
                # 跟随重定向
                redirect = resp.getheader("Location")
                conn.close()
                return download_file(redirect, dest)
            total = int(resp.getheader('Content-Length', 0))
            downloaded = 0
            with open(dest, 'wb') as f:
                while True:
                    chunk = resp.read(BUFFER_SIZE)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)
                    if total > 0:
                        pct = min(100, int(downloaded * 100 / total))
                        sys.stdout.write("\r  进度: %d%% (%.0f/%.0f MB) " % (
                            pct, downloaded/(1024*1024), total/(1024*1024)))
                        sys.stdout.flush()
            print("\n[OK] 下载完成")
            return True
        except Exception as e:
            print("\n  尝试方法2失败: " + str(e))

    # 方法3: Python 2 urllib2
    if sys.version_info[0] < 3:
        try:
            import urllib2, ssl
            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE
            req = urllib2.Request(url)
            req.add_header('User-Agent', 'Mozilla/5.0')
            resp = urllib2.urlopen(req, context=ctx, timeout=300)
            total = int(resp.headers.get('Content-Length', 0))
            downloaded = 0
            with open(dest, 'wb') as f:
                while True:
                    chunk = resp.read(BUFFER_SIZE)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)
                    if total > 0:
                        pct = min(100, int(downloaded * 100 / total))
                        sys.stdout.write("\r  进度: %d%% " % pct)
                        sys.stdout.flush()
            print("\n[OK] 下载完成")
            return True
        except Exception as e:
            print("\n  尝试方法3失败: " + str(e))

    return False

def extract_zip(archive, dest_dir):
    """解压 zip 文件 (Windows)"""
    print("[解压] 正在解压到: " + dest_dir)
    try:
        with zipfile.ZipFile(archive, 'r') as zf:
            members = zf.namelist()
            total = len(members)
            for i, member in enumerate(members):
                zf.extract(member, dest_dir)
                if i % 50 == 0:
                    pct = min(100, int(i * 100 / total))
                    sys.stdout.write("\r  解压: %d%% " % pct)
                    sys.stdout.flush()
        print("\n[OK] 解压完成")
        return True
    except Exception as e:
        print("\n[FAIL] 解压失败: " + str(e))
        return False

def extract_tar_gz(archive, dest_dir):
    """解压 tar.gz 文件 (Linux/macOS)"""
    print("[解压] 正在解压到: " + dest_dir)
    try:
        with tarfile.open(archive, 'r:gz') as tar:
            members = tar.getmembers()
            total = len(members)
            for i, member in enumerate(members):
                tar.extract(member, dest_dir)
                if i % 100 == 0:
                    pct = min(100, int(i * 100 / total))
                    sys.stdout.write("\r  解压: %d%% " % pct)
                    sys.stdout.flush()
        print("\n[OK] 解压完成")
        return True
    except Exception as e:
        print("\n[FAIL] 解压失败: " + str(e))
        return False

def find_jdk_root(search_dir):
    """在解压目录中找到 JDK 根目录"""
    for name in os.listdir(search_dir):
        full = os.path.join(search_dir, name)
        if not os.path.isdir(full):
            continue
        # 检查是否有 bin/java
        if SYSTEM == "windows":
            java_path = os.path.join(full, "bin", "java.exe")
        else:
            java_path = os.path.join(full, "bin", "java")
        if os.path.isfile(java_path):
            return full
        # 递归一层
        for sub in os.listdir(full):
            subfull = os.path.join(full, sub)
            if os.path.isdir(subfull):
                if SYSTEM == "windows":
                    java_path = os.path.join(subfull, "bin", "java.exe")
                else:
                    java_path = os.path.join(subfull, "bin", "java")
                if os.path.isfile(java_path):
                    return subfull
    return None

def create_link(src, link_path):
    """创建符号链接或 junction (Windows 兼容)"""
    # 先删除旧的
    if os.path.islink(link_path):
        os.unlink(link_path)
    elif os.path.isdir(link_path):
        shutil.rmtree(link_path)
    elif os.path.isfile(link_path):
        os.remove(link_path)

    if SYSTEM == "windows":
        # Windows: 使用 junction 或复制目录
        try:
            import subprocess
            # 尝试 mklink /J (需要管理员权限但通常可用)
            subprocess.check_call('mklink /J "{}" "{}"'.format(link_path, src),
                                shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            return True
        except:
            # 回退: 直接使用绝对路径，不创建链接
            return False
    else:
        os.symlink(src, link_path)
        return True

def setup_jdk():
    """主流程"""
    print_banner()

    # 检查是否已安装
    if check_jdk():
        print("\n无需重复安装。运行测试:")
        print("  gradlew.bat run --args=\"Douban\"")
        return 0

    # 下载
    os.makedirs(JDK_DIR, exist_ok=True)
    archive = os.path.join(JDK_DIR, JDK_FILE)

    print("下载大小: ~195 MB (Windows zip)")
    print("下载地址: GitHub Releases (Adoptium)")
    print()

    if not download_file(GITHUB_URL, archive):
        print("\n" + "=" * 55)
        print("  自动下载失败！请手动下载 JDK：")
        print("=" * 55)
        print()
        print("  方法 A - 浏览器下载（推荐）:")
        print("  1. 打开: https://adoptium.net/releases.html")
        print("  2. 选择: Temurin 21 LTS / Windows x64 / JDK / .zip")
        print("  3. 将下载的 .zip 文件放到:")
        print("     " + archive)
        print("  4. 重新运行: python setup_jdk.py")
        print()
        print("  方法 B - 直接链接:")
        print("  " + GITHUB_URL)
        print()
        print("  方法 C - 国内镜像 (可能更快):")
        print("  https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/windows/")
        print()
        return 1

    # 解压
    print()
    if ARCHIVE_TYPE == "zip":
        if not extract_zip(archive, JDK_DIR):
            return 1
    else:
        if not extract_tar_gz(archive, JDK_DIR):
            return 1

    # 查找 JDK 根目录
    jdk_root = find_jdk_root(JDK_DIR)
    if not jdk_root:
        print("[FAIL] 未找到 JDK 目录")
        return 1

    print("[找到] " + jdk_root)

    # 创建 current 链接
    link = os.path.join(JDK_DIR, "current")
    if create_link(jdk_root, link):
        print("[链接] current -> " + os.path.basename(jdk_root))
    else:
        # Windows 没有权限创建链接，直接用目录名
        print("[提示] 使用实际目录: " + os.path.basename(jdk_root))

    # 清理压缩包
    try:
        os.remove(archive)
        print("[清理] 已删除安装包")
    except:
        pass

    # 验证
    if SYSTEM == "windows":
        java_final = os.path.join(jdk_root, "bin", "java.exe")
    else:
        java_final = os.path.join(jdk_root, "bin", "java")

    if os.path.isfile(java_final):
        print()
        print("=" * 55)
        print("  ✅ JDK 安装成功！")
        print("=" * 55)
        try:
            import subprocess
            out = subprocess.check_output([java_final, "-version"], stderr=subprocess.STDOUT)
            ver = out.decode("utf-8", errors="replace").strip().split("\n")[0]
            print("  " + ver)
        except:
            try:
                p = subprocess.Popen([java_final, "-version"], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                out, err = p.communicate()
                ver = (out or err).decode("utf-8", errors="replace").strip().split("\n")[0]
                print("  " + ver)
            except:
                pass
        print()
        print("  JDK 位置: " + jdk_root)
        print("  仅本项目可用，不会影响系统环境")
        print()
        print("  运行测试:")
        print("    gradlew.bat run --args=\"Douban\"")
        print("    gradlew.bat run --args=\"AppYsV2 http://xxx.com/api.php/app/\"")
        print()
        return 0
    else:
        print("[FAIL] JDK 安装验证失败")
        return 1

if __name__ == "__main__":
    sys.exit(setup_jdk())
