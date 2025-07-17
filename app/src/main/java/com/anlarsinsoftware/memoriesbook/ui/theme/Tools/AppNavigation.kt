package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import HomeScreen
import ProfileScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen.MessagesScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.*
import com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance.LoginScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance.RegisterScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance.WellComeScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen.ChatDetailScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen.ConnectionsScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen.SettingsScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ChatDetailViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ChatDetailViewModelFactory
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.CommentsViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val themeDataStore = remember { ThemeDataStore(context) }
    val isDarkMode by themeDataStore.getTheme.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val toggleTheme: () -> Unit = {
        scope.launch {
            themeDataStore.setTheme(!isDarkMode)
        }
    }
    val homeViewModel: HomeViewModel = viewModel()
    val commentsViewModel: CommentsViewModel = viewModel()
    MemoriesBookTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "welcome_screen") {
            composable("welcome_screen") {
                WellComeScreen(navController = navController)
            }
            composable("register_screen") {
                RegisterScreen(navController)
            }
            composable("login_screen") {
                LoginScreen(navController)
            }
            composable("home_screen") {

                val posts by homeViewModel.posts.collectAsState()
                val comments by commentsViewModel.comments.collectAsState()

                HomeScreen(
                    navController = navController,
                    postList = posts,
                    commentList = comments,
                    onPostLikeClicked = homeViewModel::onPostLikeClicked,
                    onCommentLikeClicked = commentsViewModel::onCommentLikeClicked,
                    homeViewModel = homeViewModel,
                    commentsViewModel =  commentsViewModel
                )
            }
            composable("createPost_screen") {
                CreatePostScreen(navController )
            }
            composable("profile_screen") {
                ProfileScreen(navController)
            }
            composable("settings_screen") {
                SettingsScreen(
                    navController = navController,
                    isDarkMode = isDarkMode,
                    onThemeToggle = toggleTheme
                )
            }
            composable("messages_screen"){
                MessagesScreen(navController)
            }
            composable("connections_screen"){
                ConnectionsScreen(navController)
            }
            composable("chat_screen/{friendId}") { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId")

                // ChatDetailViewModel'i burada, Factory kullanarak ve homeViewModel'i vererek oluşturuyoruz.
                val chatDetailViewModel: ChatDetailViewModel = viewModel(
                    factory = ChatDetailViewModelFactory(
                        friendId = friendId ?: "",
                        homeViewModel = homeViewModel
                    )
                )

                ChatDetailScreen(
                    navController = navController,
                    chatDetailViewModel = chatDetailViewModel
                )
            }
        }
    }
}