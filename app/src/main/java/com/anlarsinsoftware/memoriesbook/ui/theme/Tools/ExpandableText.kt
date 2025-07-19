package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    collapsedMaxLines: Int = 3,
    style: TextStyle = LocalTextStyle.current,
    seeMoreColor: Color = Color.Gray
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isClickable by remember { mutableStateOf(false) }
    var lastCharIndex by remember { mutableStateOf(0) }

    Box(modifier = modifier.clickable(
        // Sadece tıklanabilir olduğunda (yani metin sığmadığında) tıklamayı aktif et
        enabled = isClickable,
        onClick = { isExpanded = !isExpanded }
    )) {
        Text(
            text = buildAnnotatedString {
                // Eğer metin genişletilmişse, tamamını göster
                if (isExpanded) {
                    append(text)
                }
                // Eğer daraltılmışsa ve metin gerçekten sığıyorsa, tamamını göster
                else if (!isClickable) {
                    append(text)
                }
                // Eğer daraltılmışsa ve metin sığmıyorsa, kesip sonuna "...daha fazla oku" ekle
                else {
                    val readMoreText = " ...daha fazla oku"
                    val adjustedText = text.substring(0, lastCharIndex)
                        .dropLast(readMoreText.length) // Sona eklenecek yazı kadar karakter çıkar
                    append(adjustedText)
                    withStyle(style = SpanStyle(color = seeMoreColor, fontWeight = FontWeight.Bold)) {
                        append(readMoreText)
                    }
                }
            },
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            onTextLayout = { textLayoutResult ->
                // Metin çizildikten sonra, gerçekten sığıp sığmadığını kontrol ediyoruz
                // Eğer metin kesilmişse (overflow), tıklanabilir yapıyoruz.
                if (textLayoutResult.didOverflowHeight) {
                    isClickable = true
                    lastCharIndex = textLayoutResult.getLineEnd(collapsedMaxLines - 1, true)
                }
            },
            style = style
        )
    }
}