package com.example.smartview.shop

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.smartview.ui.theme.AppColors
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    product: Product? = null,
    pack: ProductPack? = null,
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val priceFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val scrollState = rememberScrollState()
    
    // Form states
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var emailSendStatus by remember { mutableStateOf<String?>(null) }
    var currentTicket by remember { mutableStateOf<EmailJSService.PurchaseTicket?>(null) }
    
    // Calculate total
    val totalAmount = pack?.discountedPrice ?: product?.price ?: 0.0
    val itemName = pack?.name ?: product?.name ?: ""
    val itemDescription = pack?.description ?: product?.description ?: ""
    val imageResId = pack?.products?.firstOrNull()?.imageResId ?: product?.imageResId
    
    val isFormValid = customerName.isNotBlank() && 
                      customerEmail.isNotBlank() && 
                      customerPhone.isNotBlank() && 
                      shippingAddress.isNotBlank()
    
    // Stripe Payment Sheet
    if (showPaymentSheet) {
        StripePaymentSheet(
            amount = totalAmount,
            onPaymentResult = { result ->
                showPaymentSheet = false
                when (result) {
                    is PaymentResult.Success -> {
                        // Create the purchase ticket
                        val ticket = if (pack != null) {
                            EmailJSService.createPackTicket(
                                pack = pack,
                                customerName = customerName,
                                customerEmail = customerEmail,
                                customerPhone = customerPhone,
                                shippingAddress = shippingAddress
                            )
                        } else if (product != null) {
                            EmailJSService.createProductTicket(
                                product = product,
                                customerName = customerName,
                                customerEmail = customerEmail,
                                customerPhone = customerPhone,
                                shippingAddress = shippingAddress
                            )
                        } else null
                        
                        currentTicket = ticket
                        showSuccessDialog = true
                        
                        // Auto-send email via EmailJS
                        if (ticket != null) {
                            emailSendStatus = "sending"
                            scope.launch {
                                val emailResult = EmailJSService.sendTicketEmail(ticket)
                                if (emailResult.isSuccess) {
                                    emailSendStatus = "success"
                                } else {
                                    emailSendStatus = "error"
                                    val errorMsg = emailResult.exceptionOrNull()?.message ?: "Error desconocido"
                                    Toast.makeText(context, "EmailJS Error: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    is PaymentResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                    is PaymentResult.Cancelled -> {
                        // User cancelled, do nothing
                    }
                    else -> {}
                }
            },
            onDismiss = { showPaymentSheet = false }
        )
    }
    
    // Success dialog - email is sent automatically
    if (showSuccessDialog && currentTicket != null) {
        AlertDialog(
            onDismissRequest = { },
            icon = { 
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null,
                    tint = AppColors.Success,
                    modifier = Modifier.size(48.dp)
                ) 
            },
            title = { Text("¡Pedido Confirmado!", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Tu pedido ha sido procesado exitosamente.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Orden: ${currentTicket!!.orderId}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Email status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (emailSendStatus) {
                            "sending" -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AppColors.PrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Enviando ticket a ${currentTicket!!.customerEmail}...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary
                                )
                            }
                            "success" -> {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = AppColors.Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Ticket enviado a ${currentTicket!!.customerEmail}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.Success
                                )
                            }
                            "error" -> {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppColors.Warning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "No se pudo enviar el ticket. Configura EmailJS.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.Warning
                                )
                            }
                            else -> {}
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onPaymentComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                    enabled = emailSendStatus != "sending"
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen del Pedido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (imageResId != null) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = itemName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = if (pack != null) "${pack.products.size} productos" else "Producto individual",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                    
                    if (pack != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        pack.products.forEach { p ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AppColors.Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextPrimary
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            if (pack != null) {
                                Text(
                                    text = priceFormat.format(pack.originalPrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = priceFormat.format(totalAmount),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryBlue
                            )
                        }
                    }
                }
            }
            
            // Customer Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información del Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = AppColors.PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            focusedLabelColor = AppColors.PrimaryBlue
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = { customerEmail = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = AppColors.PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            focusedLabelColor = AppColors.PrimaryBlue
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = AppColors.PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            focusedLabelColor = AppColors.PrimaryBlue
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = shippingAddress,
                        onValueChange = { shippingAddress = it },
                        label = { Text("Dirección de envío") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = AppColors.PrimaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            focusedLabelColor = AppColors.PrimaryBlue
                        )
                    )
                }
            }
            
            // Payment Method Card (Stripe placeholder)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.Success
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Método de Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Stripe integration placeholder
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.SurfaceLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = AppColors.PrimaryBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Pago seguro con Stripe",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = "Tarjeta de crédito o débito",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "🔒 Tus datos están protegidos con encriptación SSL",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pay Button - Opens Stripe PaymentSheet
            Button(
                onClick = {
                    // Show Stripe PaymentSheet for card input
                    showPaymentSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryBlue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pagar ${priceFormat.format(totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
