package com.peluqueria.turnos.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

object GoogleAuthHelper {

    val SCOPE_DRIVE = Scope("https://www.googleapis.com/auth/drive.file")
    val SCOPE_CALENDAR = Scope("https://www.googleapis.com/auth/calendar")
    val SCOPE_GMAIL = Scope("https://www.googleapis.com/auth/gmail.send")

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(SCOPE_DRIVE, SCOPE_CALENDAR, SCOPE_GMAIL)
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun obtenerCuentaActual(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun tienePermisoDrive(context: Context): Boolean {
        val account = obtenerCuentaActual(context) ?: return false
        return GoogleSignIn.hasPermissions(account, SCOPE_DRIVE)
    }

    fun tienePermisoCalendar(context: Context): Boolean {
        val account = obtenerCuentaActual(context) ?: return false
        return GoogleSignIn.hasPermissions(account, SCOPE_CALENDAR)
    }

    fun tienePermisoGmail(context: Context): Boolean {
        val account = obtenerCuentaActual(context) ?: return false
        return GoogleSignIn.hasPermissions(account, SCOPE_GMAIL)
    }

    fun tieneTodosLosPermisos(context: Context): Boolean {
        return tienePermisoDrive(context) && tienePermisoCalendar(context) && tienePermisoGmail(context)
    }
}
