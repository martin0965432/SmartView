package com.example.smartview.shop

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * EmailJS Service for sending automatic purchase tickets.
 * 
 * SETUP INSTRUCTIONS:
 * 1. Create account at https://www.emailjs.com/
 * 2. Add Email Service (Gmail) - get SERVICE_ID
 * 3. Create Email Template - get TEMPLATE_ID
 * 4. Get your Public Key from Account > API Keys
 * 5. Replace the constants below with your IDs
 * 
 * TEMPLATE VARIABLES (create these in your EmailJS template):
 * {{to_email}} - Customer email
 * {{to_name}} - Customer name
 * {{order_id}} - Order number
 * {{order_date}} - Purchase date
 * {{item_name}} - Product/Pack name
 * {{products_list}} - List of products
 * {{subtotal}} - Subtotal amount
 * {{discount}} - Discount (if any)
 * {{total}} - Total amount
 * {{shipping_address}} - Shipping address
 * {{customer_phone}} - Customer phone
 */
object EmailJSService {
    
    // EmailJS Credentials - ViewSmart
    private const val SERVICE_ID = "service_1tqaqe8"
    private const val TEMPLATE_ID = "template_rvh8eha"
    private const val PUBLIC_KEY = "81cBISkDKys0O1BZW"
    
    private const val EMAILJS_API = "https://api.emailjs.com/api/v1.0/email/send"
    
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
        val purchaseDate: Date = Date()
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
     * Send the purchase ticket via EmailJS API.
     * Returns true if successful, false otherwise.
     */
    suspend fun sendTicketEmail(ticket: PurchaseTicket): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(EMAILJS_API)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                
                // Build template parameters
                val templateParams = JSONObject().apply {
                    put("to_email", ticket.customerEmail)
                    put("to_name", ticket.customerName)
                    put("order_id", ticket.orderId)
                    put("order_date", dateFormat.format(ticket.purchaseDate))
                    put("item_name", ticket.itemName)
                    put("item_description", ticket.itemDescription)
                    put("products_list", ticket.products.joinToString("\n• ", prefix = "• "))
                    put("subtotal", priceFormat.format(ticket.subtotal))
                    put("discount", if (ticket.discount > 0) "-${priceFormat.format(ticket.discount)}" else "N/A")
                    put("total", priceFormat.format(ticket.total))
                    put("shipping_address", ticket.shippingAddress)
                    put("customer_phone", ticket.customerPhone)
                }
                
                // Build request body
                val requestBody = JSONObject().apply {
                    put("service_id", SERVICE_ID)
                    put("template_id", TEMPLATE_ID)
                    put("user_id", PUBLIC_KEY)
                    put("template_params", templateParams)
                }
                
                // Send request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("EmailJS", "Email sent successfully to ${ticket.customerEmail}")
                    Result.success(true)
                } else {
                    val errorMessage = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e("EmailJS", "Failed to send email: $responseCode - $errorMessage")
                    Result.failure(Exception("Error $responseCode: $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("EmailJS", "Exception sending email", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Check if EmailJS is configured.
     */
    fun isConfigured(): Boolean {
        return SERVICE_ID != "YOUR_SERVICE_ID" && 
               TEMPLATE_ID != "YOUR_TEMPLATE_ID" && 
               PUBLIC_KEY != "YOUR_PUBLIC_KEY"
    }
}
