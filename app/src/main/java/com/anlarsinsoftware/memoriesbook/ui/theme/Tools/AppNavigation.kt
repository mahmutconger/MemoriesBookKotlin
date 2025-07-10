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
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.MemoriesBookTheme
import com.anlarsinsoftware.memoriesbook.ui.theme.View.*
import com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen.SettingsScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.ThemeDataStore
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

                val homeViewModel: HomeViewModel = viewModel()
                val commentsViewModel: CommentsViewModel = viewModel()
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
                CreatePostScreen(navController)
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
        }
    }
}