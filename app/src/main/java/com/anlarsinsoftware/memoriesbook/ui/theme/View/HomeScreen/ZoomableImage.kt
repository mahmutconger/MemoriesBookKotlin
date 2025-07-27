package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ZoomableImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onZoomChanged: (Boolean) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(scale) {
        onZoomChanged(scale > 1.1f)
    }
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.size > 1 || scale > 1f) {
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()

                                val newScale = (scale * zoom).coerceIn(1f, 5f)

                                val newOffset = if (newScale > 1f) {
                                    val imageSize = this.size
                                    val maxOffsetX = (imageSize.width * (newScale - 1)) / 2f
                                    val maxOffsetY = (imageSize.height * (newScale - 1)) / 2f
                                    (offset + pan).let {
                                        Offset(
                                            it.x.coerceIn(-maxOffsetX, maxOffsetX),
                                            it.y.coerceIn(-maxOffsetY, maxOffsetY)
                                        )
                                    }
                                } else {
                                    Offset.Zero
                                }

                                scale = newScale
                                offset = newOffset

                                event.changes.forEach { it.consume() }
                            }
                           

                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = contentScale
        )
    }
}