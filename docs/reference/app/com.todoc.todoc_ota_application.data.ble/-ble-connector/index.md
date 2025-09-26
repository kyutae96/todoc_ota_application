//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.ble](../index.md)/[BleConnector](index.md)

# BleConnector

[androidJvm]\
class [BleConnector](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

## Constructors

| | |
|---|---|
| [BleConnector](-ble-connector.md) | [androidJvm]<br />constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Properties

| Name | Summary |
|---|---|
| [_internalSerial](_internal-serial.md) | [androidJvm]<br />val [_internalSerial](_internal-serial.md): MutableStateFlow&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [bleResponseFlow](ble-response-flow.md) | [androidJvm]<br />val [bleResponseFlow](ble-response-flow.md): SharedFlow&lt;[BleResponse](../../com.todoc.todoc_ota_application.core.model/-ble-response/index.md)&gt; |
| [bond](bond.md) | [androidJvm]<br />val [bond](bond.md): StateFlow&lt;[BondState](../-bond-state/index.md)&gt; |
| [buildPasswordPacket](build-password-packet.md) | [androidJvm]<br />var [buildPasswordPacket](build-password-packet.md): ([String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)?<br />?쒓났諛쏆? ?⑥뒪?ㅻ? OTA ?꾩넚??諛붿씠?몃줈 援ъ꽦 |
| [connection](connection.md) | [androidJvm]<br />val [connection](connection.md): StateFlow&lt;[ConnectionState](../-connection-state/index.md)&gt; |
| [errorResponseFlow](error-response-flow.md) | [androidJvm]<br />val [errorResponseFlow](error-response-flow.md): SharedFlow&lt;[ErrorResponse](../../com.todoc.todoc_ota_application.core.model/-error-response/index.md)&gt; |
| [infoState](info-state.md) | [androidJvm]<br />val [infoState](info-state.md): StateFlow&lt;[InfoResponse](../../com.todoc.todoc_ota_application.core.model/-info-response/index.md)?&gt; |
| [internalSerial](internal-serial.md) | [androidJvm]<br />val [internalSerial](internal-serial.md): StateFlow&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [otaFileProgress](ota-file-progress.md) | [androidJvm]<br />val [otaFileProgress](ota-file-progress.md): StateFlow&lt;[OtaFileProgress](../../com.todoc.todoc_ota_application.core.model/-ota-file-progress/index.md)?&gt; |
| [packetFlow](packet-flow.md) | [androidJvm]<br />val [packetFlow](packet-flow.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt;&gt; |
| [packetTimeoutMs](packet-timeout-ms.md) | [androidJvm]<br />var [packetTimeoutMs](packet-timeout-ms.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html) |
| [passkeyProvider](passkey-provider.md) | [androidJvm]<br />var [passkeyProvider](passkey-provider.md): suspend () -&gt; [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)??<br />?⑥뒪?ㅻ? 鍮꾨룞湲곕줈 ?쒓났 (DB/DataStore ?? |
| [passwordTimeoutMs](password-timeout-ms.md) | [androidJvm]<br />var [passwordTimeoutMs](password-timeout-ms.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html)<br />?몄쬆 ?湲???꾩븘??ms) 湲곕낯 10珥?|
| [rx](rx.md) | [androidJvm]<br />val [rx](rx.md): SharedFlow&lt;[ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt; |
| [selectState](select-state.md) | [androidJvm]<br />val [selectState](select-state.md): StateFlow&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)&gt; |
| [services](services.md) | [androidJvm]<br />val [services](services.md): StateFlow&lt;[ServiceState](../-service-state/index.md)&gt; |
| [soundProcessorTimeoutMs](sound-processor-timeout-ms.md) | [androidJvm]<br />var [soundProcessorTimeoutMs](sound-processor-timeout-ms.md): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [androidJvm]<br />fun [close](close.md)() |
| [connect](connect.md) | [androidJvm]<br />fun [connect](connect.md)(device: [BluetoothDevice](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothDevice.html)) |
| [disconnect](disconnect.md) | [androidJvm]<br />fun [disconnect](disconnect.md)() |
| [isOtaReady](is-ota-ready.md) | [androidJvm]<br />fun [isOtaReady](is-ota-ready.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html)<br />OTA ?쒕퉬???뱀꽦源뚯? ?뺣낫?섏뿀?붿? |
| [maxPayload](max-payload.md) | [androidJvm]<br />fun [maxPayload](max-payload.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)<br />?덉슜 ?섏씠濡쒕뱶 (MTU-3). ?ㅺ퀬 ???덉쑝硫?蹂댄넻 20. |
| [notifications](notifications.md) | [androidJvm]<br />fun [notifications](notifications.md)(): SharedFlow&lt;[ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt;<br />RX ?뚮┝ ?ㅽ듃由? dev?뭓pp ?섏떊 諛붿씠??|
| [onBleResponse](on-ble-response.md) | [androidJvm]<br />fun [onBleResponse](on-ble-response.md)(header: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), commandId: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), responseData: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)?) |
| [onErrorResponse](on-error-response.md) | [androidJvm]<br />fun [onErrorResponse](on-error-response.md)(header: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), rspCode: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |
| [onInfoResponse](on-info-response.md) | [androidJvm]<br />fun [onInfoResponse](on-info-response.md)(result: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), versionMajor: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), versionMinor: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), currentBootSlotNum: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), preBootSlotNum: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html)) |
| [onOtaFileProgress](on-ota-file-progress.md) | [androidJvm]<br />fun [onOtaFileProgress](on-ota-file-progress.md)(p: [OtaFileProgress](../../com.todoc.todoc_ota_application.core.model/-ota-file-progress/index.md)) |
| [onSelectResponse](on-select-response.md) | [androidJvm]<br />fun [onSelectResponse](on-select-response.md)(result: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) |
| [sendEndCommandByBle](send-end-command-by-ble.md) | [androidJvm]<br />suspend fun [sendEndCommandByBle](send-end-command-by-ble.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendInfoCommandByBle](send-info-command-by-ble.md) | [androidJvm]<br />suspend fun [sendInfoCommandByBle](send-info-command-by-ble.md)(payload: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)) |
| [sendSelectCommandByBle](send-select-command-by-ble.md) | [androidJvm]<br />suspend fun [sendSelectCommandByBle](send-select-command-by-ble.md)(slot: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) |
| [sendStartCommandByBle](send-start-command-by-ble.md) | [androidJvm]<br />suspend fun [sendStartCommandByBle](send-start-command-by-ble.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendWriteDataCommandByBle](send-write-data-command-by-ble.md) | [androidJvm]<br />suspend fun [sendWriteDataCommandByBle](send-write-data-command-by-ble.md)(payload: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendWriteIndex0CommandByBle](send-write-index0-command-by-ble.md) | [androidJvm]<br />suspend fun [sendWriteIndex0CommandByBle](send-write-index0-command-by-ble.md)(payload: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [writeCurrentPacket](write-current-packet.md) | [androidJvm]<br />fun [writeCurrentPacket](write-current-packet.md)(message: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)) |
| [writeOta](write-ota.md) | [androidJvm]<br />suspend fun [writeOta](write-ota.md)(payload: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html), noResponse: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) = true): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html)<br />TX ?뱀꽦???곌린 (payload??maxPayload() ?댄븯濡?蹂대궪 寃? |


