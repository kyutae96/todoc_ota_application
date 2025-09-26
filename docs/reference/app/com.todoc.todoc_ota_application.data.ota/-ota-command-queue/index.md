//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.ota](../index.md)/[OtaCommandQueue](index.md)

# OtaCommandQueue

[androidJvm]\
class [OtaCommandQueue](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), connector: [BleConnector](../../com.todoc.todoc_ota_application.data.ble/-ble-connector/index.md), onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html))

## Constructors

| | |
|---|---|
| [OtaCommandQueue](-ota-command-queue.md) | [androidJvm]<br />constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), connector: [BleConnector](../../com.todoc.todoc_ota_application.data.ble/-ble-connector/index.md), onComplete: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [scope](scope.md) | [androidJvm]<br />val [scope](scope.md): CoroutineScope |
| [scopeIO](scope-i-o.md) | [androidJvm]<br />val [scopeIO](scope-i-o.md): CoroutineScope |

## Functions

| Name | Summary |
|---|---|
| [clear](clear.md) | [androidJvm]<br />fun [clear](clear.md)() |
| [dumpForDebug](dump-for-debug.md) | [androidJvm]<br />fun [dumpForDebug](dump-for-debug.md)(tag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html) = &quot;OTA-QUEUE&quot;)<br />?붾쾭洹?濡쒓렇: ?먯쓽 ?욌?遺꾩쓣 ?μ궗濡??ㅽ봽 |
| [enqueue](enqueue.md) | [androidJvm]<br />fun [enqueue](enqueue.md)(command: [OtaCommand](../../com.todoc.todoc_ota_application.core.model/-ota-command/index.md)) |
| [onResponse](on-response.md) | [androidJvm]<br />fun [onResponse](on-response.md)(header: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html), commandId: [Byte](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte/index.html)) |
| [planFile](plan-file.md) | [androidJvm]<br />fun [planFile](plan-file.md)(fileId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), displayName: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)?, totalChunks: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) |
| [retryCurrentCommand](retry-current-command.md) | [androidJvm]<br />fun [retryCurrentCommand](retry-current-command.md)()<br />?먮윭 ?곹솴(?⑦궥 ?꾩넚 ?ㅽ뙣)????queue ?뺤씤??泥섏쓬 紐낅졊 遺???ㅼ떆 ?ㅽ뻾 |
| [setOnTimeoutListener](set-on-timeout-listener.md) | [androidJvm]<br />fun [setOnTimeoutListener](set-on-timeout-listener.md)(listener: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html)) |


