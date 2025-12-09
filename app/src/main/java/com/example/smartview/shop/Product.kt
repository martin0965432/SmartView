package com.example.smartview.shop

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smartview.R

/**
 * Represents a single product in the ViewSmart catalog.
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageResId: Int,
    val icon: ImageVector,
    val features: List<String>
)

/**
 * Represents a pack of products with a discount.
 */
data class ProductPack(
    val id: String,
    val name: String,
    val description: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val products: List<Product>,
    val savings: Double
)

/**
 * Catalog of all available products.
 */
object ProductCatalog {
    
    val baston = Product(
        id = "baston_inteligente",
        name = "Bastón Inteligente",
        description = "Sensores ultrasónicos de alta precisión para detectar obstáculos a nivel del suelo.",
        price = 2499.0,
        imageResId = R.drawable.baston,
        icon = Icons.Default.Place,
        features = listOf(
            "Sensores ultrasónicos de alta precisión",
            "Detección de obstáculos a nivel del suelo",
            "Alertas vibratorias y sonoras",
            "Batería de larga duración (48 horas)",
            "Resistente al agua (IP67)"
        )
    )
    
    val gafas = Product(
        id = "gafas_inteligentes",
        name = "Gafas Inteligentes",
        description = "Tecnología integrada para detectar obstáculos a altura de cabeza.",
        price = 3999.0,
        imageResId = R.drawable.gafas1,
        icon = Icons.Default.Face,
        features = listOf(
            "Sensores ultrasónicos integrados",
            "Detección de obstáculos a altura de cabeza",
            "Alertas de audio direccionales",
            "Diseño ligero y ergonómico",
            "Compatible con lentes graduados"
        )
    )
    
    val dije = Product(
        id = "dije_sensores",
        name = "Dije con Sensores",
        description = "Dispositivo discreto para detección de obstáculos a altura media.",
        price = 1999.0,
        imageResId = R.drawable.dije,
        icon = Icons.Default.FavoriteBorder,
        features = listOf(
            "Sensores ultrasónicos de torso",
            "Detección de obstáculos a altura media",
            "Alertas táctiles suaves",
            "Diseño discreto y elegante",
            "Sincronización automática con otros dispositivos"
        )
    )
    
    val allProducts = listOf(baston, gafas, dije)
    
    // Packs - Always include bastón, no mixing gafas+dije without bastón
    val packVision = ProductPack(
        id = "pack_vision",
        name = "Pack Visión",
        description = "Cobertura superior e inferior para máxima seguridad.",
        originalPrice = baston.price + gafas.price,
        discountedPrice = 5999.0,
        products = listOf(baston, gafas),
        savings = (baston.price + gafas.price) - 5999.0
    )
    
    val packEsencial = ProductPack(
        id = "pack_esencial",
        name = "Pack Esencial",
        description = "Cobertura inferior y media para movilidad diaria.",
        originalPrice = baston.price + dije.price,
        discountedPrice = 3999.0,
        products = listOf(baston, dije),
        savings = (baston.price + dije.price) - 3999.0
    )
    
    val packCompleto = ProductPack(
        id = "pack_completo",
        name = "Pack Completo",
        description = "Sistema integral de 360° para protección total.",
        originalPrice = baston.price + gafas.price + dije.price,
        discountedPrice = 7499.0,
        products = listOf(baston, gafas, dije),
        savings = (baston.price + gafas.price + dije.price) - 7499.0
    )
    
    val allPacks = listOf(packVision, packEsencial, packCompleto)
}

/**
 * Represents a purchase order.
 */
data class PurchaseOrder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val item: Any, // Product or ProductPack
    val totalAmount: Double,
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val shippingAddress: String = ""
)
