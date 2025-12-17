package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.border

@Composable
fun ProfileTaskCalendar(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance() // 현재 날짜 기준
    val year = cal.get(Calendar.YEAR)
    val month0 = cal.get(Calendar.MONTH) // 0-based (0=Jan)
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    // 이번 달 1일로 이동해서 시작 요일/일수 계산
    val firstCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0)
        set(Calendar.DAY_OF_MONTH, 1)
        // 시/분/초 통일(날짜 비교 안정화)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val daysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Calendar.DAY_OF_WEEK: 1=Sunday ... 7=Saturday
    // UI는 일~토이므로 offset = (dayOfWeek - 1)
    val startOffset = firstCal.get(Calendar.DAY_OF_WEEK) - 1

    val monthName = firstCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.KOREAN) ?: "${month0 + 1}월"

    Column(modifier = modifier) {
        Text(
            text = "${year}년 $monthName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 요일 헤더
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
        val rows = (totalCells + 6) / 7 // 올림

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
                            val dayTasks = tasks.filter { t ->
                                val due = t.dueDate ?: return@filter false
                                val dcal = Calendar.getInstance().apply {
                                    time = due
                                }
                                dcal.get(Calendar.YEAR) == year &&
                                        dcal.get(Calendar.MONTH) == month0 &&
                                        dcal.get(Calendar.DAY_OF_MONTH) == day
                            }

                            val completionRate = calculateCompletionRate(dayTasks)

                            CalendarDayCell(
                                day = day,
                                isToday = (day == todayDay),
                                completionRate = completionRate,
                                modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier
) {
    val bg = when {
        completionRate == 0 -> Color(0xFFE0E0E0)         // task 없음 또는 0%
        completionRate in 1..49 -> Color(0xFFB2DFDB)     // 낮음
        completionRate in 50..99 -> Color(0xFF4DB6AC)    // 중간
        completionRate == 100 -> Color(0xFF1E8A3B)       // 완료
        else -> Color(0xFFE0E0E0)
    }

    val shape = RoundedCornerShape(6.dp)
    val borderColor = if (isToday) Color(0xFF1E8A3B) else Color.Transparent

    Box(
        modifier = modifier
            .height(42.dp)
            .background(bg, shape)
            .then(
                if (isToday) Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                else Modifier
            ),
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