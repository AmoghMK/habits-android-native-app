package com.example

import com.example.data.DurationUnit
import com.example.util.TimeUtils
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun timerCalculatesCorrectNextDueAt_hours() {
      val lastCompletedAt = Instant.parse("2026-09-10T08:23:00Z").toEpochMilli()
      val nextDue = TimeUtils.calculateNextDueAt(lastCompletedAt, 36, DurationUnit.HOURS)
      val expected = Instant.parse("2026-09-11T20:23:00Z").toEpochMilli()
      assertEquals(expected, nextDue)
  }

  @Test
  fun timerCalculatesCorrectNextDueAt_minutes() {
      val lastCompletedAt = Instant.parse("2026-09-10T08:23:00Z").toEpochMilli()
      val nextDue = TimeUtils.calculateNextDueAt(lastCompletedAt, 30, DurationUnit.MINUTES)
      val expected = Instant.parse("2026-09-10T08:53:00Z").toEpochMilli()
      assertEquals(expected, nextDue)
  }
}
