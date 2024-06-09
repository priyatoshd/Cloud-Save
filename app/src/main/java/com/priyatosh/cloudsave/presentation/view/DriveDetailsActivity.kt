package com.priyatosh.cloudsave.presentation.view

import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.priyatosh.cloudsave.R
import com.priyatosh.cloudsave.domain.model.GoogleIdTokenCredentialApp
import com.priyatosh.cloudsave.presentation.viewmodel.DriveDetailsViewModel
import com.priyatosh.cloudsave.ui.theme.CloudSaveTheme


class DriveDetailsActivity : ComponentActivity() {

    private val TAG = "DriveDetailsActivityTAG"

    private lateinit var viewModel: DriveDetailsViewModel

    private lateinit var authorizationResult: AuthorizationResult
    private lateinit var googleIdTokenCredential: GoogleIdTokenCredentialApp

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DriveDetailsViewModel::class.java]

        authorizationResult = intent.getParcelableExtra("AUTH_RESULT")!!
        googleIdTokenCredential = intent.getParcelableExtra("SIGN_IN_RESULT")!!

        val name = googleIdTokenCredential.displayName ?: "Guest"

        Log.d(TAG, "AuthResult access Token: ${authorizationResult.accessToken}")

        viewModel.fetchFiles(createDriveService())

        setContent {
            val files by viewModel.files.observeAsState(initial = emptyList())

            CloudSaveTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surface),
                            title = { Text(text = "Welcome! $name") },
                        )},
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { uploadAFile() },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_upload_file),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                        ) {
                            items(files) { file ->
                                FileCard(file)
                                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun uploadAFile() {

    }

    private fun createDriveService(): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(DriveScopes.DRIVE)
        )
        // Retrieve the account name and set it in the credential.
        // Ensure you have already signed in and have the necessary account information.
        credential.selectedAccountName = googleIdTokenCredential.id

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Cloud Save")
            .build()
    }



}

@Composable
fun FileCard(file: File) {
    Card(
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = file.name, fontSize = 16.sp, softWrap = true)
        }

    }
}

