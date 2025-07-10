package com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController:NavController,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    Scaffold (topBar = {
        TopAppBar(title = {
            Text(
                "Settings",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        tint= MaterialTheme.colorScheme.primary,
                        contentDescription = "Geri Butonu"
                    )
                }
            },
            actions = {
                Image(
                    painter = painterResource(R.drawable.pp),
                    "Profil resmi",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, myBrush(), CircleShape)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent))


    }){innerPadding->
        Column (modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally){


            myCard("Koyu Mod", rowScope = {
                Switch(
                    checked = isDarkMode, // Switch'in durumu dışarıdan gelen state'e bağlı
                    onCheckedChange = { onThemeToggle() } // Değiştiğinde dışarıdan gelen fonksiyonu çağırır
                )
            })

            myCard("Diğer Ayarlar", rowScope = {
                Switch(
                    checked = isDarkMode, // Switch'in durumu dışarıdan gelen state'e bağlı
                    onCheckedChange = { onThemeToggle() } // Değiştiğinde dışarıdan gelen fonksiyonu çağırır
                )
            })

        }
    }
}

@Composable
fun myCard(title:String, rowScope: @Composable () -> Unit) {
    Card (Modifier
        .fillMaxWidth()
        .padding(15.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(2.dp,Color.LightGray)
    ){

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            rowScope()

        }
    }
}



@Preview(showBackground = true)
@Composable
private fun prev_setttings() {
    SettingsScreen(rememberNavController(),true,{})
}