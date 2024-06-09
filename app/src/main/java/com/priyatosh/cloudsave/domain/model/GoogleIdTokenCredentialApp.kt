package com.priyatosh.cloudsave.domain.model

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoogleIdTokenCredentialApp(
    val idToken: String? = null,
    val id: String? = null,
    val displayName: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    val profilePictureUri: Uri? = null,
    val phoneNumber: String? = null,
    val type: String,
    val data: Bundle,
): Parcelable