package com.peluqueria.turnos.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.peluqueria.turnos.utils.EmailHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DialogoNuevoTurno(
    datosImportados: EmailHelper.DatosExtraidosMail? = null,
    duracionDefectoMinutos: Int = 30,
    onDismiss: () -> Unit,
    onAgendar: (
        nombre: String,
        email: String,
        telefono: String,
        servicio: String,
        fechaHoraMillis: Long,
        duracionMinutos: Int,
        notas: String,
        sincronizarCalendar: Boolean,
        enviarEmail: Boolean
    ) -> Unit
) {
    var nombre by remember { mutableStateOf(datosImportados?.clienteNombre ?: "") }
    var email by remember { mutableStateOf(datosImportados?.clienteEmail ?: "") }
    var telefono by remember { mutableStateOf(datosImportados?.clienteTelefono ?: "") }
    var servicio by remember { mutableStateOf(datosImportados?.servicio.takeIf { !it.isNullOfEmpty() } ?: "Corte de Pelo") }
    var notas by remember { mutableStateOf(datosImportados?.textoCompleto?.take(150) ?: "") }
    var duracionText by remember { mutableStateOf(duracionDefectoMinutos.toString()) }

    var sincronizarCalendar by remember { mutableStateOf(true) }
    var enviarEmail by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) } }

    var fechaHoraMillis by remember { mutableStateOf(calendar.timeInMillis) }
    val formatterFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formatterHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var fechaTexto by remember { mutableStateOf(formatterFecha.format(Date(fechaHoraMillis))) }
    var horaTexto by remember { mutableStateOf(formatterHora.format(Date(fechaHoraMillis))) }

    val serviciosRapidos = listOf("Corte de Pelo", "Corte y Barba", "Coloración", "Peinado / Lavado", "Alisado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Nuevo Turno", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (datosImportados != null && datosImportados.textoCompleto.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📩 Datos extraídos automáticamente del mail",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Cliente *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email para Confirmación") },
                    singleLine = true,
                    placeholder = { Text("ejemplo@gmail.com") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono de Contacto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Servicio", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    serviciosRapidos.forEach { s ->
                        FilterChip(
                            selected = (servicio == s),
                            onClick = { servicio = s },
                            label = { Text(s) }
                        )
                    }
                }

                OutlinedTextField(
                    value = servicio,
                    onValueChange = { servicio = it },
                    label = { Text("Otro Servicio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selección de Fecha y Hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = fechaHoraMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    c.set(Calendar.YEAR, year)
                                    c.set(Calendar.MONTH, month)
                                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    fechaHoraMillis = c.timeInMillis
                                    fechaTexto = formatterFecha.format(c.time)
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(fechaTexto)
                    }

                    Button(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = fechaHoraMillis }
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    c.set(Calendar.MINUTE, minute)
                                    fechaHoraMillis = c.timeInMillis
                                    horaTexto = formatterHora.format(c.time)
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(horaTexto)
                    }
                }

                OutlinedTextField(
                    value = duracionText,
                    onValueChange = { duracionText = it },
                    label = { Text("Duración (minutos)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas o Comentarios") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = sincronizarCalendar,
                        onCheckedChange = { sincronizarCalendar = it }
                    )
                    Text("Sincronizar con Google Calendar")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = enviarEmail,
                        onCheckedChange = { enviarEmail = it }
                    )
                    Text("Abrir Gmail para enviar confirmación")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        val duracionInt = duracionText.toIntOrNull() ?: duracionDefectoMinutos
                        onAgendar(
                            nombre,
                            email,
                            telefono,
                            servicio,
                            fechaHoraMillis,
                            duracionInt,
                            notas,
                            sincronizarCalendar,
                            enviarEmail
                        )
                    }
                }
            ) {
                Text("Agendar Turno")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun String?.isNullOfEmpty(): Boolean {
    return this == null || this.isBlank()
}
