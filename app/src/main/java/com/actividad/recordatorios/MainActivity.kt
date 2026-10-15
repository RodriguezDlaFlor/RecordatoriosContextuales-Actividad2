package com.actividad.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val repository = remember { InMemoryReminderRepository() }
                val vm: ReminderViewModel = viewModel(factory = ReminderViewModel.factory(repository))
                ReminderApp(vm)
            }
        }
    }
}

@Composable
fun ReminderApp(vm: ReminderViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "list") {
        composable("list") {
            val state by vm.list.collectAsStateWithLifecycle()
            ListScreen(state, onCreate = {
                vm.resetForm()
                nav.navigate("create")
            }, onOpen = { nav.navigate("detail/$it") }, onRetry = vm::reload,
                onSimulate = vm::simulate)
        }
        composable("create") {
            val state by vm.form.collectAsStateWithLifecycle()
            LaunchedEffect(state.saved) { if (state.saved) nav.popBackStack() }
            CreateScreen(state, vm::setTitle, vm::setDescription, vm::setCategory,
                vm::setPriority, vm::save, onCancel = { nav.popBackStack() })
        }
        composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("id") ?: -1
            val list by vm.list.collectAsStateWithLifecycle()
            // The ID is the only navigation argument; the item comes from the repository.
            var reminder by remember(id) { mutableStateOf<Reminder?>(null) }
            LaunchedEffect(id, list) { reminder = vm.findById(id) }
            DetailScreen(reminder, onBack = { nav.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    state: ListUiState, onCreate: () -> Unit, onOpen: (Int) -> Unit,
    onRetry: () -> Unit, onSimulate: (String) -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Mis recordatorios") }) },
        floatingActionButton = { FloatingActionButton(onClick = onCreate) { Text("+ Nuevo") } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Actividad 2 · Estados de prueba", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("normal" to "Datos", "empty" to "Vacío", "error" to "Error").forEach { (mode, label) ->
                    AssistChip(onClick = { onSimulate(mode) }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(8.dp))
            when (state) {
                ListUiState.Loading -> StatusPanel("Cargando recordatorios…", null)
                ListUiState.Empty -> StatusPanel("Todavía no hay recordatorios", "Crear recordatorio", onCreate)
                is ListUiState.Error -> StatusPanel(state.message, "Reintentar", onRetry)
                is ListUiState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.reminders, key = { it.id }) { item -> ReminderCard(item, onClick = { onOpen(item.id) }) }
                }
            }
        }
    }
}

@Composable
fun StatusPanel(message: String, actionLabel: String?, onAction: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message)
        if (actionLabel != null) Button(onClick = onAction) { Text(actionLabel) }
        if (actionLabel == null) CircularProgressIndicator()
    }
}

@Composable
fun ReminderCard(reminder: Reminder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Categoría: ${reminder.category.label}")
            Text("Prioridad: ${reminder.priority.label} · Estado: ${reminder.status.label}")
            Text("Ver detalle →", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ValidatedField(value: String, onChange: (String) -> Unit, label: String,
    errorMessage: String?, showError: Boolean, modifier: Modifier = Modifier, maxLines: Int = 1) {
    val visibleError = if (showError) errorMessage else null
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        modifier = modifier.fillMaxWidth().then(if (visibleError != null) Modifier.semantics { error(visibleError) } else Modifier),
        isError = visibleError != null, supportingText = {
            if (visibleError != null) Text(visibleError)
        }, maxLines = maxLines)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: FormUiState, onTitle: (String) -> Unit, onDescription: (String) -> Unit,
    onCategory: (String) -> Unit, onPriority: (Priority) -> Unit,
    onSave: () -> Unit, onCancel: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Nuevo recordatorio") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ValidatedField(state.title, onTitle, "Título", state.titleError, state.touched || state.title.isNotEmpty())
            ValidatedField(state.description, onDescription, "Descripción", state.descriptionError,
                state.touched || state.description.length > 200, maxLines = 5)
            Text("${state.description.length}/200 caracteres")
            Text("Categoría", fontWeight = FontWeight.Bold)
            SampleData.categories.forEach { category ->
                Row { RadioButton(selected = state.categoryId == category.id, onClick = { onCategory(category.id) })
                    Text(category.label, Modifier.padding(top = 12.dp)) }
            }
            state.categoryError?.let { error ->
                if (state.touched) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
            Text("Prioridad", fontWeight = FontWeight.Bold)
            Priority.entries.forEach { priority ->
                Row { RadioButton(selected = state.priority == priority, onClick = { onPriority(priority) })
                    Text(priority.label, Modifier.padding(top = 12.dp)) }
            }
            state.priorityError?.let { error ->
                if (state.touched) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
            state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSave, enabled = state.canSave) { Text(if (state.isSaving) "Guardando…" else "Guardar") }
                OutlinedButton(onClick = onCancel, enabled = !state.isSaving) { Text("Cancelar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(reminder: Reminder?, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle") },
        navigationIcon = { TextButton(onClick = onBack) { Text("← Volver") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (reminder == null) {
                Text("Cargando detalle o recordatorio no encontrado")
            } else {
                Text(reminder.title, style = MaterialTheme.typography.headlineSmall)
                Text("ID: ${reminder.id}")
                Text("Descripción: ${reminder.description ?: "Sin descripción"}")
                Text("Categoría: ${reminder.category.label}")
                Text("Prioridad: ${reminder.priority.label}")
                Text("Estado: ${reminder.status.label}")
                Text("Ubicación: ${reminder.location ?: "No especificada"}")
                Text("Aviso: ${reminder.alertAt ?: "No configurado"}")
            }
        }
    }
}
