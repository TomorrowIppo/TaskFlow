package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProfileTaskCalendar(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    onDayClick: (year: Int, month0: Int, day: Int) -> Unit
) {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month0 = cal.get(Calendar.MONTH)
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    val firstCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val daysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startOffset = firstCal.get(Calendar.DAY_OF_WEEK) - 1
    val monthName =
        firstCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.KOREAN) ?: "${month0 + 1}월"

    Column(modifier = modifier) {
        Text(
            text = "${year}년 $monthName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { w ->
                Text(
                    text = w,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        var day = 1
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col

                        if (cellIndex < startOffset || day > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).height(42.dp))
                        } else {
                            val cellDay = day // ✅ 핵심: 클릭/필터에서 쓸 날짜를 고정 캡처

                            val dayTasks = tasks.filter { t ->
                                val due = t.dueDate ?: return@filter false
                                val dcal = Calendar.getInstance().apply { time = due }
                                dcal.get(Calendar.YEAR) == year &&
                                        dcal.get(Calendar.MONTH) == month0 &&
                                        dcal.get(Calendar.DAY_OF_MONTH) == cellDay
                            }

                            val completionRate = calculateCompletionRate(dayTasks)

                            CalendarDayCell(
                                day = cellDay,
                                isToday = (cellDay == todayDay),
                                completionRate = completionRate,
                                modifier = Modifier.weight(1f),
                                onClick = { onDayClick(year, month0, cellDay) } // ✅ day -> cellDay
                            )

                            day++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    completionRate: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when {
        completionRate == 0 -> Color(0xFFFFFFFF)
        completionRate in 10..24 -> Color(0xFFC8E6C9)
        completionRate in 25..49 -> Color(0xFFA5D6A7)
        completionRate in 50..74 -> Color(0xFF66BB6A)
        completionRate in 75..99 -> Color(0xFF388E3C)
        completionRate == 100 -> Color(0xFF1B5E20)
        else -> Color(0xFFE8F5E9)
    }

    val shape = RoundedCornerShape(6.dp)
    val borderColor = if (isToday) Color(0xFF000000) else Color.Transparent

    Box(
        modifier = modifier
            .height(42.dp)
            .background(bg, shape)
            .then(if (isToday) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

private fun calculateCompletionRate(tasks: List<Task>): Int {
    if (tasks.isEmpty()) return 0
    val completed = tasks.count { it.status == TaskStatus.DONE }
    return ((completed.toFloat() / tasks.size) * 100f).roundToInt()
}