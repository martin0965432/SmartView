package com.example.smartview.shop

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service for generating and sending purchase tickets via email.
 * 
 * This uses the device's email client to send the ticket.
 * For production, consider integrating with a backend email service like:
 * - SendGrid
 * - Mailgun
 * - AWS SES
 */
object EmailService {
    
    private val priceFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
    
    /**
     * Data class representing a purchase ticket.
     */
    data class PurchaseTicket(
        val orderId: String = generateOrderId(),
        val customerName: String,
        val customerEmail: String,
        val customerPhone: String,
        val shippingAddress: String,
        val itemName: String,
        val itemDescription: String,
        val products: List<String>,
        val subtotal: Double,
        val discount: Double = 0.0,
        val total: Double,
        val purchaseDate: Date = Date(),
        val paymentMethod: String = "Stripe - Tarjeta"
    )
    
    /**
     * Generate a unique order ID.
     */
    private fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "VS-${timestamp.toString().takeLast(6)}$random"
    }
    
    /**
     * Generate the email subject for a purchase ticket.
     */
    fun generateSubject(ticket: PurchaseTicket): String {
        return "ViewSmart - Confirmación de Compra #${ticket.orderId}"
    }
    
    /**
     * Generate the email body for a purchase ticket.
     */
    fun generateEmailBody(ticket: PurchaseTicket): String {
        val productsText = ticket.products.joinToString("\n") { "  • $it" }
        
        return """
¡Gracias por tu compra, ${ticket.customerName}!

═══════════════════════════════════════
         TICKET DE COMPRA
         ViewSmart
═══════════════════════════════════════

📋 DETALLES DEL PEDIDO
───────────────────────────────────────
Número de Orden: ${ticket.orderId}
Fecha: ${dateFormat.format(ticket.purchaseDate)}
Método de Pago: ${ticket.paymentMethod}

📦 PRODUCTO(S)
───────────────────────────────────────
${ticket.itemName}
${ticket.itemDescription}

Incluye:
$productsText

💰 RESUMEN DE PAGO
───────────────────────────────────────
Subtotal:     ${priceFormat.format(ticket.subtotal)}
${if (ticket.discount > 0) "Descuento:    -${priceFormat.format(ticket.discount)}" else ""}
───────────────────────────────────────
TOTAL:        ${priceFormat.format(ticket.total)}

📍 DIRECCIÓN DE ENVÍO
───────────────────────────────────────
${ticket.shippingAddress}

📞 CONTACTO
───────────────────────────────────────
Email: ${ticket.customerEmail}
Teléfono: ${ticket.customerPhone}

═══════════════════════════════════════

Tu pedido será procesado en las próximas 24 horas.
Recibirás una notificación cuando sea enviado.

Tiempo estimado de entrega: 3-5 días hábiles

¿Tienes alguna pregunta?
📧 contacto@viewsmart.mx
📞 +52 938 123 4567

─────────────────────────────────────
ViewSmart - Tecnología para la inclusión
www.viewsmart.mx
─────────────────────────────────────
        """.trimIndent()
    }
    
    /**
     * Create a ticket for a product purchase.
     */
    fun createProductTicket(
        product: Product,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        shippingAddress: String
    ): PurchaseTicket {
        return PurchaseTicket(
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            shippingAddress = shippingAddress,
            itemName = product.name,
            itemDescription = product.description,
            products = listOf(product.name),
            subtotal = product.price,
            total = product.price
        )
    }
    
    /**
     * Create a ticket for a pack purchase.
     */
    fun createPackTicket(
        pack: ProductPack,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        shippingAddress: String
    ): PurchaseTicket {
        return PurchaseTicket(
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            shippingAddress = shippingAddress,
            itemName = pack.name,
            itemDescription = pack.description,
            products = pack.products.map { it.name },
            subtotal = pack.originalPrice,
            discount = pack.savings,
            total = pack.discountedPrice
        )
    }
    
    /**
     * Send the purchase ticket via email using device's email client.
     */
    fun sendTicketEmail(context: Context, ticket: PurchaseTicket) {
        val subject = generateSubject(ticket)
        val body = generateEmailBody(ticket)
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ticket.customerEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Enviar ticket por email"))
        } catch (e: Exception) {
            // Fallback: open email with just the data
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${ticket.customerEmail}?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
            }
            context.startActivity(fallbackIntent)
        }
    }
}
