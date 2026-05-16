import sys

path = r'e:\Learn\programming\ponisha\MessageApp2\SpringBoot\src\main\kotlin\com\iliyadev\springboot\models\Entities.kt'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Current state from view_file:
# 1495: )
# 1496: 
# 1497:     var title: String = "",
# 1498:     var durationText: String = "",
# 1499:     var sessionStartTime: Instant? = null
# 1500: )
# 1501: 
# 1502: @Entity

# We want to remove lines 1496 to 1501 (1-indexed)
# In 0-indexed list, that's indices 1495 to 1500

del lines[1495:1501]

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print("Fixed.")
