package com.example.smartview.shop

import android.app.Activity
import android.content.Context
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

/**
 * Configuration for Stripe payment integration.
 * 
 * SETUP INSTRUCTIONS FOR PRODUCTION:
 * 1. Create a Stripe account at https://stripe.com
 * 2. Get your publishable key from Stripe Dashboard
 * 3. Replace STRIPE_PUBLISHABLE_KEY with your actual key
 * 4. Set up a backend server to create PaymentIntents
 * 5. Update BACKEND_URL with your server endpoint
 * 
 * SECURITY NOTE:
 * - Never expose your Secret Key in the app
 * - All payment processing should go through your backend
 * 
 * FOR TESTING:
 * Use Stripe's test mode publishable key (starts with pk_test_)
 * Test card: 4242 4242 4242 4242 (any future date, any CVC)
 */
object PaymentConfig {
    
    // Stripe Test Mode Publishable Key
    // Replace with your actual key from https://dashboard.stripe.com/test/apikeys
    const val STRIPE_PUBLISHABLE_KEY = "pk_test_51ScK9QRJ5zPYnHcZLzcMVTsux7D5FTqXAqW5vPU0AXqU3EolCeUdHFLCeeWCvsIRk1lCuOlXOK5craaTMGCJkl7U00U4diIy8L"
    
    // Backend URL for creating PaymentIntents
    // You need to set up a backend server for production
    const val BACKEND_URL = "https://your-backend.com/api"
    const val CREATE_PAYMENT_INTENT = "$BACKEND_URL/create-payment-intent"
    
    // Currency
    const val CURRENCY = "mxn"
    
    // Company info for receipts
    const val COMPANY_NAME = "ViewSmart"
    const val COMPANY_EMAIL = "contacto@viewsmart.mx"
    
    // Test mode flag - set to false for production
    var isTestMode = true
    
    /**
     * Initialize Stripe SDK.
     * Call this in Application.onCreate() or MainActivity.onCreate()
     */
    fun initializeStripe(context: Context) {
        PaymentConfiguration.init(context, STRIPE_PUBLISHABLE_KEY)
    }
    
    /**
     * Create PaymentSheet configuration for checkout.
     */
    fun createPaymentSheetConfig(customerName: String, customerEmail: String): PaymentSheet.Configuration {
        return PaymentSheet.Configuration(
            merchantDisplayName = COMPANY_NAME,
            customer = null, // Set customer if you have Stripe Customer ID
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = if (isTestMode) 
                    PaymentSheet.GooglePayConfiguration.Environment.Test 
                else 
                    PaymentSheet.GooglePayConfiguration.Environment.Production,
                countryCode = "MX",
                currencyCode = CURRENCY.uppercase()
            ),
            allowsDelayedPaymentMethods = false,
            billingDetailsCollectionConfiguration = PaymentSheet.BillingDetailsCollectionConfiguration(
                name = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                email = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                phone = PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Never,
                address = PaymentSheet.BillingDetailsCollectionConfiguration.AddressCollectionMode.Never
            )
        )
    }
    
    /**
     * Data class for creating a PaymentIntent request.
     * Send this to your backend server.
     */
    data class PaymentIntentRequest(
        val amount: Long, // Amount in centavos
        val currency: String = CURRENCY,
        val customerEmail: String,
        val customerName: String,
        val description: String,
        val metadata: Map<String, String> = emptyMap()
    )
    
    /**
     * Data class for PaymentIntent response from backend.
     */
    data class PaymentIntentResponse(
        val clientSecret: String,
        val paymentIntentId: String,
        val ephemeralKey: String? = null,
        val customerId: String? = null
    )
    
    /**
     * Convert price in pesos to centavos for Stripe.
     */
    fun pesosTocentavos(pesos: Double): Long {
        return (pesos * 100).toLong()
    }
    
    /**
     * Format amount for display.
     */
    fun formatAmount(centavos: Long): String {
        val pesos = centavos / 100.0
        return String.format("$%.2f MXN", pesos)
    }
}

/**
 * Payment result sealed class for handling payment outcomes.
 */
sealed class PaymentResult {
    data class Success(
        val paymentIntentId: String,
        val orderId: String
    ) : PaymentResult()
    
    data class Error(
        val message: String,
        val code: String? = null
    ) : PaymentResult()
    
    object Cancelled : PaymentResult()
    
    object Processing : PaymentResult()
}

/**
 * Helper class to manage PaymentSheet presentation.
 * 
 * Usage in your Activity/Fragment:
 * 
 * val paymentManager = StripePaymentManager(activity) { result ->
 *     when (result) {
 *         is PaymentResult.Success -> // Handle success
 *         is PaymentResult.Error -> // Handle error
 *         is PaymentResult.Cancelled -> // Handle cancellation
 *     }
 * }
 * 
 * // When ready to pay:
 * paymentManager.presentPaymentSheet(clientSecret, config)
 */
class StripePaymentManager(
    private val activity: Activity,
    private val onPaymentResult: (PaymentResult) -> Unit
) {
    private var paymentSheet: PaymentSheet? = null
    
    init {
        // Initialize Stripe
        PaymentConfig.initializeStripe(activity)
        
        // Create PaymentSheet instance
        // Note: In Compose, you would use rememberPaymentSheet() instead
    }
    
    /**
     * Present the PaymentSheet for payment collection.
     * 
     * @param clientSecret The client secret from your backend
     * @param config PaymentSheet configuration
     */
    fun presentPaymentSheet(
        clientSecret: String,
        config: PaymentSheet.Configuration
    ) {
        paymentSheet?.presentWithPaymentIntent(clientSecret, config)
    }
    
    /**
     * Handle payment result callback.
     */
    fun handlePaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                onPaymentResult(PaymentResult.Success(
                    paymentIntentId = "pi_simulated",
                    orderId = "VS-${System.currentTimeMillis()}"
                ))
            }
            is PaymentSheetResult.Canceled -> {
                onPaymentResult(PaymentResult.Cancelled)
            }
            is PaymentSheetResult.Failed -> {
                onPaymentResult(PaymentResult.Error(
                    message = result.error.localizedMessage ?: "Error en el pago",
                    code = result.error.message
                ))
            }
        }
    }
}
