package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import HomeScreen
import MessagesViewModel
import ProfileScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anlarsinsoftware.memoriesbook.ui.theme.View.* // View altındaki her şeyi import et
import com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen.CreatePostScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen.ThumbnailSelectorScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance.*
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.MediaViewerScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen.*
import com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen.*
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    // 1. Tema yönetimi gibi uygulama genelindeki state'ler en üstte kalır.
    val context = LocalContext.current
    val themeDataStore = remember { ThemeDataStore(context) }
    val isDarkMode by themeDataStore.getTheme.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    MemoriesBookTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "auth_flow") {

            // --- GİRİŞ ÖNCESİ EKRANLAR (Authentication Flow) ---
            navigation(startDestination = "welcome_screen", route = "auth_flow") {
                composable("welcome_screen") {
                    WellComeScreen(navController = navController)
                }
                composable("register_screen") {
                    // Kayıt başarılı olduğunda ana akışa yönlendir
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate("main_flow") {
                                popUpTo("auth_flow") { inclusive = true }
                            }
                        }
                    )
                }
                composable("login_screen") {
                    // Giriş başarılı olduğunda ana akışa yönlendir
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("main_flow") {
                                popUpTo("auth_flow") { inclusive = true }
                            }
                        }
                    )
                }
            }


            navigation(startDestination = "home_screen", route = "main_flow") {

                composable("home_screen") {
                    // Bu ekranda kullanılacak ViewModel'leri, bu grafiğin yaşam döngüsüne bağlıyoruz.
                    val homeViewModel: HomeViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))
                    val commentsViewModel: CommentsViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))
                    val connectionsViewModel: ConnectionsViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))

                    HomeScreen(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        commentsViewModel = commentsViewModel,
                        connectionsViewModel = connectionsViewModel,

                    )
                }

                composable(
                    "thumbnail_selector/{videoUri}",
                    arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedUri = backStackEntry.arguments?.getString("videoUri") ?: ""
                    ThumbnailSelectorScreen(navController, videoUriString = encodedUri)
                }

                composable("media_viewer/{postId}/{initialIndex}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: ""
                    val initialIndex = backStackEntry.arguments?.getString("initialIndex")?.toIntOrNull() ?: 0

                    val mainGraphEntry = remember(backStackEntry) { navController.getBackStackEntry("main_flow") }
                    val homeViewModel: HomeViewModel = viewModel(mainGraphEntry)

                    MediaViewerScreen(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        postId = postId,
                        initialIndex = initialIndex
                    )
                }

                composable("createPost_screen") {
                    val connectionsViewModel: ConnectionsViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))
                    val createPostViewModel: CreatePostViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))

                    CreatePostScreen(navController, createPostViewModel = createPostViewModel, connectionsViewModel =connectionsViewModel ) // Kendi ViewModel'i var
                }
                composable("profile_screen") {
                    val connectionsViewModel: ConnectionsViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))
                    val profileViewModel: ProfileViewModel = viewModel(it.findViewModelStoreOwner(navController, "main_flow"))
                    ProfileScreen(navController, connectionsViewModel, profileViewModel)
                }
                composable("settings_screen") {
                    val toggleTheme: () -> Unit = {
                        scope.launch { themeDataStore.setTheme(!isDarkMode) }
                    }
                    SettingsScreen(
                        navController = navController,
                        isDarkMode = isDarkMode,
                        onThemeToggle = toggleTheme
                    )
                }
                composable("messages_screen") {
                    MessagesScreen(navController)
                }
                composable("connections_screen") {
                    ConnectionsScreen(navController)
                }
                composable("chat_screen/{friendId}") { backStackEntry ->
                    val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
                    val homeViewModel: HomeViewModel = viewModel(backStackEntry.findViewModelStoreOwner(navController, "main_flow"))

                    val chatDetailViewModel: ChatDetailViewModel = viewModel(
                        factory = ChatDetailViewModelFactory(friendId = friendId, homeViewModel = homeViewModel)
                    )
                    ChatDetailScreen(navController, chatDetailViewModel)
                }
            }
        }
    }
}

// ViewModel'i ait olduğu NavGraph'tan bulmak için yardımcı fonksiyon
@Composable
fun NavBackStackEntry.findViewModelStoreOwner(navController: NavController, route: String): NavBackStackEntry {
    return remember(this) { navController.getBackStackEntry(route) }
}