import subprocess
import os
import sys

PROJECT_DIR = r"E:\Learn\programming\ponisha\MessageApp2"
APK_PATH = r"app\build\outputs\apk\debug\app-debug.apk"
PACKAGE_NAME = "com.kelasor.app"
MAIN_ACTIVITY = "MainActivity"  # ← اسم دقیق اکتیویتی اصلی

def run(cmd, cwd=None):
    result = subprocess.run(cmd, shell=True, cwd=cwd)
    if result.returncode != 0:
        print("❌ Command failed:", cmd)
        sys.exit(1)

def build():
    print("🔨 Building APK...")
    run("gradlew assembleDebug", cwd=PROJECT_DIR)

def get_devices():
    out = subprocess.check_output("adb devices", shell=True).decode(errors="ignore")
    return [l.split("\t")[0] for l in out.splitlines() if "\tdevice" in l]

def install(device):
    apk_full_path = os.path.join(PROJECT_DIR, APK_PATH)
    print(f"🚀 Installing on {device}...")
    run(f'adb -s {device} install -r "{apk_full_path}"')

def launch(device):
    print(f"▶ Launching on {device}...")
    run(f'adb -s {device} shell am start -n {PACKAGE_NAME}/.{MAIN_ACTIVITY}')

if __name__ == "__main__":
    build()
    devices = get_devices()

    if not devices:
        print("❌ No devices connected")
        sys.exit(1)

    for device in devices:
        install(device)
        launch(device)

    print("🎉 APK installed & launched on all devices successfully!")
