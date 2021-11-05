package dk.denuafhaengige.android.models

import android.net.Uri

interface Identifiable {
    val id: Int
}

interface Titled {
    val title: String
}

interface MetaTitled {
    val metaTitle: String?
    val metaTitleSupplement: String?
}

interface Described {
    val description: String?
}

interface Imaged {
    val wideImageUri: Uri?
    val squareImageUri: Uri?
}
