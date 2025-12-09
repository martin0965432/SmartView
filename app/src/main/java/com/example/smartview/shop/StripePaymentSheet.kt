package com.example.smartview.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.smartview.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/**
 * Stripe-style Payment Sheet simulation.
 * Uses Stripe test cards for simulation:
 * - 4242 4242 4242 4242 → Success
 * - 4000 0000 0000 0002 → Declined
 */
@Composable
fun StripePaymentSheet(
    amount: Double,
    onPaymentResult: (PaymentResult) -> Unit,
    onDismiss: () -> Unit
) {
    val priceFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isFormValid = cardNumber.replace(" ", "").length == 16 &&
                      expiryDate.length == 5 &&
                      cvc.length >= 3 &&
                      cardholderName.isNotBlank()
    
    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pago Seguro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryDark
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                
                // Amount
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.SurfaceLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total a pagar:", color = AppColors.TextSecondary)
                        Text(
                            text = priceFormat.format(amount),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryBlue
                        )
                    }
                }
                
                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { 
                        val digits = it.replace(" ", "").filter { c -> c.isDigit() }
                        if (digits.length <= 16) {
                            cardNumber = digits.chunked(4).joinToString(" ")
                        }
                        errorMessage = null
                    },
                    label = { Text("Número de tarjeta") },
                    placeholder = { Text("4242 4242 4242 4242") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = AppColors.PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isProcessing,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        focusedLabelColor = AppColors.PrimaryBlue
                    )
                )
                
                // Expiry and CVC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { 
                            val digits = it.filter { c -> c.isDigit() }
                            if (digits.length <= 4) {
                                expiryDate = if (digits.length >= 2) {
                                    digits.take(2) + "/" + digits.drop(2)
                                } else {
                                    digits
                                }
                            }
                        },
                        label = { Text("MM/YY") },
                        placeholder = { Text("12/34") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isProcessing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            focusedLabelColor = AppColors.PrimaryBlue
                        )
                    )
                    
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                cvc = it
                            }
                        },
                        label = { Text("CVC") },
                        placeholder = { Text("123") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isProcessing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.PrimaryBlue,
                            focusedLabelColor = AppColors.PrimaryBlue
                        )
                    )
                }
                
                // Cardholder Name
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it.uppercase() },
                    label = { Text("Nombre del titular") },
                    placeholder = { Text("NOMBRE COMO APARECE EN LA TARJETA") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = AppColors.PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isProcessing,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.PrimaryBlue,
                        focusedLabelColor = AppColors.PrimaryBlue
                    )
                )
                
                // Error Message
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Test cards info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF3E5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🧪 Modo Prueba",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                        Text(
                            "4242 4242 4242 4242 → Exitoso",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7B1FA2)
                        )
                        Text(
                            "4000 0000 0000 0002 → Rechazado",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
                
                // Pay Button
                Button(
                    onClick = {
                        isProcessing = true
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Procesando...")
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pagar ${priceFormat.format(amount)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Security note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Pago seguro con encriptación SSL",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
    }
    
    // Process payment when isProcessing becomes true
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            // Simulate network delay
            delay(2000)
            
            val cardDigits = cardNumber.replace(" ", "")
            
            when {
                // Test cards
                cardDigits == "4242424242424242" -> {
                    // Success
                    onPaymentResult(PaymentResult.Success(
                        paymentIntentId = "pi_test_${System.currentTimeMillis()}",
                        orderId = "VS-${System.currentTimeMillis().toString().takeLast(6)}${(1000..9999).random()}"
                    ))
                }
                cardDigits == "4000000000000002" -> {
                    // Declined
                    errorMessage = "Tu tarjeta fue rechazada. Por favor, intenta con otra tarjeta."
                    isProcessing = false
                }
                cardDigits.startsWith("4") -> {
                    // Other Visa cards - simulate success for testing
                    onPaymentResult(PaymentResult.Success(
                        paymentIntentId = "pi_test_${System.currentTimeMillis()}",
                        orderId = "VS-${System.currentTimeMillis().toString().takeLast(6)}${(1000..9999).random()}"
                    ))
                }
                cardDigits.startsWith("5") -> {
                    // Mastercard - simulate success
                    onPaymentResult(PaymentResult.Success(
                        paymentIntentId = "pi_test_${System.currentTimeMillis()}",
                        orderId = "VS-${System.currentTimeMillis().toString().takeLast(6)}${(1000..9999).random()}"
                    ))
                }
                else -> {
                    errorMessage = "Número de tarjeta inválido. Por favor verifica los datos."
                    isProcessing = false
                }
            }
        }
    }
}
