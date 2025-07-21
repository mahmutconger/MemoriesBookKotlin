package com.anlarsinsoftware.memoriesbook.ui.theme.View
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.AppNavigation
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.MemoriesBookTheme
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.let {
            val chatPartnerId = it.getString("chat_partner_id")
            // Eğer bildirimden geliyorsa, başlangıç rotasını ayarla (bu kısım NavController'a iletilmeli)
            // Bu, 'AppNavigation'da bir başlangıç argümanı olarak ele alınabilir.
        }
        enableEdgeToEdge()
        setContent {
            MemoriesBookTheme {
                AppNavigation()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        homeViewModel.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        homeViewModel.setUserOffline()
    }

}






