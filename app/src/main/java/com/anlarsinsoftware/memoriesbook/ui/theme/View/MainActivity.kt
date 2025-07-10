package com.anlarsinsoftware.memoriesbook.ui.theme.View
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.AppNavigation
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.MemoriesBookTheme

class MainActivity : ComponentActivity() {
    private val postList=ArrayList<Posts>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoriesBookTheme {
                AppNavigation()
            }
        }
    }
}






