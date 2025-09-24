package com.jotadev.aiapaec.ui.theme

import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jotadev.aiapaec.R

val robotoProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val Roboto = FontFamily(
    Font(GoogleFont("Roboto"), robotoProvider, FontWeight.Normal),
    Font(GoogleFont("Roboto"), robotoProvider, FontWeight.Medium),
    Font(GoogleFont("Roboto"), robotoProvider, FontWeight.Bold)
)