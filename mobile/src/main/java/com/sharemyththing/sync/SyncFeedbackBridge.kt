package com.sharemyththing.sync

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SyncFeedbackBridge {
    private val _failures = MutableSharedFlow<SyncResult>(extraBufferCapacity = 1)
    val failures: SharedFlow<SyncResult> = _failures.asSharedFlow()

    fun emitFailure(result: SyncResult) {
        if (result != SyncResult.Success) {
            _failures.tryEmit(result)
        }
    }
}
