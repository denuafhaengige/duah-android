package dk.denuafhaengige.android.util

import io.github.g00fy2.versioncompare.Version

fun Version.stringValue(): String {
    return "$major.$minor.$patch$suffix"
}