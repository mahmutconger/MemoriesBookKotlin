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
            val post1=Posts("mahmutconger@gmail.com","Bu ilk post ",url1,"1")
            val post3=Posts("mahmutconger@gmail.com","2.projem 2. video ",url1,"2")
            val post4=Posts("mahmutconger@gmail.com","bugün günlerden cumartesi",url1,"2")
            val post5=Posts("mahmutconger@gmail.com","Ödevi unutma",url1,"2")
            val post2=Posts("mahmutconger@gmail.com","ne yapacağız",url1,"2")
            var postList=ArrayList<Posts>()
            postList.add(post1)
            postList.add(post2)
            postList.add(post3)
            postList.add(post4)
            postList.add(post5)

            HomeScreen(navController, postList =postList )
        }

    }
}