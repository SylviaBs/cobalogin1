package com.learn.cobalogin1.presentation.sign_in

data class SignInState (
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)