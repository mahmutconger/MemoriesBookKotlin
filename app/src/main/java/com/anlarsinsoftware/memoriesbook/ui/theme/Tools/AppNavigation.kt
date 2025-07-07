package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.url1
import com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen
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
            val post1=Posts("mahmutconger@gmail.com","05.07.2025","Bu ilk post ",url1,"1")
            val post3=Posts("mahmutconger@gmail.com","01.07.2025","2.projem 2. video ",url1,"2")
            val post4=Posts("mahmutconger@gmail.com","11.03.2025","bugün günlerden cumartesi",url1,"3")
            val post5=Posts("mahmutconger@gmail.com","18.02.2025","Ödevi unutma",url1,"4")
            val post2=Posts("mahmutconger@gmail.com","21.07.2024","ne yapacağız",url1,"5")
            var postList=ArrayList<Posts>()
            postList.add(post1)
            postList.add(post2)
            postList.add(post3)
            postList.add(post4)
            postList.add(post5)

            val comment1=Comments("bu post yeni","07.09.2023","Can Conger","1","c1")
            val comment11=Comments("bu post eski","05.12.2025","Mahmut Çönger","1","c11")
            val comment2=Comments("selamlaarr","06.09.2024","Can MAHMUT","2","c2")
            val comment3=Comments("ne garip ama","05.05.2025","Can Sivas","3","c3")
            val comment4=Comments("nabeeri","12.09.2023","Sivas Conger","4","c4")
            val comment5=Comments("bgun daha ne yapacağızz","25.11.2021","as Conger","5","c5")
            var commentList=ArrayList<Comments>()
            commentList.add(comment1)
            commentList.add(comment11)
            commentList.add(comment2)
            commentList.add(comment3)
            commentList.add(comment4)
            commentList.add(comment5)
            commentList.add(comment1)
            commentList.add(comment2)
            commentList.add(comment3)
            commentList.add(comment4)
            commentList.add(comment5)

            HomeScreen(navController, postList =postList,commentList=commentList )
        }
        composable("createPost_screen"){
            CreatePostScreen(navController)
        }

    }
}