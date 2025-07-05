import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items'ı bu paketten import etmeliyiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.url1

@Composable
fun HomeScreen(
    navController: NavController,
    postList: List<Posts>
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(onOptionsMenuClick = { showToast(context, "Options Tıklandı") })
        },
        bottomBar = {
            BottomNavigationBar(context = context)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(items = postList) { post ->
                PostItem(post = post)
            }
        }
    }
}

@Composable
fun TopAppBar(onOptionsMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Memories Book",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        myImageButton(id = R.drawable.options_ico, onClick = onOptionsMenuClick)
    }
}

@Composable
fun BottomNavigationBar(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        myImageButton(id = R.drawable.home_icon, tintColor = Color.Black) {
            showToast(
                context,
                "Home Tıklandı"
            )
        }
        myImageButton(id = R.drawable.person_ico, tintColor = Color.Black) {
            showToast(
                context,
                "Person Tıklandı"
            )
        }
        myImageButton(id = R.drawable.add_post, tintColor = Color.Black) {
            showToast(
                context,
                "Add Post Tıklandı"
            )
        }
        myImageButton(id = R.drawable.message_circle2, tintColor = Color.Black) {
            showToast(
                context,
                "Message Tıklandı"
            )
        }
    }
}

@Composable
fun PostItem(post: Posts) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.email, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = post.downloadUrl, contentDescription = "Post Image", modifier = Modifier.size(400.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.comment)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun prev_Home() {
    val post1 = Posts("mahmutconger@gmail.com", "Bu ilk post ", url1, "1")
    val post2 = Posts("user@test.com", "Bu da ikinci postum!", url1, "2")
    val postList = arrayListOf(post1, post2)
    HomeScreen(rememberNavController(), postList)
}