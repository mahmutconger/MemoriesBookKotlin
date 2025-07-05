package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.url1
import com.anlarsinsoftware.memoriesbook.ui.theme.View.LoginScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.RegisterScreen
import com.anlarsinsoftware.memoriesbook.ui.theme.View.WellComeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome_screen") {
        composable("welcome_screen") {
            WellComeScreen(navController = navController)
        }
        composable("register_screen") {
            RegisterScreen(navController)
        }
        composable("login_screen"){
            LoginScreen(navController)
        }
        composable("home_screen"){
            val post1= Posts("mahmutconger@gmail.com","Bu ilk post ",url1,"1")
            var postList=ArrayList<Posts>()
            postList.add(post1)

            HomeScreen(navController, postList =postList )
        }

    }
}