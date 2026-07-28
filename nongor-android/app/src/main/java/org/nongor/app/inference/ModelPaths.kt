package org.nongor.app.inference

/**
 * Gemma 4 E2B LiteRT-LM bundle (see google-ai-edge/gallery model_allowlists/1_0_11.json).
 */
object ModelPaths {
    const val HF_REPO = "litert-community/gemma-4-E2B-it-litert-lm"
    const val MODEL_FILENAME = "gemma-4-E2B-it.litertlm"
    const val COMMIT_HASH = "7fa1d78473894f7e736a21d920c3aa80f950c0db"
    const val MIN_DEVICE_RAM_GB = 6L

    fun resolveDownloadUrl(): String =
        "https://huggingface.co/$HF_REPO/resolve/$COMMIT_HASH/$MODEL_FILENAME"
}
