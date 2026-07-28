package org.nongor.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.data.download.HfDownloadRepository
import org.nongor.app.inference.ModelPaths
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val downloading: Boolean = false,
    val progress: Pair<Long, Long> = 0L to -1L,
    val error: String? = null,
    val lowMemoryWarning: Boolean = false,
    val completed: Boolean = false,
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication

    private val _ui = MutableStateFlow(OnboardingUiState())
    val ui: StateFlow<OnboardingUiState> = _ui.asStateFlow()

    init {
        val actManager = application.getSystemService(android.app.ActivityManager::class.java)
        val info = android.app.ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(info)
        val totalGb = info.totalMem / (1024L * 1024L * 1024L)
        _ui.value = _ui.value.copy(
            lowMemoryWarning = totalGb < ModelPaths.MIN_DEVICE_RAM_GB,
        )
    }

    fun startDownload() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(downloading = true, error = null, progress = 0L to -1L)
            val dest = HfDownloadRepository.modelFile(getApplication())
            val result = app.downloadRepository.download(
                url = ModelPaths.resolveDownloadUrl(),
                destFile = dest,
                onProgress = { done, total ->
                    _ui.value = _ui.value.copy(progress = done to total)
                },
            )
            _ui.value = if (result.isSuccess) {
                _ui.value.copy(downloading = false, completed = true)
            } else {
                _ui.value.copy(
                    downloading = false,
                    error = result.exceptionOrNull()?.message ?: "Download failed",
                )
            }
        }
    }
}
