package com.example.smartview

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartview.auth.*
import com.example.smartview.shop.*
import com.example.smartview.ui.theme.SmartViewTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private val firebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var isLoading by remember { mutableStateOf(false) }
                    var errorMessage by remember { mutableStateOf<String?>(null) }

                    // Check if user is already signed in
                    val startDestination = if (firebaseAuth.currentUser != null) "main" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        // Login Screen
                        composable("login") {
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            isLoading = true
                                            try {
                                                val signInResult = googleAuthUiClient.signInWithIntent(
                                                    intent = result.data ?: return@launch
                                                )
                                                if (signInResult.data != null) {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "Inicio de sesión exitoso",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.navigate("main") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                } else {
                                                    errorMessage = signInResult.errorMessage
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = e.message
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            )

                            LoginScreen(
                                onLoginClick = { email, password ->
                                    lifecycleScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        try {
                                            firebaseAuth.signInWithEmailAndPassword(email, password).await()
                                            Toast.makeText(
                                                applicationContext,
                                                "Inicio de sesión exitoso",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error: ${e.localizedMessage}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                onGoogleSignInClick = {
                                    lifecycleScope.launch {
                                        isLoading = true
                                        try {
                                            val signInIntentSender = googleAuthUiClient.signIn()
                                            launcher.launch(
                                                IntentSenderRequest.Builder(
                                                    signInIntentSender ?: return@launch
                                                ).build()
                                            )
                                        } catch (e: Exception) {
                                            errorMessage = e.message
                                            isLoading = false
                                        }
                                    }
                                },
                                onRegisterClick = {
                                    navController.navigate("register")
                                },
                                isLoading = isLoading,
                                errorMessage = errorMessage
                            )
                        }

                        // Register Screen
                        composable("register") {
                            RegisterScreen(
                                onRegisterClick = { name, email, password ->
                                    lifecycleScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        try {
                                            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                                            // Update profile with name
                                            result.user?.updateProfile(
                                                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name)
                                                    .build()
                                            )?.await()
                                            
                                            Toast.makeText(
                                                applicationContext,
                                                "Cuenta creada exitosamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error: ${e.localizedMessage}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                onLoginClick = {
                                    navController.popBackStack()
                                },
                                isLoading = isLoading,
                                errorMessage = errorMessage
                            )
                        }

                        // Main Screen with Drawer
                        composable("main") {
                            val currentUser = firebaseAuth.currentUser
                            val userData = currentUser?.let {
                                UserData(
                                    userId = it.uid,
                                    username = it.displayName ?: it.email?.substringBefore("@"),
                                    profilePictureUrl = it.photoUrl?.toString()
                                )
                            }

                            MainScreen(
                                userData = userData,
                                onSignOut = {
                                    lifecycleScope.launch {
                                        firebaseAuth.signOut()
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Sesión cerrada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToProducts = {
                                    navController.navigate("products")
                                },
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onNavigateToContact = {
                                    navController.navigate("contact")
                                },
                                onNavigateToShop = {
                                    navController.navigate("shop")
                                }
                            )
                        }

                        // Products Screen
                        composable("products") {
                            ProductsScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Contact Screen
                        composable("contact") {
                            ContactScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Profile Screen
                        composable("profile") {
                            val currentUser = firebaseAuth.currentUser
                            val userData = currentUser?.let {
                                UserData(
                                    userId = it.uid,
                                    username = it.displayName ?: it.email?.substringBefore("@"),
                                    profilePictureUrl = it.photoUrl?.toString()
                                )
                            }

                            ProfileScreen(
                                userData = userData,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onSignOut = {
                                    lifecycleScope.launch {
                                        firebaseAuth.signOut()
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Sesión cerrada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onSwitchAccount = {
                                    lifecycleScope.launch {
                                        firebaseAuth.signOut()
                                        googleAuthUiClient.signOut()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        // Shop Screen
                        composable("shop") {
                            var selectedProduct by remember { mutableStateOf<Product?>(null) }
                            var selectedPack by remember { mutableStateOf<ProductPack?>(null) }
                            
                            if (selectedProduct != null || selectedPack != null) {
                                CheckoutScreen(
                                    product = selectedProduct,
                                    pack = selectedPack,
                                    onBackClick = {
                                        selectedProduct = null
                                        selectedPack = null
                                    },
                                    onPaymentComplete = {
                                        selectedProduct = null
                                        selectedPack = null
                                        navController.navigate("main") {
                                            popUpTo("shop") { inclusive = true }
                                        }
                                        Toast.makeText(
                                            applicationContext,
                                            "¡Gracias por tu compra!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            } else {
                                ShopScreen(
                                    onBackClick = {
                                        navController.popBackStack()
                                    },
                                    onProductSelect = { product ->
                                        selectedProduct = product
                                    },
                                    onPackSelect = { pack ->
                                        selectedPack = pack
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
