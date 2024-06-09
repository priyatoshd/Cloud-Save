package com.priyatosh.cloudsave.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.priyatosh.cloudsave.domain.model.fetchDriveFiles
import kotlinx.coroutines.launch

class DriveDetailsViewModel(): ViewModel() {

    private val TAG = "DriveDetailsViewModelTAG"

    private val _files = MutableLiveData<List<File>>()
    val files: LiveData<List<File>> get() = _files

    fun fetchFiles(driveService: Drive) {
        viewModelScope.launch {
            try {
                val fetchedFiles = fetchDriveFiles(driveService)
                _files.value = fetchedFiles
            } catch (e: UserRecoverableAuthIOException) {
                Log.e(TAG, "UserRecoverableAuthIOException: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
            }
        }
    }

}