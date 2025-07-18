package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts

@Composable
fun EditPostBottomSheet(
    post: Posts,
    onSaveClicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var updatedComment by remember { mutableStateOf(post.comment) }

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Açıklamayı Düzenle", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = updatedComment,
            onValueChange = { updatedComment = it },
            label = { Text("Yeni Açıklama") },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSaveClicked(updatedComment) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Değişiklikleri Kaydet")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}