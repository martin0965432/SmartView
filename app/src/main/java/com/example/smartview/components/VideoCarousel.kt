package com.example.smartview.components

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp

@Composable
fun VideoCarousel(
    videoResIds: List<Int>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Video actual
        VideoBanner(
            videoResId = videoResIds[currentIndex],
            modifier = Modifier.fillMaxSize()
        )

        // Controles de navegación
        if (videoResIds.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón anterior
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex > 0) currentIndex - 1 else videoResIds.size - 1
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
                }

                // Botón siguiente
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex < videoResIds.size - 1) currentIndex + 1 else 0
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
                }
            }
        }
    }
}
