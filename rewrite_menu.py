import sys

with open('app/src/main/java/com/Kelasor/app/ui/screens/profile/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    original = f.read()

# Insert isOrganizer above LazyColumn
insert_point_lazy = '            LazyColumn('
replacement_lazy = '''            val isOrganizer = user?.institutionId != null || user?.isTeacher == true
            LazyColumn('''
original = original.replace(insert_point_lazy, replacement_lazy)

start_marker = '                    // ── 2. Mosbat Elm Menu ──────────────────────────────────────'
end_marker = '                // ── Common. App Version ──────────────────────────────────────'

new_menu_content = '''                    // ── 2. Mosbat Elm Menu ──────────────────────────────────────
                    item {
                        val menuItems = if (isOrganizer) {
                            listOf(
                                SettingsItemData(
                                    icon = Icons.Default.QueryStats,
                                    title = "آمار و درآمد",
                                    subtitle = "مشاهده عملکرد و گزارش مالی",
                                    iconColor = extendedColors.accent,
                                    onClick = { /* TODO */ }
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.School,
                                    title = "دوره‌های من",
                                    subtitle = "مدیریت و ایجاد دوره‌های آموزشی",
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = { /* TODO: Navigate */ }
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Handshake,
                                    title = "همکاری‌ها",
                                    subtitle = "مدیریت شرکا و مدرسین همکار",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = { /* TODO */ }
                                )
                            )
                        } else {
                            listOf(
                                SettingsItemData(
                                    icon = Icons.Default.School,
                                    title = "دوره‌های خریداری شده",
                                    subtitle = "مشاهده دوره‌های فعال شما",
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = { /* TODO */ }
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.EmojiEvents,
                                    title = "مدرک‌های دریافت شده",
                                    subtitle = "گواهینامه‌های دوره‌های گذرانده شده",
                                    iconColor = Color(0xFFFFC107),
                                    onClick = { /* TODO */ }
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Stars,
                                    title = "پست‌های لایک شده",
                                    subtitle = "محتواهایی که پسندیده‌اید",
                                    iconColor = Color(0xFFE91E63),
                                    onClick = { /* TODO */ }
                                )
                            )
                        }
                        
                        SettingsSection(items = menuItems)
                    }
                    
                    if (!isOrganizer) {
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                        
                        item {
                            // Become Organizer Button
                            androidx.compose.material3.Button(
                                onClick = { /* TODO: Navigate to Organizer Dashboard or set as Organizer */ },
                                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = extendedColors.accent,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تغییر اکانت به برگزارکننده (مدرس)",
                                    fontFamily = VazirFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

'''

start_idx = original.find(start_marker)
end_idx = original.find(end_marker)

if start_idx != -1 and end_idx != -1:
    new_content = original[:start_idx] + new_menu_content + '                // ── Common. App Version ──────────────────────────────────────\n' + original[end_idx + len(end_marker):]
    with open('app/src/main/java/com/Kelasor/app/ui/screens/profile/SettingsScreen.kt', 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('Updated successfully')
else:
    print('Could not find markers')
