package com.ben.khata

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ben.khata.data.ReminderWithPerson

private fun money(value: Long): String = NumberFormat.getNumberInstance(Locale.US).format(value)
private fun date(value: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(value))
private fun dateTime(value: Long): String = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(value))

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun RemindersScreen(vm: KhataViewModel, back: () -> Unit, add: () -> Unit) {
    var selectedReminderId by remember { mutableStateOf<Long?>(null) }
    val reminders by vm.reminders.collectAsState(emptyList())
    
    val selectedReminder = selectedReminderId?.let { id -> reminders.find { it.id == id } }
    if (selectedReminder != null) {
        ReminderDetailScreen(selectedReminder, vm, back = { selectedReminderId = null })
        return
    }

    val now = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    
    
    val today = reminders.filter { it.dueDate <= now && it.status == 0 }
    val upcoming = reminders.filter { it.dueDate > now && it.status == 0 }
    val completed = reminders.filter { it.status == 1 }
    
    var snoozeId by remember { mutableStateOf<Long?>(null) }
    
    if (snoozeId != null) {
        val r = reminders.find { it.id == snoozeId }
        if (r != null) {
            SnoozeSheet(r, { snoozeId = null }) { newDue ->
                vm.updateReminder(r.id, newDue)
                snoozeId = null
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Reminders", fontWeight = FontWeight.ExtraBold) }) }, floatingActionButton = { FloatingActionButton(add, containerColor = MaterialTheme.colorScheme.primary) { Icon(Icons.Default.Add, "Add", tint = Color.White) } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (reminders.isEmpty()) {
                item { 
                    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)); Spacer(Modifier.height(16.dp)); Text("No reminders yet.\nTap + to add one.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                }
            } else {
                if (today.isNotEmpty()) {
                    item { Text("Today (${today.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(today) { r -> ReminderCard(r, onSnooze = { snoozeId = r.id }, onClick = { selectedReminderId = r.id }, onComplete = { vm.completeReminder(r.id) }) }
                }
                if (upcoming.isNotEmpty()) {
                    item { Text("Upcoming (${upcoming.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(upcoming) { r -> ReminderCard(r, onSnooze = { snoozeId = r.id }, onClick = { selectedReminderId = r.id }, onComplete = { vm.completeReminder(r.id) }) }
                }
                if (completed.isNotEmpty()) {
                    item { Text("Completed (${completed.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    items(completed) { r -> ReminderCard(r, onSnooze = { snoozeId = r.id }, onClick = { selectedReminderId = r.id }, onComplete = { vm.uncompleteReminder(r.id) }) }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable private fun ReminderCard(r: ReminderWithPerson, onSnooze: () -> Unit, onClick: () -> Unit, onComplete: () -> Unit) {
    var localStatus by remember(r.status) { mutableStateOf(r.status) }
    var hidden by remember { mutableStateOf(false) }
    if (hidden) return
    val today = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    val status = if (localStatus == 1) (if (r.type == 0) "Paid" else "Received") else if (r.dueDate < today) "Pending" else if (r.dueDate < today + 86400000L) "Due Today" else "Upcoming"
    val statusColor = if (localStatus == 1) MaterialTheme.colorScheme.outline else if (r.dueDate < today) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.05f)), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(if (r.type == 0) MaterialTheme.colorScheme.errorContainer.copy(alpha=0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Text(if (r.type == 0) "PAID" else "REC", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (r.type == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(r.personName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(money(r.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (r.type == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Text(date(r.dueDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { localStatus = if (localStatus == 1) 0 else 1; onComplete() }) { Icon(if (localStatus == 1) Icons.Default.Close else Icons.Default.Check, if (localStatus == 1) "Undo" else "Mark Complete", tint = MaterialTheme.colorScheme.primary) }
                if (localStatus == 0) {
                    IconButton(onClick = { hidden = true; onSnooze() }) { Icon(Icons.Default.Schedule, "Snooze", tint = MaterialTheme.colorScheme.secondary) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(status, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.background(statusColor.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SnoozeSheet(r: ReminderWithPerson, onDismiss: () -> Unit, onSnooze: (Long) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Snooze Reminder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val now = System.currentTimeMillis()
            Button(onClick = { onSnooze(now + 3600000L) }, modifier = Modifier.fillMaxWidth()) { Text("In 1 Hour") }
            Button(onClick = { onSnooze(now + 86400000L) }, modifier = Modifier.fillMaxWidth()) { Text("Tomorrow") }
            Button(onClick = { onSnooze(now + 172800000L) }, modifier = Modifier.fillMaxWidth()) { Text("In 2 Days") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AddReminderScreen(vm: KhataViewModel, back: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    val type = 0 // 0 = To Pay
    
    val today = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    var dueDate by rememberSaveable { mutableLongStateOf(today + 86400000L) }
    var reminderHour by rememberSaveable { mutableIntStateOf(10) }
    var reminderMinute by rememberSaveable { mutableIntStateOf(0) }
    val timing = 1 // Hardcoded to 1 day before
    
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED else true
    ) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (!granted) error = "Notifications disabled."
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { dueDate = it }; showDatePicker = false }) { Text("OK") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }) { DatePicker(state = datePickerState) }
    }
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = reminderHour, initialMinute = reminderMinute)
        AlertDialog(onDismissRequest = { showTimePicker = false }, confirmButton = { TextButton(onClick = { reminderHour = timePickerState.hour; reminderMinute = timePickerState.minute; showTimePicker = false }) { Text("OK") } }, dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }, text = { TimePicker(state = timePickerState) })
    }
    if (showPermissionDialog) {
        AlertDialog(onDismissRequest = { showPermissionDialog = false }, title = { Text("Enable Payment Reminders", fontWeight = FontWeight.Bold) }, text = { Text("Hisaab needs notification permission to remind you about upcoming payments.") }, confirmButton = { TextButton(onClick = { showPermissionDialog = false; if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) } else { hasPermission = true } }) { Text("Allow Notifications") } }, dismissButton = { TextButton(onClick = { showPermissionDialog = false; error = "Notifications disabled." }) { Text("Skip") } })
    }
    
    Scaffold(topBar = { TopAppBar(title = { Text("Add Reminder", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Select Person") }, singleLine = true, shape = RoundedCornerShape(12.dp)) }

            item { OutlinedTextField(amount, { amount = it.filter(Char::isDigit); error = null }, Modifier.fillMaxWidth(), label = { Text("Amount (PKR)") }, prefix = { Text("Rs. ") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp)) }
            item { Text("Due Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OutlinedCard(onClick = { showDatePicker = true }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DateRange, "Calendar", tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Text(date(dueDate), style = MaterialTheme.typography.bodyLarge) } } }
            
            item { Text("Reminder Time (1 day before)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OutlinedCard(onClick = { showTimePicker = true }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Schedule, "Time", tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Text(String.format(Locale.US, "%02d:%02d", reminderHour, reminderMinute), style = MaterialTheme.typography.bodyLarge) } } }
            item { OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Notes (optional)") }, minLines = 2, shape = RoundedCornerShape(12.dp)) }
            item { error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } }
            item { Spacer(Modifier.height(8.dp)) }
            item { Button(onClick = {
                val value = amount.toLongOrNull()
                error = when { name.trim().isEmpty() -> "Enter a person name."; value == null || value <= 0 -> "Enter an amount greater than zero."; else -> null }
                if (error == null) {
                    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { 
                        showPermissionDialog = true 
                    } else {
                        val cal = Calendar.getInstance(); cal.timeInMillis = dueDate; cal.set(Calendar.HOUR_OF_DAY, reminderHour); cal.set(Calendar.MINUTE, reminderMinute); cal.set(Calendar.SECOND, 0)
                        vm.addReminder(name, value!!, type, cal.timeInMillis, timing, note, null)
                        back()
                    }
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)) { Text("Create Reminder", fontWeight = FontWeight.Bold, fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ReminderDetailScreen(r: ReminderWithPerson, vm: KhataViewModel, back: () -> Unit) {
    val today = remember { Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    val isCompleted = r.status == 1
    val status = if (isCompleted) (if (r.type == 0) "Paid" else "Received") else if (r.dueDate < today) "Pending" else if (r.dueDate < today + 86400000L) "Today" else "Upcoming"
    var showSnooze by remember { mutableStateOf(false) }

    if (showSnooze) {
        ModalBottomSheet(onDismissRequest = { showSnooze = false }) {
            Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.NotificationsPaused, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Snooze Reminder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Remind me again for ${r.personName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                
                val snoozeOpts = listOf(Pair("In 1 hour", 3600000L), Pair("Tomorrow", 86400000L), Pair("In 2 days", 172800000L))
                snoozeOpts.forEach { opt ->
                    OutlinedButton(onClick = { 
                        vm.addReminder(r.personName, r.amount, r.type, r.dueDate + opt.second, r.reminderTiming, r.note, r.personId)
                        vm.deleteReminder(r.id)
                        showSnooze = false; back() 
                    }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Schedule, null); Spacer(Modifier.width(8.dp)); Text(opt.first, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                }
                TextButton(onClick = { showSnooze = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Reminder Details") }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(r.personName, size = 64.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(r.personName, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (r.type == 0) "Paid" else "Received", color = Color.White.copy(alpha=0.8f), modifier = Modifier.background(Color.Black.copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(horizontal=8.dp, vertical=4.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(money(r.amount), color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                    Text(status, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.White.copy(alpha=0.2f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp).padding(top = 8.dp))
                }
            }
            
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row { Text("Due Date:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(date(r.dueDate), fontWeight = FontWeight.Bold) }
                    Row { Text("Reminder Time:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(r.dueDate)), fontWeight = FontWeight.Bold) }
                    Row { Text("Created On:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(date(r.createdAt), fontWeight = FontWeight.Bold) }
                    if (r.note.isNotBlank()) {
                        Row { Text("Notes:", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(r.note, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                if (!isCompleted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { vm.completeReminder(r.id); back() }.weight(1f).padding(8.dp)) {
                        Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.tertiaryContainer, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.tertiary) }
                        Spacer(Modifier.height(8.dp)); Text("Complete", style = MaterialTheme.typography.labelMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showSnooze = true }.weight(1f).padding(8.dp)) {
                        Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Snooze, null, tint = MaterialTheme.colorScheme.secondary) }
                        Spacer(Modifier.height(8.dp)); Text("Snooze", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { vm.deleteReminder(r.id); back() }.weight(1f).padding(8.dp)) {
                    Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.errorContainer, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp)); Text("Delete", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
