package com.priyatosh.cloudsave.presentation.view

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.services.drive.DriveScopes
import com.priyatosh.cloudsave.R
import com.priyatosh.cloudsave.data.AppUtil
import com.priyatosh.cloudsave.domain.model.GoogleIdTokenCredentialApp
import com.priyatosh.cloudsave.ui.theme.CloudSaveTheme
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID


class MainActivity : ComponentActivity() {

    private lateinit var googleIdTokenCredential: GoogleIdTokenCredentialApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudSaveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        GoogleSignInButton()
                    }
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_AUTHORIZE) {
            val authorizationResult = Identity.getAuthorizationClient(
                this
            ).getAuthorizationResultFromIntent(data)
            navigateToDriveActivity(authorizationResult)
            Toast.makeText(this, "Authorization Result",Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToDriveActivity(authorizationResult: AuthorizationResult) {
        val intent = Intent(this, DriveDetailsActivity::class.java)
        intent.putExtra("AUTH_RESULT", authorizationResult)
        intent.putExtra("SIGN_IN_RESULT", googleIdTokenCredential)
        startActivity(intent)
    }


    @Composable
    fun GoogleSignInButton() {
        val coroutineScope = rememberCoroutineScope()

        val credentialManager = CredentialManager.create(this)

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(stringResource(id = R.string.web_client_id))
            .setNonce(getHashedNonce())
            .build()


        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val onClick: () -> Unit = {
            Log.d(TAG, "OnCLick Called")
            coroutineScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@MainActivity,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Exception in the Call: ${e.message}")
                }
            }
        } 
        
        Button(onClick = onClick) {
            Text(text = "Sign In with Google")
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data).let {
                                GoogleIdTokenCredentialApp(
                                    id = it.id,
                                    idToken = it.idToken,
                                    displayName = it.displayName,
                                    givenName = it.givenName,
                                    familyName = it.displayName,
                                    profilePictureUri = it.profilePictureUri,
                                    phoneNumber = it.phoneNumber,
                                    data = credential.data,
                                    type = it.type,
                                )
                            }
                        authoriseAndHandleNavigation()

                        Log.d(TAG, "Token: ${googleIdTokenCredential.idToken}")
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                }
                else {
                    // Catch any unrecognized credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun authoriseAndHandleNavigation() {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(mutableListOf(Scope(DriveScopes.DRIVE)))
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult: AuthorizationResult ->
                if (authorizationResult.hasResolution()) {
                    // Access needs to be granted by the user
                    val pendingIntent = authorizationResult.pendingIntent
                    try {
                        startIntentSenderForResult(
                            pendingIntent!!.intentSender,
                            REQUEST_AUTHORIZE, null, 0, 0, 0, null
                        )
                    } catch (e: SendIntentException) {
                        Log.e(
                            TAG,
                            "Couldn't start Authorization UI: " + e.localizedMessage
                        )
                    }
                } else {
                    // Access already granted, continue with user action
                    navigateToDriveActivity(authorizationResult)
                    Toast.makeText(this, "Already Granted",Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(this, "Failed Authorize: ${e?.message}",Toast.LENGTH_LONG).show()
                Log.e(
                    TAG,
                    "Failed to authorize",
                    e
                )
            }
    }


    private fun getHashedNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    companion object {
        const val REQUEST_AUTHORIZE = 100
        const val TAG = "MainActivityTAG"
    }

}
