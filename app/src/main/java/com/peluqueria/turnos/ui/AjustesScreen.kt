package com.peluqueria.turnos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.peluqueria.turnos.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    prefs: PreferencesManager,
    cuentaGoogle: GoogleSignInAccount? = null,
    onConectarGoogle: () -> Unit = {},
    onDesconectarGoogle: () -> Unit = {},
    onVolver: () -> Unit
) {
    var nombreNegocio by remember { mutableStateOf(prefs.nombreNegocio) }
    var duracionDefecto by remember { mutableStateOf(prefs.duracionDefectoMinutos.toString()) }
    var enviarMailAuto by remember { mutableStateOf(prefs.enviarMailAutomatico) }
    var agregarCalendarAuto by remember { mutableStateOf(prefs.agregarACalendarAuto) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de la Peluquería") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cuenta de Google (Drive, Calendar, Gmail)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (cuentaGoogle != null) "Cuenta Vinculada" else "Sin vincular",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cuentaGoogle?.email ?: "Conecta para autorizar Drive, Calendar y Gmail",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (cuentaGoogle == null) {
                        Button(onClick = onConectarGoogle) {
                            Text("Conectar")
                        }
                    } else {
                        OutlinedButton(onClick = onDesconectarGoogle) {
                            Text("Salir")
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Datos de tu Negocio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nombreNegocio,
                onValueChange = { nombreNegocio = it },
                label = { Text("Nombre de la Peluquería / Barbería") },
                placeholder = { Text("Ej: Peluquería Estilo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duracionDefecto,
                onValueChange = { duracionDefecto = it },
                label = { Text("Duración por defecto de turnos (minutos)") },
                placeholder = { Text("30") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Opciones de Sincronización",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sincronizar con Google Calendar", fontWeight = FontWeight.Bold)
                    Text("Agrega automáticamente cada turno al calendario del teléfono", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = agregarCalendarAuto,
                    onCheckedChange = { agregarCalendarAuto = it }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Avisos por Gmail", fontWeight = FontWeight.Bold)
                    Text("Abre Gmail listo para enviar la confirmación al agendar", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = enviarMailAuto,
                    onCheckedChange = { enviarMailAuto = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    prefs.nombreNegocio = nombreNegocio.ifBlank { "Peluquería Estilo" }
                    prefs.duracionDefectoMinutos = duracionDefecto.toIntOrNull() ?: 30
                    prefs.enviarMailAutomatico = enviarMailAuto
                    prefs.agregarACalendarAuto = agregarCalendarAuto
                    onVolver()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        }
    }
}
