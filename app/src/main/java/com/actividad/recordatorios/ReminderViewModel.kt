package com.actividad.recordatorios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ListUiState {
    data object Loading : ListUiState
    data object Empty : ListUiState
    data class Content(val reminders: List<Reminder>) : ListUiState
    data class Error(val message: String) : ListUiState
}

data class FormUiState(
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val priority: Priority? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val generalError: String? = null,
    val touched: Boolean = false
) {
    val titleError: String? get() = when {
        title.trim().length < 4 -> "El título debe tener al menos 4 caracteres"
        title.trim().length > 80 -> "El título no puede superar 80 caracteres"
        else -> null
    }
    val descriptionError: String? get() = if (description.length > 200) "Máximo 200 caracteres" else null
    val categoryError: String? get() = if (SampleData.categories.none { it.id == categoryId }) "Seleccioná una categoría válida" else null
    val priorityError: String? get() = if (priority == null) "Seleccioná una prioridad" else null
    val isValid: Boolean get() = titleError == null && descriptionError == null && categoryError == null && priorityError == null
    val canSave: Boolean get() = isValid && !isSaving
}

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {
    private val _list = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val list: StateFlow<ListUiState> = _list.asStateFlow()
    private val _form = MutableStateFlow(FormUiState())
    val form: StateFlow<FormUiState> = _form.asStateFlow()
    private var demoMode = "normal"

    init { reload() }

    fun reload() {
        viewModelScope.launch {
            _list.value = ListUiState.Loading
            try {
                val data = repository.getAll()
                _list.value = when (demoMode) {
                    "empty" -> ListUiState.Empty
                    "error" -> ListUiState.Error("No se pudieron cargar los recordatorios (simulación).")
                    else -> if (data.isEmpty()) ListUiState.Empty else ListUiState.Content(data)
                }
            } catch (e: Exception) {
                _list.value = ListUiState.Error(e.message ?: "Error al cargar los recordatorios")
            }
        }
    }

    fun simulate(mode: String) { demoMode = mode; reload() }
    suspend fun findById(id: Int): Reminder? = repository.getById(id)
    fun resetForm() { _form.value = FormUiState() }
    fun setTitle(value: String) { _form.update { it.copy(title = value, generalError = null) } }
    fun setDescription(value: String) { _form.update { it.copy(description = value, generalError = null) } }
    fun setCategory(value: String) { _form.update { it.copy(categoryId = value, generalError = null) } }
    fun setPriority(value: Priority) { _form.update { it.copy(priority = value, generalError = null) } }

    fun save() {
        val current = _form.value
        _form.update { it.copy(touched = true) }
        if (!current.canSave) return
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, generalError = null) }
            try {
                val category = SampleData.categories.first { it.id == current.categoryId }
                repository.save(Reminder(
                    id = 0, title = current.title.trim(),
                    description = current.description.trim().ifBlank { null },
                    category = category, priority = requireNotNull(current.priority),
                    status = ReminderStatus.Pending
                ))
                _form.update { it.copy(isSaving = false, saved = true) }
                demoMode = "normal"
                reload()
            } catch (e: Exception) {
                _form.update { it.copy(isSaving = false, generalError = e.message ?: "No se pudo guardar") }
            }
        }
    }

    companion object {
        fun factory(repository: ReminderRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReminderViewModel(repository) as T
            }
    }
}
