package com.learn.cobalogin1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.learn.cobalogin1.data.GoogleAuthUIClient
import com.learn.cobalogin1.presentation.profile.ProfileScreen
import com.learn.cobalogin1.presentation.sign_in.SignInScreen
import com.learn.cobalogin1.presentation.sign_in.SignInViewModel
import com.learn.cobalogin1.ui.theme.Cobalogin1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val GoogleAuthUIClient by lazy {
        GoogleAuthUIClient(
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cobalogin1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "sign_in"){

                        composable("sign_in"){
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if(GoogleAuthUIClient.getSignInUser() != null){
                                    navController.navigate("profile")
                                }
                            }

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if(state.isSignInSuccessful){
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign In Successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("Profile")
                                    viewModel.resetState()
                                }
                            }
                            SignInScreen(
                                state = state,
                                onSignInClick =  {
                                    lifecycleScope.launch {
                                        val result = GoogleAuthUIClient.signIn()
                                        viewModel.onSignInResult(result)
                                    }
                                }
                            )
                        }
                        composable("profile"){
                            ProfileScreen(
                                userData = GoogleAuthUIClient.getSignInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        GoogleAuthUIClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Sign Out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

