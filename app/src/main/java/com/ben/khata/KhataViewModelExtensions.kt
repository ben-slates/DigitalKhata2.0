package com.ben.khata

import androidx.lifecycle.viewModelScope
import com.ben.khata.data.KhataDatabase
import com.ben.khata.data.Person
import com.ben.khata.data.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch


fun KhataViewModel.addReminder(
    name: String, amount: Long, type: Int, dueDate: Long, timing: Int, note: String, existingPersonId: Long?
) = viewModelScope.launch {
    val db = KhataDatabase.create(getApplication())
    val dao = db.dao()
    val personId = existingPersonId ?: run {
        val cleanName = name.trim()
        dao.personIdByNormalizedName(cleanName) ?: dao.addPerson(Person(name = cleanName)).let { insertedId ->
            if (insertedId == -1L) dao.personIdByNormalizedName(cleanName) ?: error("Unable to create person") else insertedId
        }
    }
    val reminder = Reminder(personId = personId, type = type, amount = amount, dueDate = dueDate, reminderTiming = timing, note = note.trim())
    val id = dao.addReminder(reminder)
    ReminderUtils.scheduleReminder(getApplication(), reminder.copy(id = id))
}

fun KhataViewModel.completeReminder(id: Long) = viewModelScope.launch {
    val db = KhataDatabase.create(getApplication())
    val dao = db.dao()
    val r = dao.getReminderById(id)
    if (r != null) {
        dao.updateReminderStatus(id, 1)
        ReminderUtils.cancelReminder(getApplication(), id)
        
        val txAmount = if (r.type == 1) r.amount else -r.amount
        dao.addEntry("", txAmount, "From reminder", r.personId)
    }
}

fun KhataViewModel.deleteReminder(id: Long) = viewModelScope.launch {
    KhataDatabase.create(getApplication()).dao().deleteReminder(id)
    ReminderUtils.cancelReminder(getApplication(), id)
}

fun KhataViewModel.uncompleteReminder(id: Long) = viewModelScope.launch {
    val db = KhataDatabase.create(getApplication())
    val dao = db.dao()
    val r = dao.getReminderById(id)
    if (r != null) {
        dao.updateReminderStatus(id, 0)
        
        val txAmount = if (r.type == 1) -r.amount else r.amount
        dao.addEntry("", txAmount, "Reverted reminder", r.personId)
    }
}

fun KhataViewModel.updateReminder(id: Long, newDue: Long) = viewModelScope.launch {
    val db = KhataDatabase.create(getApplication())
    val r = db.dao().getReminderById(id) ?: return@launch
    db.dao().updateReminder(id, r.type, r.amount, newDue, r.reminderTiming, r.note)
}
