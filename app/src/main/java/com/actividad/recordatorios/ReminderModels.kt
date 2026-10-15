package com.actividad.recordatorios

/** Modelos inmutables: todas las propiedades se declaran con val. */
data class Category(val id: String, val label: String)

enum class Priority(val label: String) {
    LOW("Baja"), MEDIUM("Media"), HIGH("Alta")
}

sealed class ReminderStatus(val label: String) {
    data object Pending : ReminderStatus("Pendiente")
    data object Completed : ReminderStatus("Completado")
}

/** Los datos de ubicación y aviso son opcionales en esta primera etapa. */
data class Reminder(
    val id: Int,
    val title: String,
    val description: String? = null,
    val category: Category,
    val priority: Priority,
    val status: ReminderStatus,
    val location: String? = null,
    val alertAt: String? = null
)

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

fun validateReminder(title: String, category: Category?): ValidationResult = when {
    title.isBlank() -> ValidationResult.Invalid("El título no puede estar vacío")
    title.length > 80 -> ValidationResult.Invalid("El título no puede superar 80 caracteres")
    category == null -> ValidationResult.Invalid("Seleccioná una categoría")
    else -> ValidationResult.Valid
}
