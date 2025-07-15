package com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavController,friendId:String) {


    Scaffold(
        topBar = {
            TopAppBar(
                // 1. Başlık bölümü. Ortalanması için Modifier ekliyoruz.
                title = {
                    Text(
                        "Kullanıcı Adı",
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
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Seçenekler Menüsü"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ){ innerPadding->
        Column (modifier = Modifier.padding(innerPadding)){

        }
    }
}