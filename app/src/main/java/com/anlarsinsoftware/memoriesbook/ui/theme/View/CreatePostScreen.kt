package com.anlarsinsoftware.memoriesbook.ui.theme.View

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast

@Composable
fun CreatePostScreen(navController: NavController) {
    var postDescription by remember { mutableStateOf("") }
    val context = LocalContext.current
    Scaffold(topBar = {

        topBar {
            navController.popBackStack()
        }
    }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.add_post),
                contentDescription = "Resim seçmek için basınız.",
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp)
                    .clickable {
                        //Fotoğraf seçme kısmı burada olacak.
                    }
            )
            OutlinedTextField(modifier=Modifier.height(100.dp), value= "",
                label = {
                    Text("Açıklama")
                }, onValueChange = {change->
                    postDescription=change
                })
            OutlinedButton(modifier = Modifier.padding(10.dp), onClick = {
                showToast(context,"Gönderi Oluşturuluyor...")
            }) {
                Text("Yükle")
            }

        }

    }
}

@Composable
fun topBar(onBackCilck:()->Unit) {
    Row(modifier = Modifier.fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically){
        IconButton(onClick = { onBackCilck() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                tint= Color.Black,
                contentDescription = "Geri Butonu"
            )
        }

        Text("Anı Oluştur",
            Modifier.fillMaxWidth(),
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center)

    }
}

@Preview
@Composable
private fun prev_createPost() {

    CreatePostScreen(rememberNavController())
}

