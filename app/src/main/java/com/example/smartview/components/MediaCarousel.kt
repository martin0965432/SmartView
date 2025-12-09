package com.example.smartview.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

sealed class MediaItem {
    data class Video(val resId: Int) : MediaItem()
    data class Image(val resId: Int) : MediaItem()
}

@Composable
fun MediaCarousel(
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Contenido actual (video o imagen)
        when (val currentItem = mediaItems[currentIndex]) {
            is MediaItem.Video -> {
                VideoBanner(
                    videoResId = currentItem.resId,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is MediaItem.Image -> {
                Image(
                    painter = painterResource(id = currentItem.resId),
                    contentDescription = "Imagen ${currentIndex + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Controles de navegación
        if (mediaItems.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón anterior
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex > 0) currentIndex - 1 else mediaItems.size - 1
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
                }

                // Indicadores de página
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mediaItems.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentIndex) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentIndex) Color.White 
                                    else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                // Botón siguiente
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex < mediaItems.size - 1) currentIndex + 1 else 0
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
                }
            }
        }
    }
}
