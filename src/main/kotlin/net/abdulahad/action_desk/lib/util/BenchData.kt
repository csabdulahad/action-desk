package net.abdulahad.action_desk.lib.util

data class BenchData(
    val start: Long,
    var end: Long = 0
) {
	
    val durationSeconds: Double
        get() {
            val effectiveEnd = if (end == 0L) System.nanoTime() else end
            return (effectiveEnd - start) / 1_000_000_000.0
        }
	
}
