package com.example.smartview.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartview.R
import com.example.smartview.components.MediaCarousel
import com.example.smartview.components.MediaItem
import com.example.smartview.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToShop: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    userData = userData,
                    onSignOut = {
                        scope.launch {
                            drawerState.close()
                            onSignOut()
                        }
                    },
                    onNavigateToProducts = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToProducts()
                        }
                    },
                    onNavigateToContact = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToContact()
                        }
                    },
                    onNavigateToProfile = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToProfile()
                        }
                    },
                    onNavigateToShop = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToShop()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ViewSmart", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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
            MainContent(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun DrawerContent(
    userData: UserData?,
    onSignOut: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToShop: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilePreferences = remember { ProfilePreferences(context) }
    val localPhotoPath = profilePreferences.getSavedPhotoPath()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile photo - prioritize local, then Google, then placeholder
            val imageToShow: Any? = localPhotoPath?.let { java.io.File(it) } ?: userData?.profilePictureUrl
            
            if (imageToShow != null) {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = AppColors.PrimaryBlue,
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppColors.PrimaryLight.copy(alpha = 0.2f))
                        .border(
                            width = 3.dp,
                            color = AppColors.PrimaryBlue,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = AppColors.PrimaryBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userData?.username ?: "Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            selected = true,
            onClick = { }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Share, contentDescription = null) },
            label = { Text("Nuestra Solución") },
            selected = false,
            onClick = onNavigateToProducts
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Email, contentDescription = null) },
            label = { Text("Contacto") },
            selected = false,
            onClick = onNavigateToContact
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Perfil") },
            selected = false,
            onClick = onNavigateToProfile
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Tienda") },
            selected = false,
            onClick = onNavigateToShop
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
            label = { Text("Cerrar Sesión") },
            selected = false,
            onClick = { onSignOut() },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = MaterialTheme.colorScheme.error,
                unselectedIconColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "media_carousel") {
            MediaCarousel(
                mediaItems = listOf(
                    MediaItem.Video(R.raw.banner_video_1),
                    MediaItem.Image(R.drawable.baston),
                    MediaItem.Image(R.drawable.gafas1),
                    MediaItem.Image(R.drawable.gafas2),
                    MediaItem.Image(R.drawable.gafas3),
                    MediaItem.Image(R.drawable.gafas4),
                    MediaItem.Image(R.drawable.gafas5),
                    MediaItem.Image(R.drawable.gafas6),
                    MediaItem.Image(R.drawable.gafas7),
                    MediaItem.Image(R.drawable.dije)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionTitle(
                title = "El Desafío Visual",
                icon = Icons.Default.Warning
            )
        }

        item {
            ProblemCard(
                title = "253 Millones de Personas",
                description = "Viven con discapacidad visual moderada o severa en todo el mundo",
                color = Color(0xFFE53935)
            )
        }

        item {
            ProblemCard(
                title = "36 Millones",
                description = "Experimentan ceguera total, enfrentando desafíos diarios de movilidad",
                color = Color(0xFFFB8C00)
            )
        }

        item {
            ProblemCard(
                title = "Limitaciones Actuales",
                description = "Los bastones tradicionales solo detectan obstáculos a nivel del suelo",
                color = Color(0xFFFDD835)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "La Solución ViewSmart",
                icon = Icons.Default.Star
            )
        }

        item {
            Text(
                text = "Un ecosistema integrado de tres dispositivos que trabajan en armonía para proporcionar una cobertura completa de 360 grados.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item(key = "baston_inteligente") {
            ProductCard(
                title = "Bastón Inteligente",
                icon = Icons.Default.Place,
                features = listOf(
                    "Sensores ultrasónicos de alta precisión",
                    "Detección de obstáculos a nivel del suelo",
                    "Alertas vibratorias y sonoras",
                    "Batería de larga duración (48 horas)",
                    "Resistente al agua (IP67)"
                ),
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
            )
        }

        item(key = "gafas_inteligentes") {
            ProductCard(
                title = "Gafas Inteligentes",
                icon = Icons.Default.Face,
                features = listOf(
                    "Sensores ultrasónicos integrados",
                    "Detección de obstáculos a altura de cabeza",
                    "Alertas de audio direccionales",
                    "Diseño ligero y ergonómico",
                    "Compatible con lentes graduados"
                ),
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
            )
        }

        item(key = "dije_sensores") {
            ProductCard(
                title = "Dije con Sensores",
                icon = Icons.Default.FavoriteBorder,
                features = listOf(
                    "Sensores ultrasónicos de torso",
                    "Detección de obstáculos a altura media",
                    "Alertas táctiles suaves",
                    "Diseño discreto y elegante",
                    "Sincronización automática con otros dispositivos"
                ),
                gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "Impacto Real",
                icon = Icons.Default.Star
            )
        }

        item {
            ImpactCard(
                quote = "ViewSmart ha transformado mi vida. Ahora puedo moverme con confianza y seguridad.",
                author = "María González, usuaria desde 2023"
            )
        }

        item {
            ImpactCard(
                quote = "La tecnología de 360 grados me da una libertad que nunca pensé posible.",
                author = "Carlos Ramírez, usuario desde 2024"
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle(
                title = "Beneficios Clave",
                icon = Icons.Default.CheckCircle
            )
        }

        item {
            BenefitsList()
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ViewSmart - Innovación que transforma vidas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ProblemCard(title: String, description: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
                    .align(Alignment.Top)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = color
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    title: String,
    icon: ImageVector,
    features: List<String>,
    gradientColors: List<Color>,
    imageResId: Int? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (imageResId != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = imageResId),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp)
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

@Composable
fun ImpactCard(quote: String, author: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "— $author",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun BenefitsList() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BenefitItem(
                icon = Icons.Default.Lock,
                text = "Cobertura de 360 grados para máxima seguridad"
            )
            BenefitItem(
                icon = Icons.Default.Notifications,
                text = "Respuesta en tiempo real ante obstáculos"
            )
            BenefitItem(
                icon = Icons.Default.Settings,
                text = "Autonomía extendida para uso diario"
            )
            BenefitItem(
                icon = Icons.Default.Refresh,
                text = "Sincronización automática entre dispositivos"
            )
            BenefitItem(
                icon = Icons.Default.Person,
                text = "Mayor independencia y confianza"
            )
            BenefitItem(
                icon = Icons.Default.ThumbUp,
                text = "Mejora significativa en calidad de vida"
            )
        }
    }
}

@Composable
fun BenefitItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}