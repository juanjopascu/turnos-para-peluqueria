package com.peluqueria.turnos.utils

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.TimeZone

object CalendarHelper {

    fun tienePermisos(context: Context): Boolean {
        val readPerm = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val writePerm = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        return readPerm && writePerm
    }

    /**
     * Obtiene el ID del calendario predeterminado (por ejemplo, Google Calendar principal).
     */
    private fun obtenerCalendarIdPredeterminado(context: Context): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY
        )
        try {
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            cursor?.use {
                val idCol = it.getColumnIndex(CalendarContract.Calendars._ID)
                val primaryCol = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val isPrimary = if (primaryCol >= 0) it.getInt(primaryCol) == 1 else false
                    if (isPrimary) return id
                }
                // Si no se encuentra uno primario explicito, usar el primero disponible
                if (it.moveToFirst()) {
                    return it.getLong(idCol)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 1L // Fallback a 1
    }

    /**
     * Agrega un turno directamente al calendario de Google/Android del dispositivo.
     */
    fun agregarTurnoACalendar(
        context: Context,
        titulo: String,
        descripcion: String,
        inicioMillis: Long,
        duracionMinutos: Int
    ): Long? {
        if (!tienePermisos(context)) return null

        val finMillis = inicioMillis + (duracionMinutos * 60 * 1000)
        val calendarId = obtenerCalendarIdPredeterminado(context)

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, inicioMillis)
            put(CalendarContract.Events.DTEND, finMillis)
            put(CalendarContract.Events.TITLE, titulo)
            put(CalendarContract.Events.DESCRIPTION, descripcion)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
