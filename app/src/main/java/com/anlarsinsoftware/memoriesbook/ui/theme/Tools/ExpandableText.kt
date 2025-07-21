package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    collapsedMaxLines: Int = 3,
    style: TextStyle = LocalTextStyle.current,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    // "daha fazla oku" ve "daha az göster" metinleri için stiller
    val clickableStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    Column(
        modifier = modifier
            .clickable(
                // Sadece metin sığmadığında tıklanabilir yap
                enabled = hasOverflow,
                onClick = { isExpanded = !isExpanded }
            )
            // Açılıp kapanırken yumuşak bir animasyon ekler
            .animateContentSize()
    ) {
        Text(
            text = text,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            onTextLayout = { textLayoutResult ->
                if (!hasOverflow) {
                    hasOverflow = textLayoutResult.didOverflowHeight
                }
            },
            style = style
        )

        // Eğer metin sığmıyorsa ve genişletilmişse "daha az göster" yazısını ekle
        if (hasOverflow && isExpanded) {
            Text(
                text = "daha az göster",
                style = style.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Eğer metin sığmıyorsa ve daraltılmışsa "daha fazla oku" yazısını ekle
        if (hasOverflow && !isExpanded) {
            Text(
                text = "...daha fazla oku",
                style = style.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}