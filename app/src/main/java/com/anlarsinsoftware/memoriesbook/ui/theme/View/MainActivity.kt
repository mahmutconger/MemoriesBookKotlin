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
    private fun gonderiler(){
        val post1=Posts("mahmutconger@gmail.com","Bu ilk post ",url1,"1")
        val post3=Posts("mahmutconger@gmail.com","2.projem 2. video ",url1,"2")
        val post4=Posts("mahmutconger@gmail.com","bugün günlerden cumartesi",url1,"2")
        val post5=Posts("mahmutconger@gmail.com","Ödevi unutma",url1,"2")
        val post2=Posts("mahmutconger@gmail.com","ne yapacağız",url1,"2")

        postList.add(post1)
        postList.add(post2)
        postList.add(post3)
        postList.add(post4)
        postList.add(post5)
    }
}






