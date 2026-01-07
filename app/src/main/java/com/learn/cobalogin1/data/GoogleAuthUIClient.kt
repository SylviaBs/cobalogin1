package com.learn.cobalogin1.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.learn.cobalogin1.R
import kotlinx.coroutines.tasks.await

class GoogleAuthUIClient (
    private val context: Context
){
    private val auth = FirebaseAuth.getInstance()

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): SignInResult{
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            return handleSignIn(result)

        }catch (e: GetCredentialCancellationException){
            return SignInResult(data = null, errorMessage = "Dibatalkan")
        }catch (e: Exception){
            e.printStackTrace()
            return SignInResult(data = null, errorMessage = e.message)
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): SignInResult {
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
            try {
                val GoogleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val googleIdToken = GoogleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                val user = auth.signInWithCredential(firebaseCredential).await().user

                return SignInResult(
                    data = user?.run {
                        UserData(
                            userId = uid,
                            username = displayName,
                            profilePictureUrl = photoUrl?.toString()
                        )
                    },
                    errorMessage = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return SignInResult(data = null, errorMessage = e.message)
            }
        return SignInResult(data = null, errorMessage = "No Google Credential Found")
    }

    suspend fun signOut(){
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            auth.signOut()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getSignInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}