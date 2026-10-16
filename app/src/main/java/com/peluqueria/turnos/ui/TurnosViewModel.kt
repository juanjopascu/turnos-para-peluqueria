package com.peluqueria.turnos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peluqueria.turnos.data.AppDatabase
import com.peluqueria.turnos.data.Turno
import com.peluqueria.turnos.utils.CalendarHelper
import com.peluqueria.turnos.utils.EmailHelper
import com.peluqueria.turnos.utils.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TurnosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val turnoDao = db.turnoDao()
    val prefs = PreferencesManager(application)

    val turnos: StateFlow<List<Turno>> = turnoDao.obtenerTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agendarTurno(
        clienteNombre: String,
        clienteEmail: String,
        clienteTelefono: String,
        servicio: String,
        fechaHoraMillis: Long,
        duracionMinutos: Int = prefs.duracionDefectoMinutos,
        notas: String = "",
        sincronizarCalendar: Boolean = true,
        enviarEmail: Boolean = true
    ) {
        viewModelScope.launch {
            var eventId: Long? = null

            // 1. Sincronizar con Google Calendar nativo
            if (sincronizarCalendar && CalendarHelper.tienePermisos(getApplication())) {
                val titulo = "Turno: $clienteNombre ($servicio)"
                val descripcion = "Peluquería: ${prefs.nombreNegocio}\nCliente: $clienteNombre\nEmail: $clienteEmail\nTeléfono: $clienteTelefono\nNotas: $notas"
                eventId = CalendarHelper.agregarTurnoACalendar(
                    getApplication(),
                    titulo,
                    descripcion,
                    fechaHoraMillis,
                    duracionMinutos
                )
            }

            // 2. Guardar en Base de Datos Local
            val nuevoTurno = Turno(
                clienteNombre = clienteNombre,
                clienteEmail = clienteEmail,
                clienteTelefono = clienteTelefono,
                servicio = servicio,
                fechaHoraMillis = fechaHoraMillis,
                duracionMinutos = duracionMinutos,
                notas = notas,
                calendarEventId = eventId
            )
            turnoDao.insertar(nuevoTurno)

            // 3. Preparar mail en Gmail con la plantilla de confirmación
            if (enviarEmail) {
                val fechaTexto = java.text.SimpleDateFormat("EEEE d 'de' MMMM 'a las' HH:mm 'hs'", java.util.Locale("es", "ES"))
                    .format(java.util.Date(fechaHoraMillis)).replaceFirstChar { it.uppercase() }
                EmailHelper.enviarConfirmacionTurno(
                    context = getApplication(),
                    emailCliente = clienteEmail,
                    nombreCliente = clienteNombre,
                    servicio = servicio,
                    fechaHoraTexto = fechaTexto,
                    duracionMinutos = duracionMinutos,
                    notas = notas,
                    nombreNegocio = prefs.nombreNegocio
                )
            }
        }
    }

    fun eliminarTurno(turno: Turno) {
        viewModelScope.launch {
            turnoDao.eliminar(turno)
        }
    }

    fun enviarAvisoGmail(turno: Turno) {
        val fechaTexto = java.text.SimpleDateFormat("EEEE d 'de' MMMM 'a las' HH:mm 'hs'", java.util.Locale("es", "ES"))
            .format(java.util.Date(turno.fechaHoraMillis)).replaceFirstChar { it.uppercase() }
        EmailHelper.enviarConfirmacionTurno(
            context = getApplication(),
            emailCliente = turno.clienteEmail,
            nombreCliente = turno.clienteNombre,
            servicio = turno.servicio,
            fechaHoraTexto = fechaTexto,
            duracionMinutos = turno.duracionMinutos,
            notas = turno.notas,
            nombreNegocio = prefs.nombreNegocio
        )
    }

    fun sincronizarEnCalendar(turno: Turno) {
        if (CalendarHelper.tienePermisos(getApplication())) {
            val titulo = "Turno: ${turno.clienteNombre} (${turno.servicio})"
            val descripcion = "Peluquería: ${prefs.nombreNegocio}\nCliente: ${turno.clienteNombre}\nEmail: ${turno.clienteEmail}\nTeléfono: ${turno.clienteTelefono}\nNotas: ${turno.notas}"
            val eventId = CalendarHelper.agregarTurnoACalendar(
                getApplication(),
                titulo,
                descripcion,
                turno.fechaHoraMillis,
                turno.duracionMinutos
            )
            if (eventId != null) {
                viewModelScope.launch {
                    turnoDao.actualizar(turno.copy(calendarEventId = eventId))
                }
            }
        }
    }
}
