package org.nongor.app.data

import android.content.Context
import org.nongor.app.core.PhrasebookData

/** Loads the bundled phrasebook once and keeps it for the life of the process. */
object PhrasebookRepository {

    @Volatile
    private var cached: PhrasebookData? = null

    fun get(context: Context): PhrasebookData =
        cached ?: synchronized(this) {
            cached ?: Assets.readObject<PhrasebookData>(context, "phrasebook.json").also {
                cached = it
            }
        }
}
