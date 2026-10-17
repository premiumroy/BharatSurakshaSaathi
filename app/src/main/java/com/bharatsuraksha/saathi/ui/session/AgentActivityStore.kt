package com.bharatsuraksha.saathi.ui.session

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

/**
 * Today's agent activity, counted on the device only. Nothing is uploaded and
 * nothing is kept beyond the counters shown on the dashboard.
 */
object AgentActivityStore {

    private const val PREFS = "saathi_agent_activity"

    data class Activity(
        val dayKey: String = "",
        val messagesChecked: Int = 0,
        val warningsSent: Int = 0,
        val callsScreened: Int = 0,
        val hourly: List<Int> = List(24) { 0 }
    )

    private val _activity = MutableStateFlow(Activity())
    val activity: StateFlow<Activity> = _activity

    fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = p.getString("day", "").orEmpty()
        val today = todayKey()
        if (stored != today) {
            _activity.value = Activity(dayKey = today)
            persist(context)
            return
        }
        _activity.value = Activity(
            dayKey = today,
            messagesChecked = p.getInt("checked", 0),
            warningsSent = p.getInt("warned", 0),
            callsScreened = p.getInt("calls", 0),
            hourly = p.getString("hourly", "").orEmpty()
                .split(",")
                .mapNotNull { it.toIntOrNull() }
                .let { if (it.size == 24) it else List(24) { 0 } }
        )
    }

    fun recordMessage(context: Context, warned: Boolean) = update(context) { current ->
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hourly = current.hourly.toMutableList()
        hourly[hour] = hourly[hour] + 1
        current.copy(
            messagesChecked = current.messagesChecked + 1,
            warningsSent = current.warningsSent + if (warned) 1 else 0,
            hourly = hourly
        )
    }

    fun recordCall(context: Context) = update(context) { it.copy(callsScreened = it.callsScreened + 1) }

    fun reset(context: Context) {
        _activity.value = Activity(dayKey = todayKey())
        persist(context)
    }

    private fun update(context: Context, block: (Activity) -> Activity) {
        val today = todayKey()
        val base = if (_activity.value.dayKey == today) _activity.value else Activity(dayKey = today)
        _activity.value = block(base)
        persist(context)
    }

    private fun persist(context: Context) {
        val a = _activity.value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("day", a.dayKey)
            .putInt("checked", a.messagesChecked)
            .putInt("warned", a.warningsSent)
            .putInt("calls", a.callsScreened)
            .putString("hourly", a.hourly.joinToString(","))
            .apply()
    }

    private fun todayKey(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }
}
