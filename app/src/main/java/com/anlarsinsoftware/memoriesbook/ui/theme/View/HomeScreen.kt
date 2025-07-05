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

// 1. HomeScreen'i Scaffold ile yeniden yapılandırıyoruz.
@Composable
fun HomeScreen(
    navController: NavController,
    postList: List<Posts>
) { // ArrayList yerine List kullanmak daha iyi bir pratiktir.
    val context = LocalContext.current

    Scaffold(
        topBar = {
            // Scaffold'un üst bar yuvasına kendi Row'umuzu koyuyoruz.
            TopAppBar(onOptionsMenuClick = { showToast(context, "Options Tıklandı") })
        },
        bottomBar = {
            // Scaffold'un alt bar yuvasına kendi Row'umuzu koyuyoruz.
            BottomNavigationBar(context = context)
        }
    ) { innerPadding ->
        // Scaffold'un ana içerik alanı burasıdır.
        // 'innerPadding' parametresi, içeriğin üst ve alt barların altına girmesini engeller.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 2. Scaffold'un verdiği padding'i uyguluyoruz.
        ) {
            // 3. Sabit bir sayı yerine, gelen postList'i kullanıyoruz.
            items(items = postList) { post ->
                PostItem(post = post)
            }
        }
    }
}

// Okunabilirlik için üst barı ayrı bir Composable'a taşıdık.
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

// Okunabilirlik için alt barı ayrı bir Composable'a taşıdık.
@Composable
fun BottomNavigationBar(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        myImageButton(id = R.drawable.home_icon) { showToast(context, "Home Tıklandı") }
        myImageButton(id = R.drawable.person_ico) { showToast(context, "Person Tıklandı") }
        myImageButton(id = R.drawable.add_post) { showToast(context, "Add Post Tıklandı") }
        myImageButton(id = R.drawable.message_circle2) { showToast(context, "Message Tıklandı") }
    }
}

// Her bir post'un nasıl görüneceğini belirleyen Composable
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
            AsyncImage(model = post.downloadUrl, contentDescription = "Post Image")

            Text(text = post.comment)
        }
    }
}

// myImageButton ve showToast fonksiyonları aynı kalabilir.
@Composable
fun myImageButton(id: Int, onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .size(30.dp)
            .clickable(onClick = onClick),
        painter = painterResource(id = id),
        contentDescription = "Action Icon",
        colorFilter = ColorFilter.tint(Color.Black)
    )
}

fun showToast(context: Context, msg: String, isLengthLong: Boolean = false) {
    val duration = if (isLengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(context, msg, duration).show()
}


@Preview(showBackground = true)
@Composable
fun prev_Home() {
    val url1 =
        "https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/gorsel%2F12fb5b13-5eaa-44c4-a9f2-df7def4dba05.jpg?alt=media&token=11239fac-d521-4863-bbaf-5afedc313b4e"
    val post1 = Posts("mahmutconger@gmail.com", "Bu ilk post ", url1, "1")
    val post2 = Posts("user@test.com", "Bu da ikinci postum!", url1, "2")
    val postList = arrayListOf(post1, post2) // arrayListOf() daha kısa bir kullanım
    HomeScreen(rememberNavController(), postList)
}