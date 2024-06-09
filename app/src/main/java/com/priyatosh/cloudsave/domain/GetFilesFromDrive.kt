package com.priyatosh.cloudsave.domain.model

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchDriveFiles(driveService: Drive): List<com.google.api.services.drive.model.File> {
    return withContext(Dispatchers.IO) {
        val result: FileList = driveService.files().list()
            .execute()
        result.files ?: emptyList()
    }
}