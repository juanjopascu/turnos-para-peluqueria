package com.peluqueria.turnos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.peluqueria.turnos.data.Turno
import com.peluqueria.turnos.ui.*
import com.peluqueria.turnos.ui.theme.PeluqueriaTheme
import com.peluqueria.turnos.utils.EmailHelper
import com.peluqueria.turnos.utils.GoogleAuthHelper

class MainActivity : ComponentActivity() {

    private val viewModel: TurnosViewModel by viewModels()
    private var datosImportadosMail by mutableStateOf<EmailHelper.DatosExtraidosMail?>(null)
    private var cuentaGoogleState by mutableStateOf<GoogleSignInAccount?>(null)

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permisos procesados
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            cuentaGoogleState = task.getResult(ApiException::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar sesión actual de Google
        cuentaGoogleState = GoogleAuthHelper.obtenerCuentaActual(this)

        // Solicitar permisos de calendario al iniciar
        calendarPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
            )
        )

        // Procesar si se abrió compartiendo un texto/email
        procesarIntent(intent)

        setContent {
            PeluqueriaTheme {
                val turnos by viewModel.turnos.collectAsState()
                var pantallaActual by remember { mutableStateOf("turnos") }

                when (pantallaActual) {
                    "turnos" -> TurnosScreen(
                        turnos = turnos,
                        nombreNegocio = viewModel.prefs.nombreNegocio,
                        datosImportadosMail = datosImportadosMail,
                        cuentaGoogle = cuentaGoogleState,
                        onConectarGoogle = { iniciarSesionGoogle() },
                        onDesconectarGoogle = { cerrarSesionGoogle() },
                        onRequestCalendarPermissions = {
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_CALENDAR,
                                    android.Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        },
                        onAbrirAjustes = { pantallaActual = "ajustes" },
                        onAgendarTurno = { nom: String, mail: String, tel: String, serv: String, fechaMillis: Long, dur: Int, not: String, syncCal: Boolean, sendMail: Boolean ->
                            viewModel.agendarTurno(
                                clienteNombre = nom,
                                clienteEmail = mail,
                                clienteTelefono = tel,
                                servicio = serv,
                                fechaHoraMillis = fechaMillis,
                                duracionMinutos = dur,
                                notas = not,
                                sincronizarCalendar = syncCal,
                                enviarEmail = sendMail
                            )
                        },
                        onEliminarTurno = { turno: Turno ->
                            viewModel.eliminarTurno(turno)
                        },
                        onEnviarAvisoGmail = { turno: Turno ->
                            viewModel.enviarAvisoGmail(turno)
                        },
                        onSincronizarCalendar = { turno: Turno ->
                            viewModel.sincronizarEnCalendar(turno)
                        },
                        onLimpiarDatosImportados = {
                            datosImportadosMail = null
                        }
                    )

                    "ajustes" -> AjustesScreen(
                        prefs = viewModel.prefs,
                        cuentaGoogle = cuentaGoogleState,
                        onConectarGoogle = { iniciarSesionGoogle() },
                        onDesconectarGoogle = { cerrarSesionGoogle() },
                        onVolver = { pantallaActual = "turnos" }
                    )
                }
            }
        }
    }

    private fun iniciarSesionGoogle() {
        val client = GoogleAuthHelper.getGoogleSignInClient(this)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun cerrarSesionGoogle() {
        val client = GoogleAuthHelper.getGoogleSignInClient(this)
        client.signOut().addOnCompleteListener {
            cuentaGoogleState = null
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { procesarIntent(it) }
    }

    private fun procesarIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val textoCompartido = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
            if (textoCompartido.isNotBlank()) {
                datosImportadosMail = EmailHelper.extraerDatosDeTextoCompartido(textoCompartido)
            }
        }
    }
}
