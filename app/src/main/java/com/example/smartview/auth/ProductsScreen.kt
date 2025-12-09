package com.example.smartview.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartview.R
import com.example.smartview.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuestra Solución", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.PrimaryBlue,
                    titleContentColor = AppColors.TextOnPrimary,
                    navigationIconContentColor = AppColors.TextOnPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.SurfaceLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Un Ecosistema Integrado de Tres Dispositivos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryDark
            )
            
            Text(
                text = "Trabajando en armonía para proporcionar una cobertura completa de 360 grados.",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Producto 1: Bastón Inteligente
            ProductGalleryCard(
                title = "Bastón Inteligente",
                icon = Icons.Default.Place,
                description = "Sensores ultrasónicos de alta precisión para detectar obstáculos a nivel del suelo.",
                features = listOf(
                    "Sensores ultrasónicos de alta precisión",
                    "Detección de obstáculos a nivel del suelo",
                    "Alertas vibratorias y sonoras",
                    "Batería de larga duración (48 horas)",
                    "Resistente al agua (IP67)"
                ),
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                imageResId = R.drawable.baston
            )

            // Producto 2: Gafas Inteligentes
            ProductGalleryCard(
                title = "Gafas Inteligentes",
                icon = Icons.Default.Face,
                description = "Tecnología integrada para detectar obstáculos a altura de cabeza.",
                features = listOf(
                    "Sensores ultrasónicos integrados",
                    "Detección de obstáculos a altura de cabeza",
                    "Alertas de audio direccionales",
                    "Diseño ligero y ergonómico",
                    "Compatible con lentes graduados"
                ),
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
                imageResId = R.drawable.gafas1
            )

            // Producto 3: Dije con Sensores
            ProductGalleryCard(
                title = "Dije con Sensores",
                icon = Icons.Default.FavoriteBorder,
                description = "Dispositivo discreto para detección de obstáculos a altura media.",
                features = listOf(
                    "Sensores ultrasónicos de torso",
                    "Detección de obstáculos a altura media",
                    "Alertas táctiles suaves",
                    "Diseño discreto y elegante",
                    "Sincronización automática con otros dispositivos"
                ),
                gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                imageResId = R.drawable.dije
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProductGalleryCard(
    title: String,
    icon: ImageVector,
    description: String,
    features: List<String>,
    gradientColors: List<Color>,
    imageResId: Int? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Imagen del producto
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder si no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    gradientColors.first().copy(alpha = 0.7f),
                                    gradientColors.last().copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(100.dp),
                        tint = Color.White
                    )
                }
            }
            
            // Información del producto con gradiente
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = gradientColors
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                features.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }
    }
}
