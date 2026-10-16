package com.peluqueria.turnos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object EmailHelper {

    /**
     * Genera el asunto y el cuerpo con la plantilla formateada de confirmación del turno.
     */
    fun armarPlantillaConfirmacion(
        nombreCliente: String,
        servicio: String,
        fechaHoraTexto: String,
        duracionMinutos: Int,
        notas: String,
        nombreNegocio: String
    ): Pair<String, String> {
        val asunto = "✂️ Confirmación de Turno - $nombreNegocio"
        val cuerpo = """
            Hola $nombreCliente,

            ¡Tu turno en $nombreNegocio ha sido agendado y confirmado con éxito! 🎉

            📋 DETALLES DEL TURNO:
            --------------------------------------------------
            💈 Negocio: $nombreNegocio
            💇 Servicio: $servicio
            📅 Fecha y Hora: $fechaHoraTexto
            ⏱️ Duración estimada: $duracionMinutos minutos
            ${if (notas.isNotBlank()) "📝 Notas: $notas\n" else ""}--------------------------------------------------

            📍 Te esperamos en el local. Por favor preséntate unos minutos antes.
            Si necesitas cancelar o reprogramar tu turno, avísanos respondiendo a este mail.

            ¡Muchas gracias por elegirnos!
            $nombreNegocio
        """.trimIndent()

        return Pair(asunto, cuerpo)
    }

    /**
     * Abre Gmail o la app de correo predeterminada con la plantilla pre-completada lista para enviar.
     */
    fun enviarConfirmacionTurno(
        context: Context,
        emailCliente: String,
        nombreCliente: String,
        servicio: String,
        fechaHoraTexto: String,
        duracionMinutos: Int = 30,
        notas: String = "",
        nombreNegocio: String
    ) {
        val (asunto, cuerpo) = armarPlantillaConfirmacion(
            nombreCliente = nombreCliente,
            servicio = servicio,
            fechaHoraTexto = fechaHoraTexto,
            duracionMinutos = duracionMinutos,
            notas = notas,
            nombreNegocio = nombreNegocio
        )

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${if (emailCliente.isNotBlank()) emailCliente else ""}")
            if (emailCliente.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailCliente))
            }
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Extrae datos útiles (Nombre, Mail, Servicio, Teléfono) cuando se recibe un texto compartido desde Gmail.
     */
    data class DatosExtraidosMail(
        val clienteNombre: String = "",
        val clienteEmail: String = "",
        val clienteTelefono: String = "",
        val servicio: String = "",
        val textoCompleto: String = ""
    )

    fun extraerDatosDeTextoCompartido(texto: String): DatosExtraidosMail {
        var email = ""
        var telefono = ""
        var nombre = ""
        var servicio = "Corte de Pelo"

        // Buscar email con expresiones regulares
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        val emailMatch = emailRegex.find(texto)
        if (emailMatch != null) {
            email = emailMatch.value
        }

        // Buscar teléfono de 8 a 15 dígitos
        val phoneRegex = Regex("\\+?[0-9]{8,15}")
        val phoneMatch = phoneRegex.find(texto)
        if (phoneMatch != null) {
            telefono = phoneMatch.value
        }

        // Buscar servicio si menciona palabras clave
        val textoLower = texto.lowercase()
        when {
            textoLower.contains("barba") -> servicio = "Corte y Barba"
            textoLower.contains("color") || textoLower.contains("tinta") -> servicio = "Coloración"
            textoLower.contains("peinado") || textoLower.contains("lavado") -> servicio = "Peinado / Lavado"
            textoLower.contains("corte") -> servicio = "Corte de Pelo"
        }

        // Intento simple de extraer nombre si hay una línea tipo "De: Nombre" o "Cliente: Nombre"
        val lineas = texto.lines()
        for (linea in lineas) {
            if (linea.contains("De:", ignoreCase = true) || linea.contains("Nombre:", ignoreCase = true)) {
                val partes = linea.split(":")
                if (partes.size > 1) {
                    nombre = partes[1].trim().take(30)
                    break
                }
            }
        }

        return DatosExtraidosMail(
            clienteNombre = nombre,
            clienteEmail = email,
            clienteTelefono = telefono,
            servicio = servicio,
            textoCompleto = texto
        )
    }
}
