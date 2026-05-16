import sys

path = r'e:\Learn\programming\ponisha\MessageApp2\SpringBoot\src\main\kotlin\com\iliyadev\springboot\models\Dtos.kt'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
inserted = False
for line in lines:
    if 'data class CourseDto(' in line and not inserted:
        new_lines.append('data class ManualInstructorDto(\n')
        new_lines.append('    val name: String,\n')
        new_lines.append('    val avatarUrl: String? = null,\n')
        new_lines.append('    val resume: String? = null\n')
        new_lines.append(')\n\n')
        inserted = True
    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Done.")
