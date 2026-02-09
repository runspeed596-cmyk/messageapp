import os

root_dir = r"e:\Learn\programming\ponisha\MessageApp2\app\src"
old_pkg = "com.hasani.messageapp"
new_pkg = "com.Kelasor.app"

for parent, dirs, files in os.walk(root_dir):
    for file in files:
        if file.endswith((".kt", ".xml", ".java", ".gradle", ".kts")):
            path = os.path.join(parent, file)
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                if old_pkg in content:
                    new_content = content.replace(old_pkg, new_pkg)
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"Updated {path}")
            except Exception as e:
                print(f"Error processing {path}: {e}")
