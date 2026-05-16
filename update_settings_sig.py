import sys

with open('app/src/main/java/com/Kelasor/app/ui/screens/profile/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add onMyCoursesClick: () -> Unit = {} to SettingsScreen parameters
old_sig = '''    onDevicesClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},\n'''

new_sig = '''    onDevicesClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onMyCoursesClick: () -> Unit = {},\n'''

content = content.replace(old_sig, new_sig)

# Replace the onClick for 'دوره‌های من' in Mosbat Elm Menu
old_menu_item = '''                                SettingsItemData(
                                    icon = Icons.Default.School,
                                    title = "دوره‌های من",
                                    subtitle = "مدیریت و ایجاد دوره‌های آموزشی",
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = { /* TODO: Navigate */ }
                                )'''

new_menu_item = '''                                SettingsItemData(
                                    icon = Icons.Default.School,
                                    title = "دوره‌های من",
                                    subtitle = "مدیریت و ایجاد دوره‌های آموزشی",
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = onMyCoursesClick
                                )'''

content = content.replace(old_menu_item, new_menu_item)

with open('app/src/main/java/com/Kelasor/app/ui/screens/profile/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print('Updated SettingsScreen.kt')
