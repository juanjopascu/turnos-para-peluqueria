package com.peluqueria.turnos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.peluqueria.turnos.data.Turno
import com.peluqueria.turnos.ui.theme.PeluqueriaTheme
import com.peluqueria.turnos.utils.CalendarHelper
import com.peluqueria.turnos.utils.EmailHelper
import com.peluqueria.turnos.utils.GoogleAuthHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurnosScreen(
    turnos: List<Turno>,
    nombreNegocio: String,
    datosImportadosMail: EmailHelper.DatosExtraidosMail? = null,
    cuentaGoogle: GoogleSignInAccount? = null,
    onConectarGoogle: () -> Unit = {},
    onDesconectarGoogle: () -> Unit = {},
    onRequestCalendarPermissions: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onAgendarTurno: (
        nombre: String,
        email: String,
        telefono: String,
        servicio: String,
        fechaHoraMillis: Long,
        duracionMinutos: Int,
        notas: String,
        sincronizarCalendar: Boolean,
        enviarEmail: Boolean
    ) -> Unit,
    onEliminarTurno: (Turno) -> Unit,
    onEnviarAvisoGmail: (Turno) -> Unit,
    onSincronizarCalendar: (Turno) -> Unit,
    onLimpiarDatosImportados: () -> Unit
) {
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tienePermisoCalendar = remember { CalendarHelper.tienePermisos(context) }

    // Abrir automáticamente el diálogo si viene un mail compartido
    LaunchedEffect(datosImportadosMail) {
        if (datosImportadosMail != null) {
            mostrarDialogoNuevo = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nombreNegocio, fontWeight = FontWeight.Bold)
                        Text(
                            "Agenda y Sincronización de Turnos",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAbrirAjustes) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoNuevo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agendar Turno")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Tarjeta de Estado e Integración con Google (Drive, Calendar, Gmail)
            TarjetaEstadoGoogle(
                cuentaGoogle = cuentaGoogle,
                onConectarGoogle = onConectarGoogle,
                onDesconectarGoogle = onDesconectarGoogle
            )

            // Banner de Alerta de Permisos de Calendario Nativo si no están concedidos
            if (!tienePermisoCalendar) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Permiso de Calendario Requerido",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Para guardar turnos en Google Calendar, otorga el permiso.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        TextButton(onClick = onRequestCalendarPermissions) {
                            Text("ACTIVAR")
                        }
                    }
                }
            }

            // Banner si viene un mail recibido
            if (datosImportadosMail != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "📩 Correo de Turno Recibido",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Se han extraído datos para agendar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(
                            onClick = { mostrarDialogoNuevo = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("AGENDAR")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Turnos Agendados (${turnos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (turnos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay turnos agendados todavía.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Toca el botón + para agregar uno o comparte un mail desde Gmail.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(turnos, key = { it.id }) { turno ->
                        ItemTurnoCard(
                            turno = turno,
                            onEnviarAvisoGmail = { onEnviarAvisoGmail(turno) },
                            onSincronizarCalendar = { onSincronizarCalendar(turno) },
                            onEliminar = { onEliminarTurno(turno) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoNuevo) {
        DialogoNuevoTurno(
            datosImportados = datosImportadosMail,
            onDismiss = {
                mostrarDialogoNuevo = false
                onLimpiarDatosImportados()
            },
            onAgendar = { nom, mail, tel, serv, fechaMillis, dur, not, syncCal, sendMail ->
                onAgendarTurno(nom, mail, tel, serv, fechaMillis, dur, not, syncCal, sendMail)
                mostrarDialogoNuevo = false
                onLimpiarDatosImportados()
            }
        )
    }
}

@Composable
fun TarjetaEstadoGoogle(
    cuentaGoogle: GoogleSignInAccount?,
    onConectarGoogle: () -> Unit,
    onDesconectarGoogle: () -> Unit
) {
    val context = LocalContext.current
    val tieneDrive = GoogleAuthHelper.tienePermisoDrive(context)
    val tieneCalendar = GoogleAuthHelper.tienePermisoCalendar(context)
    val tieneGmail = GoogleAuthHelper.tienePermisoGmail(context)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (cuentaGoogle != null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (cuentaGoogle != null) "Google Conectado" else "Vincular Google (Drive, Calendar, Gmail)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = cuentaGoogle?.email ?: "Toca conectar para autorizar permisos de Google",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (cuentaGoogle == null) {
                    Button(
                        onClick = onConectarGoogle,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("CONECTAR", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(onClick = onDesconectarGoogle) {
                        Text("SALIR", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chips con estado de permisos
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = onConectarGoogle,
                    label = { Text("Google Drive", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            if (cuentaGoogle != null && tieneDrive) Icons.Default.CheckCircle else Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (cuentaGoogle != null && tieneDrive) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                )
                AssistChip(
                    onClick = onConectarGoogle,
                    label = { Text("Calendar", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            if (cuentaGoogle != null && tieneCalendar) Icons.Default.CheckCircle else Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (cuentaGoogle != null && tieneCalendar) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                )
                AssistChip(
                    onClick = onConectarGoogle,
                    label = { Text("Gmail", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            if (cuentaGoogle != null && tieneGmail) Icons.Default.CheckCircle else Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (cuentaGoogle != null && tieneGmail) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ItemTurnoCard(
    turno: Turno,
    onEnviarAvisoGmail: () -> Unit,
    onSincronizarCalendar: () -> Unit,
    onEliminar: () -> Unit
) {
    val formatterFecha = remember { SimpleDateFormat("EEEE d 'de' MMMM, HH:mm", Locale("es", "ES")) }
    val fechaFormateada = remember(turno.fechaHoraMillis) {
        formatterFecha.format(Date(turno.fechaHoraMillis)).replaceFirstChar { it.uppercase() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = turno.clienteNombre,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Servicio: ${turno.servicio}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (turno.clienteEmail.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = turno.clienteEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (turno.clienteTelefono.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = turno.clienteTelefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado del Calendar
                AssistChip(
                    onClick = onSincronizarCalendar,
                    label = {
                        Text(
                            if (turno.calendarEventId != null) "En Google Calendar" else "Sincronizar Calendar",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (turno.calendarEventId != null) Icons.Default.CheckCircle else Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (turno.calendarEventId != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                )

                // Botón Aviso por Mail
                Button(
                    onClick = onEnviarAvisoGmail,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aviso Gmail", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TurnosScreenPreview() {
    PeluqueriaTheme {
        TurnosScreen(
            turnos = listOf(
                Turno(
                    id = 1,
                    clienteNombre = "Carlos Gómez",
                    clienteEmail = "carlos@example.com",
                    clienteTelefono = "1122334455",
                    servicio = "Corte y Barba",
                    fechaHoraMillis = System.currentTimeMillis() + 3600000,
                    calendarEventId = 123L
                )
            ),
            nombreNegocio = "Peluquería Estilo",
            onRequestCalendarPermissions = {},
            onAbrirAjustes = {},
            onAgendarTurno = { _, _, _, _, _, _, _, _, _ -> },
            onEliminarTurno = {},
            onEnviarAvisoGmail = {},
            onSincronizarCalendar = {},
            onLimpiarDatosImportados = {}
        )
    }
}
