import sys

with open('app/src/main/java/com/Kelasor/app/ui/screens/profile/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    original = f.read()

insert_point1 = 'val currentLayoutDirection = LocalLayoutDirection.current'
replacement1 = '''val currentLayoutDirection = LocalLayoutDirection.current
    var selectedTab by remember { mutableIntStateOf(0) }'''
original = original.replace(insert_point1, replacement1)

start_marker = '                // ── 1. Dual Profile Header ──────────────────────────────'
end_marker = '            }\n        }\n    }\n}'

new_lazy_column_content = '''                // ── 0. Top Tabs ─────────────────────────────────────────
                item {
                    androidx.compose.material3.TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = extendedColors.accent,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = extendedColors.accent
                                )
                            }
                        }
                    ) {
                        androidx.compose.material3.Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { 
                                Text(
                                    "پیام‌رسان", 
                                    fontFamily = VazirFontFamily, 
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                        androidx.compose.material3.Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { 
                                Text(
                                    "مثبت علم", 
                                    fontFamily = VazirFontFamily, 
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                if (selectedTab == 0) {
                    // ── 1. Messenger Profile ──────────────────────────────
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileCircleWithEdit(
                                imageUrl = user?.avatarUrl,
                                name = user?.displayName ?: "",
                                label = "پروفایل من",
                                onClick = onEditProfileClick,
                                accentColor = extendedColors.accent
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = user?.displayName ?: "نام کاربری",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = VazirFontFamily
                            )
                            
                            Text(
                                text = if (user != null) "${user.phoneNumber} • @${user.username}" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = VazirFontFamily
                            )
                        }
                    }

                    // ── 2. Wallet Card ──────────────────────────────────────
                    item {
                        WalletCard(
                            onClick = onWalletClick,
                            accentColor = extendedColors.accent
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // ── 3. Settings Sections ────────────────────────────────
                    item {
                        SettingsSection(
                            items = listOf(
                                SettingsItemData(
                                    icon = Icons.Default.Person,
                                    title = "حساب کاربری",
                                    subtitle = "شماره، نام کاربری، بیوگرافی",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = onAccountClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Chat,
                                    title = "تنظیمات گفتگو",
                                    subtitle = "تصویر زمینه، حالت شب، انیمیشن‌ها",
                                    iconColor = Color(0xFFFFA000),
                                    onClick = onAppearanceClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Lock,
                                    title = "حریم خصوصی و امنیت",
                                    subtitle = "آخرین بازدید، دستگاه‌ها، گذرواژه‌ها",
                                    iconColor = Color(0xFF4CAF50),
                                    onClick = onPrivacyClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Notifications,
                                    title = "اعلان‌ها",
                                    subtitle = "صداها، تماس‌ها، نشان‌ها",
                                    iconColor = Color(0xFFF44336),
                                    onClick = onNotificationsClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.PieChart,
                                    title = "داده‌ها و ذخیره‌سازی",
                                    subtitle = "تنظیمات دانلود خودکار رسانه‌ها",
                                    iconColor = Color(0xFF00BCD4),
                                    onClick = onDataStorageClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Folder,
                                    title = "پوشه‌های گفتگو",
                                    subtitle = "مرتب‌سازی گفتگوها در پوشه‌ها",
                                    iconColor = Color(0xFF2196F3),
                                    onClick = onFoldersClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.Devices,
                                    title = "دستگاه‌ها",
                                    subtitle = "مدیریت دستگاه‌های متصل",
                                    iconColor = Color(0xFF009688),
                                    onClick = onDevicesClick
                                ),
                                SettingsItemData(
                                    icon = Icons.Default.BatteryChargingFull,
                                    title = "صرفه‌جویی در باتری",
                                    subtitle = "کاهش مصرف انرژی",
                                    iconColor = Color(0xFFFF9800),
                                    onClick = { /* Power Saving */ }
                                )
                            )
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // ── 4. Logout ───────────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                authViewModel.logout()
                                onLogoutClick()
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "خروج از حساب",
                                color = Color.Red,
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // ── 1. Mosbat Elm Profile ──────────────────────────────
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (user?.institutionId != null) {
                                ProfileCircleWithEdit(
                                    imageUrl = user.institutionLogoUrl,
                                    name = user.institutionName ?: "آکادمی",
                                    label = "پروفایل موسسه",
                                    onClick = onEditAcademyProfileClick,
                                    onAvatarClick = { onAcademyProfileClick(user.institutionId) },
                                    accentColor = Color(0xFF2196F3)
                                )
                            } else if (user?.isTeacher == true) {
                                ProfileCircleWithEdit(
                                    imageUrl = user.avatarUrl,
                                    name = user.displayName,
                                    label = "پروفایل مدرس",
                                    onClick = onEditAcademyProfileClick,
                                    onAvatarClick = { /* Maybe go to teacher personal profile? */ },
                                    accentColor = Color(0xFF2196F3)
                                )
                            } else {
                                AddAcademyProfileCircle(
                                    onClick = onEditAcademyProfileClick,
                                    label = "ثبت‌نام به عنوان برگزارکننده",
                                    accentColor = Color(0xFF4CAF50)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // ── 2. Mosbat Elm Menu ──────────────────────────────────────
                    item {
                        SettingsSection(
                            items = listOf(
                                SettingsItemData(
                                    icon = Icons.Default.School,
                                    title = "دوره‌های من (مدیریت)",
                                    subtitle = "مدیریت و ایجاد دوره‌های آموزشی",
                                    iconColor = extendedColors.accent,
                                    onClick = { /* TODO: Navigate */ }
                                ),
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
                        )
                    }
                    
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

                // ── Common. App Version ──────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "کلاسور برای اندروید نسخه ۱.۰.۰",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
'''

start_idx = original.find(start_marker)
end_idx = original.find(end_marker)

if start_idx != -1 and end_idx != -1:
    new_content = original[:start_idx] + new_lazy_column_content + '\n' + original[end_idx:]
    with open('app/src/main/java/com/Kelasor/app/ui/screens/profile/SettingsScreen.kt', 'w', encoding='utf-8') as f:
        f.write(new_content)
    print('Updated successfully')
else:
    print('Could not find markers')
