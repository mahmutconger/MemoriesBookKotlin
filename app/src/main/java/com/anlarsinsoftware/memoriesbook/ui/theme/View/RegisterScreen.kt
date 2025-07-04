package com.anlarsinsoftware.memoriesbook.ui.theme.View

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myTextField

@Composable
fun RegisterScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = myBrush()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var userNameReg by remember { mutableStateOf("") }
            var userEmailReg by remember { mutableStateOf("") }
            var passwordReg by remember{ mutableStateOf("")}
            var passwordRegVerify by remember{ mutableStateOf("")}
            myTextField(
                userNameReg,
                "Kullanıcı Adı",
                false
            ) {
                userNameReg = it
            }
            mySpacer(24)
            myTextField(userEmailReg,
                "Email",
                false) {
                userEmailReg = it
            }
            mySpacer(24)
            myTextField(passwordReg,
                "Şifre",
                false) {
                passwordReg=it
            }
            mySpacer(24)
            myTextField(passwordRegVerify,
                "Şifre Onay",
                false) {
                passwordRegVerify=it
            }
            mySpacer(32)
            myButton("Kayıt Ol",false) {
                if(!passwordReg.equals(passwordRegVerify)){
                    Log.d("tagReg","Şifreler Uyuşmuyor")
                }
                else if(userNameReg==""||userEmailReg==""||passwordReg==""){
                    Log.d("tagReg","Alanları Boş Bırakmayınız.")
                }
                else{
                    Log.d("tagReg","Kayıt Başarılı")
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun prev_Register() {
    RegisterScreen(rememberNavController())
}