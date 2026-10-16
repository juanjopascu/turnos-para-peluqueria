package com.peluqueria.turnos.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("peluqueria_prefs", Context.MODE_PRIVATE)

    var nombreNegocio: String
        get() = prefs.getString("KEY_NOMBRE_NEGOCIO", "Peluquería Estilo") ?: "Peluquería Estilo"
        set(value) = prefs.edit().putString("KEY_NOMBRE_NEGOCIO", value).apply()

    var duracionDefectoMinutos: Int
        get() = prefs.getInt("KEY_DURACION_DEFECTO", 30)
        set(value) = prefs.edit().putInt("KEY_DURACION_DEFECTO", value).apply()

    var enviarMailAutomatico: Boolean
        get() = prefs.getBoolean("KEY_ENVIAR_MAIL_AUTO", true)
        set(value) = prefs.edit().putBoolean("KEY_ENVIAR_MAIL_AUTO", value).apply()

    var agregarACalendarAuto: Boolean
        get() = prefs.getBoolean("KEY_AGREGAR_CALENDAR_AUTO", true)
        set(value) = prefs.edit().putBoolean("KEY_AGREGAR_CALENDAR_AUTO", value).apply()
}
