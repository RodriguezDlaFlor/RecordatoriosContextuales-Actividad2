package com.actividad.recordatorios

import androidx.compose.ui.graphics.Color

object SampleData {
    val personal = Category("personal", "Personal")
    val work = Category("work", "Trabajo")
    val study = Category("study", "Estudio")
    val health = Category("health", "Salud")

    // Set: evita categorías duplicadas según igualdad de la data class.
    val categories: Set<Category> = setOf(personal, work, study, health, personal)

    // Map: relaciona cada identificador con un color visual.
    val categoryColors: Map<String, Color> = mapOf(
        "personal" to Color(0xFF6750A4),
        "work" to Color(0xFF176B87),
        "study" to Color(0xFF386A20),
        "health" to Color(0xFF9A4D00)
    )

    // List: datos simulados, sin persistencia ni red.
    val reminders: List<Reminder> = listOf(
        Reminder(1, "Preparar presentación", "Revisar diapositivas", work, Priority.HIGH, ReminderStatus.Pending, alertAt = "Mañana 09:00"),
        Reminder(2, "Estudiar Kotlin", "Repasar data classes y sealed classes", study, Priority.MEDIUM, ReminderStatus.Pending),
        Reminder(3, "Comprar alimento", category = personal, priority = Priority.LOW, status = ReminderStatus.Completed),
        Reminder(4, "Turno médico", category = health, priority = Priority.HIGH, status = ReminderStatus.Pending, location = "Consultorio"),
        Reminder(5, "Enviar informe", category = work, priority = Priority.MEDIUM, status = ReminderStatus.Completed)
    )

    fun filtered(status: ReminderStatus? = null, priority: Priority? = null): List<Reminder> =
        reminders.filter { item ->
            (status == null || item.status == status) &&
            (priority == null || item.priority == priority)
        }
}
