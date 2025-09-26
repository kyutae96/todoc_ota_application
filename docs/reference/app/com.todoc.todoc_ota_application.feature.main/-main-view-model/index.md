//[app](../../../index.md)/[com.todoc.todoc_ota_application.feature.main](../index.md)/[MainViewModel](index.md)

# MainViewModel

[androidJvm]\
class [MainViewModel](index.md)(app: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

## Constructors

| | |
|---|---|
| [MainViewModel](-main-view-model.md) | [androidJvm]<br />constructor(app: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Types

| Name | Summary |
|---|---|
| [SlotUiParams](-slot-ui-params/index.md) | [androidJvm]<br />data class [SlotUiParams](-slot-ui-params/index.md)(val currentBootSlot: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)?, val slot1Date: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val slot2Date: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val isSlot1RecentUpdated: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html), val isSlot2RecentUpdated: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [bleResponseFlow](ble-response-flow.md) | [androidJvm]<br />val [bleResponseFlow](ble-response-flow.md): SharedFlow&lt;[BleResponse](../../com.todoc.todoc_ota_application.core.model/-ble-response/index.md)&gt; |
| [connection](connection.md) | [androidJvm]<br />val [connection](connection.md): StateFlow&lt;[ConnectionState](../../com.todoc.todoc_ota_application.data.ble/-connection-state/index.md)&gt; |
| [connector](connector.md) | [androidJvm]<br />val [connector](connector.md): [BleConnector](../../com.todoc.todoc_ota_application.data.ble/-ble-connector/index.md) |
| [current](current.md) | [androidJvm]<br />val [current](current.md): StateFlow&lt;[OtaFileType](../../com.todoc.todoc_ota_application.core.model/-ota-file-type/index.md)?&gt; |
| [devices](devices.md) | [androidJvm]<br />val [devices](devices.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ScanResult](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult.html)&gt;&gt; |
| [errorResponseFlow](error-response-flow.md) | [androidJvm]<br />val [errorResponseFlow](error-response-flow.md): SharedFlow&lt;[ErrorResponse](../../com.todoc.todoc_ota_application.core.model/-error-response/index.md)&gt; |
| [headerEvent](header-event.md) | [androidJvm]<br />val [headerEvent](header-event.md): MutableSharedFlow&lt;[OtaPreparedFile](../../com.todoc.todoc_ota_application.core.model/-ota-prepared-file/index.md)&gt; |
| [infoState](info-state.md) | [androidJvm]<br />val [infoState](info-state.md): StateFlow&lt;[InfoResponse](../../com.todoc.todoc_ota_application.core.model/-info-response/index.md)?&gt; |
| [internalSerial](internal-serial.md) | [androidJvm]<br />val [internalSerial](internal-serial.md): StateFlow&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [isOtaComplete](is-ota-complete.md) | [androidJvm]<br />val [isOtaComplete](is-ota-complete.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html)?&gt; |
| [otaFileProgress](ota-file-progress.md) | [androidJvm]<br />val [otaFileProgress](ota-file-progress.md): StateFlow&lt;[OtaFileProgress](../../com.todoc.todoc_ota_application.core.model/-ota-file-progress/index.md)?&gt; |
| [otaProgress](ota-progress.md) | [androidJvm]<br />val [otaProgress](ota-progress.md): StateFlow&lt;[OtaProgress](../../com.todoc.todoc_ota_application.core.model/-ota-progress/index.md)?&gt; |
| [packetFlow](packet-flow.md) | [androidJvm]<br />val [packetFlow](packet-flow.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt;&gt; |
| [preparedPlan](prepared-plan.md) | [androidJvm]<br />val [preparedPlan](prepared-plan.md): StateFlow&lt;[OtaPreparedPlan](../../com.todoc.todoc_ota_application.core.model/-ota-prepared-plan/index.md)?&gt; |
| [progress](progress.md) | [androidJvm]<br />val [progress](progress.md): StateFlow&lt;[CollectProgress](../../com.todoc.todoc_ota_application.core.model/-collect-progress/index.md)?&gt; |
| [ready](ready.md) | [androidJvm]<br />val [ready](ready.md): StateFlow&lt;[Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |
| [scanning](scanning.md) | [androidJvm]<br />val [scanning](scanning.md): StateFlow&lt;[ScanningState](../../com.todoc.todoc_ota_application.data.ble/-scanning-state/index.md)&gt; |
| [selectState](select-state.md) | [androidJvm]<br />val [selectState](select-state.md): StateFlow&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)&gt; |
| [txQueue](tx-queue.md) | [androidJvm]<br />val [txQueue](tx-queue.md): [OtaCommandQueue](../../com.todoc.todoc_ota_application.data.ota/-ota-command-queue/index.md)<br />COLLECT |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](index.md#264516373%2FFunctions%2F355705176) | [androidJvm]<br />open fun [addCloseable](index.md#264516373%2FFunctions%2F355705176)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)p0: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [buildBootSlotUiParamsForCurrentDevice](build-boot-slot-ui-params-for-current-device.md) | [androidJvm]<br />suspend fun [buildBootSlotUiParamsForCurrentDevice](build-boot-slot-ui-params-for-current-device.md)(): [MainViewModel.SlotUiParams](-slot-ui-params/index.md)<br />Select 遺?낇븷 ?щ’ ?꾩옱 ?곌껐??湲곌린 湲곗??쇰줈 BottomSheet ?뚮씪誘명꽣 鍮뚮뱶 |
| [buildSlotUiParamsForCurrentDevice](build-slot-ui-params-for-current-device.md) | [androidJvm]<br />suspend fun [buildSlotUiParamsForCurrentDevice](build-slot-ui-params-for-current-device.md)(): [MainViewModel.SlotUiParams](-slot-ui-params/index.md)<br />?꾩옱 ?곌껐??湲곌린 湲곗??쇰줈 BottomSheet ?뚮씪誘명꽣 鍮뚮뱶 |
| [connectTo](connect-to.md) | [androidJvm]<br />fun [connectTo](connect-to.md)(result: [ScanResult](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult.html)) |
| [disconnect](disconnect.md) | [androidJvm]<br />fun [disconnect](disconnect.md)(errorReason: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |
| [downloadDataToFile](download-data-to-file.md) | [androidJvm]<br />fun [downloadDataToFile](download-data-to-file.md)(base: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |
| [finishOtaSession](finish-ota-session.md) | [androidJvm]<br />suspend fun [finishOtaSession](finish-ota-session.md)(status: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), errorCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null, currentSlotAfter: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)? = null) |
| [getApplication](index.md#1696759283%2FFunctions%2F355705176) | [androidJvm]<br />open fun &lt;[T](index.md#1696759283%2FFunctions%2F355705176) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](index.md#1696759283%2FFunctions%2F355705176)(): [T](index.md#1696759283%2FFunctions%2F355705176) |
| [getLatestOtaFolder](get-latest-ota-folder.md) | [androidJvm]<br />suspend fun [getLatestOtaFolder](get-latest-ota-folder.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? |
| [getLatestSourcePath](get-latest-source-path.md) | [androidJvm]<br />suspend fun [getLatestSourcePath](get-latest-source-path.md)(deviceName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? |
| [isLatestSourcePathMatching](is-latest-source-path-matching.md) | [androidJvm]<br />suspend fun [isLatestSourcePathMatching](is-latest-source-path-matching.md)(deviceName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [justDisconnect](just-disconnect.md) | [androidJvm]<br />fun [justDisconnect](just-disconnect.md)() |
| [loadTxtFromStorage](load-txt-from-storage.md) | [androidJvm]<br />suspend fun [loadTxtFromStorage](load-txt-from-storage.md)(path: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)?<br />媛쒕퀎 txt 寃쎈줈?먯꽌 ?띿뒪???쎄린 |
| [logEvent](log-event.md) | [androidJvm]<br />suspend fun [logEvent](log-event.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), slot: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)? = null, fileId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null, percent: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)? = null, processedChunks: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)? = null, totalChunks: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)? = null, message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null) |
| [onOtaResponse](on-ota-response.md) | [androidJvm]<br />fun [onOtaResponse](on-ota-response.md)(header: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), commandId: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html)) |
| [selectOtaSession](select-ota-session.md) | [androidJvm]<br />suspend fun [selectOtaSession](select-ota-session.md)(status: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), errorCode: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null, slotSelected: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)? = null, sessionType: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null) |
| [sendEndCommand](send-end-command.md) | [androidJvm]<br />suspend fun [sendEndCommand](send-end-command.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendInfoCommand](send-info-command.md) | [androidJvm]<br />suspend fun [sendInfoCommand](send-info-command.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendResetCommand](send-reset-command.md) | [androidJvm]<br />suspend fun [sendResetCommand](send-reset-command.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendSelectCommand](send-select-command.md) | [androidJvm]<br />suspend fun [sendSelectCommand](send-select-command.md)(slotNum: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendStartCommand](send-start-command.md) | [androidJvm]<br />suspend fun [sendStartCommand](send-start-command.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [setTxtFromLatestOtaFolder](set-txt-from-latest-ota-folder.md) | [androidJvm]<br />suspend fun [setTxtFromLatestOtaFolder](set-txt-from-latest-ota-folder.md)(fileName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), textView: [TextView](https://developer.android.com/reference/kotlin/android/widget/TextView.html), progressBar: [ProgressBar](https://developer.android.com/reference/kotlin/android/widget/ProgressBar.html)) |
| [stageOtaOpsThenEnqueueC3](stage-ota-ops-then-enqueue-c3.md) | [androidJvm]<br />fun [stageOtaOpsThenEnqueueC3](stage-ota-ops-then-enqueue-c3.md)(userId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)?, base: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), slotNum: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), order: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[OtaFileType](../../com.todoc.todoc_ota_application.core.model/-ota-file-type/index.md)&gt;, payloadSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html) = 16)<br />Start ??媛??뚯씪: C3 Index=0 ??C3 ?곗씠???몃뜳?ㅻ뱾 ??End ?쒖꽌濡?OtaTxOperations 由ъ뒪?몃? 留뚮뱾怨? 留덉?留됱뿉 txQueue????踰덉뿉 enqueue. |
| [startScan](start-scan.md) | [androidJvm]<br />fun [startScan](start-scan.md)() |
| [stopScan](stop-scan.md) | [androidJvm]<br />fun [stopScan](stop-scan.md)() |
| [tryAutoConnectByName](try-auto-connect-by-name.md) | [androidJvm]<br />fun [tryAutoConnectByName](try-auto-connect-by-name.md)(exactName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), timeoutMs: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html) = 6000, onTimeout: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html))<br />二쇱뼱吏??대쫫怨??뺥솗???쇱튂?섎뒗 愿묎퀬紐낆쓣 ?먮룞 ?곌껐 ?쒕룄 (timeout 6s ?? |


