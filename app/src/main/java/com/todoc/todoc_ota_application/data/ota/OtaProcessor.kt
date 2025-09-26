package com.todoc.todoc_ota_application.data.ota

import android.content.Context

import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtaProcessor(
    scope:CoroutineScope,
    onComplete: () -> Unit,
    context: Context,
    viewModel: MainViewModel
) {
    private val TAG = this.javaClass.simpleName
//    private val otaCommandQueue = OtaCommandQueue(scope, onComplete)

    /*init {
        otaCommandQueue.setOnTimeoutListener {
            showRetryDialog(
                context = context,
                onRetry = {
                    scope.launch {
                        viewModel.connection.collectLatest { st ->
                            when (st) {
                                is ConnectionState.Connected -> {
                                    otaCommandQueue.retryCurrentCommand()
                                }
                                ConnectionState.Connecting -> {
                                    Log.w(TAG, "Connecting")
                                }
                                ConnectionState.Disconnected -> {
                                    connectToDeviceByName(targetInitial) {
                                        otaCommandQueue.retryCurrentCommand()
                                    }
                                }
                            }
                        }

                    }
                },
                onInit = {
                    scope.launch {
                        viewModel.connection.collectLatest { st ->
                            when (st) {
                                is ConnectionState.Connected -> {
                                    Status.transferState = Status.TRANSFER_STATE_IDLE
                                    sendInitMapDataPacket()
                                }
                                ConnectionState.Connecting -> {
                                    Log.w(TAG, "Connecting")
                                }
                                ConnectionState.Disconnected -> {
                                    connectToDeviceByName(targetInitial) {
                                        sendInitMapDataPacket()
                                    }
                                }
                            }
                        }

                    }

                }
            )
        }
    }*/

}