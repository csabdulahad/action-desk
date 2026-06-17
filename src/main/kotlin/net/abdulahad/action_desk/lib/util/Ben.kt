package net.abdulahad.action_desk.lib.util

import java.util.Collections
import java.util.LinkedHashMap
import java.util.concurrent.atomic.AtomicInteger

object Ben {
    private val marks: MutableMap<String, BenchData> = Collections.synchronizedMap(LinkedHashMap())
    private val maxLabelWidth = AtomicInteger(5)

    fun start(label: String) {
        val len = label.length
        maxLabelWidth.updateAndGet { current -> if (current > len) current else len }
        marks[label] = BenchData(System.nanoTime())
    }

    fun end(label: String): Double {
        val data = marks[label] ?: return 0.0
        data.end = System.nanoTime()
        return data.durationSeconds
    }

    fun endPrint(label: String) {
        val duration = end(label)

        if (duration > 0.0) {
            println("$label: ${String.format("%.3f", duration)} sec")
        }
    }

    fun endAll() {
        val now = System.nanoTime()

        synchronized(marks) {
            marks.values.forEach { data ->
                if (data.end == 0L) {
                    data.end = now
                }
            }
        }
    }

    fun endAllPrint(reportTitle: String = "Benchmark Report") {
        synchronized(marks) {
            if (marks.isEmpty()) {
                println("Benchmark Report: No data.")
                return
            }

            val padding = maxLabelWidth.get()
            println("\n$reportTitle")

            var minStart = Long.MAX_VALUE
            var maxEnd = Long.MIN_VALUE

            marks.entries.forEachIndexed { index, entry ->
                val data = entry.value

                if (data.start < minStart) {
                    minStart = data.start
                }

                val effectiveEnd = if (data.end == 0L) System.nanoTime() else data.end

                if (effectiveEnd > maxEnd) {
                    maxEnd = effectiveEnd
                }

                val folderIndex = "${index + 1}.".padEnd(4)
                val label = entry.key.padEnd(padding)
                val duration = String.format("%.3f", data.durationSeconds)

                println("$folderIndex $label = $duration sec")
            }

            val totalDuration = (maxEnd - minStart) / 1_000_000_000.0
            val separator = "-".repeat(padding + 18)
            println(separator)
            println("${"Total".padEnd(padding + 5)} = ${String.format("%.3f", totalDuration)} sec\n")
        }
    }

    fun clear() {
        marks.clear()
        maxLabelWidth.set(5)
    }
}
