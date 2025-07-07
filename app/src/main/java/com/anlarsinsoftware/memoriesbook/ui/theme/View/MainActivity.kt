package com.anlarsinsoftware.memoriesbook.ui.theme.View
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.AppNavigation
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.MemoriesBookTheme
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.url1

class MainActivity : ComponentActivity() {
    private val postList=ArrayList<Posts>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoriesBookTheme {
                AppNavigation()
            }
        }
    }
}






