import sys

with open('app/src/main/java/com/Kelasor/app/ui/navigation/NavGraph.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace Routes.Settings.route
old_settings = '''        composable(Routes.Settings.route) {
            com.Kelasor.app.ui.screens.profile.SettingsScreen('''

new_settings = '''        composable(
            route = Routes.Settings.route,
            arguments = listOf(androidx.navigation.navArgument("initialTab") { type = androidx.navigation.NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            com.Kelasor.app.ui.screens.profile.SettingsScreen(
                initialTab = initialTab,'''

content = content.replace(old_settings, new_settings)

# Same for Profile (I'll just route Profile to Settings for now)
old_profile = '''        composable(Routes.Profile.route) {
            com.Kelasor.app.ui.screens.profile.SettingsScreen('''

new_profile = '''        composable(
            route = Routes.Profile.route,
            arguments = listOf(androidx.navigation.navArgument("initialTab") { type = androidx.navigation.NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            com.Kelasor.app.ui.screens.profile.SettingsScreen(
                initialTab = initialTab,'''

content = content.replace(old_profile, new_profile)

# Add MyCourses screen
# We need to add it somewhere, perhaps before closing of NavGraph
old_closing = '''}

// ═══════════════════════════════════════════════════════════════════════════════'''

new_closing = '''
        composable(Routes.MyCourses.route) {
            com.Kelasor.app.ui.screens.mosbat_elm.MyCoursesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
}

// ═══════════════════════════════════════════════════════════════════════════════'''

content = content.replace(old_closing, new_closing)

with open('app/src/main/java/com/Kelasor/app/ui/navigation/NavGraph.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated NavGraph.kt successfully")
