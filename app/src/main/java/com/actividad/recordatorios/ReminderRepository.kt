package com.actividad.recordatorios

import kotlinx.coroutines.delay

interface ReminderRepository {
    suspend fun getAll(): List<Reminder>
    suspend fun getById(id: Int): Reminder?
    suspend fun save(reminder: Reminder): Reminder
}

class InMemoryReminderRepository : ReminderRepository {
    private val records = SampleData.reminders.toMutableList()
    private var nextId = (records.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun getAll(): List<Reminder> {
        delay(500)
        return records.toList()
    }

    override suspend fun getById(id: Int): Reminder? = records.firstOrNull { it.id == id }

    override suspend fun save(reminder: Reminder): Reminder {
        delay(650)
        val saved = reminder.copy(id = nextId++)
        records.add(0, saved)
        return saved
    }
}
