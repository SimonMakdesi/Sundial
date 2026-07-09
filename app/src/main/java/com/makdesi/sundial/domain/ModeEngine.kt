package com.makdesi.sundial.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.LocalTime
import java.time.ZonedDateTime

enum class Mode { MORNING, DAY, EVENING }

/**
 * Fixed spans for v1 (not user-adjustable yet), but held as data, not constants,
 * so the door stays open (plan §3.2). Evening wraps past midnight to 06:00.
 */
data class ModeSpan(val mode: Mode, val start: LocalTime)

val MODE_SPANS = listOf(
    ModeSpan(Mode.MORNING, LocalTime.of(6, 0)),
    ModeSpan(Mode.DAY, LocalTime.of(9, 0)),
    ModeSpan(Mode.EVENING, LocalTime.of(18, 0)),
)

object ModeEngine {

    /** Fired by system receivers (mode alarm, time/timezone change) so a visible UI recomputes at once. */
    val events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun modeAt(time: LocalTime): Mode {
        var current = MODE_SPANS.last().mode // before the first span start = still evening
        for (span in MODE_SPANS) {
            if (!time.isBefore(span.start)) current = span.mode
        }
        return current
    }

    fun modeAt(now: ZonedDateTime): Mode = modeAt(now.toLocalTime())

    /** The next span-start after `now`, as a wall-clock instant (DST-safe: local time math). */
    fun nextBoundary(now: ZonedDateTime): ZonedDateTime =
        MODE_SPANS.asSequence()
            .flatMap { span ->
                sequenceOf(
                    now.toLocalDate().atTime(span.start).atZone(now.zone),
                    now.toLocalDate().plusDays(1).atTime(span.start).atZone(now.zone),
                )
            }
            .filter { it.isAfter(now) }
            .min()

    fun nextSpan(now: ZonedDateTime): ModeSpan {
        val boundary = nextBoundary(now)
        return MODE_SPANS.first { it.start == boundary.toLocalTime() }
    }
}
