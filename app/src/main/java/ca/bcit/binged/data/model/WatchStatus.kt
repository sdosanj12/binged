package ca.bcit.binged.data.model

enum class WatchStatus(val label: String) {
    WANT_TO_WATCH("Want to Watch"),
    WATCHING("Watching"),
    FINISHED("Finished");

    // Movies say "Watched" instead of "Finished".
    fun labelFor(type: MediaType): String =
        if (type == MediaType.MOVIE && this == FINISHED) "Watched" else label

    companion object {
        // Movies don't get "Watching" (there are no episodes to track).
        fun optionsFor(type: MediaType): List<WatchStatus> =
            if (type == MediaType.MOVIE) listOf(WANT_TO_WATCH, FINISHED) else entries
    }
}