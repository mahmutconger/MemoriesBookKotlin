package com.anlarsinsoftware.memoriesbook.ui.theme.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold (
        topBar = {
            TopAppBar(
                // 1. Başlık bölümü. Ortalanması için Modifier ekliyoruz.
                title = {
                    Text(
                        "Sohbetler",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                // 2. Navigasyon ikonu (soldaki ikon).
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Geri Butonu"
                        )
                    }
                },

                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Geri Butonu"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }, bottomBar = {
            BottomNavigationBar(
                context = context,
                createPostClick = {
                    navController.navigate("createPost_screen")
                }, profileClick = {

                }, messageClick = {

                }, homeClick = {
                    navController.navigate("home_screen")
                })
        }
    ){ innerPadding->
        Column (modifier = Modifier.padding(innerPadding)){

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun mes_prev() {
    MessagesScreen(rememberNavController())
}