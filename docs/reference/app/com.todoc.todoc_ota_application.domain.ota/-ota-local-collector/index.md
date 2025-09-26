//[app](../../../index.md)/[com.todoc.todoc_ota_application.domain.ota](../index.md)/[OtaLocalCollector](index.md)

# OtaLocalCollector

[androidJvm]\
object [OtaLocalCollector](index.md)

?????깅뮧?β돦裕녻キ???β돦裕뉛쭚????逾??BLE ?熬곣뫖苑?嶺?野?????chunkSize)????濡?뎄 Queue???影?ル츎??

- 
   Index??1?遊붋????戮곗굚, 嶺뚮씭??嶺?????끿춯?EndIndexByte ?ル梨?議얠쾸? ???????깅쾳.
- 
   嶺뚯쉳?듸쭛?묐ご??袁⑸츊揶??怨쀬Ŧ (collected/total) ?띠룄???

## Types

| Name | Summary |
|---|---|
| [OtaChunkPlan](-ota-chunk-plan/index.md) | [androidJvm]<br />data class [OtaChunkPlan](-ota-chunk-plan/index.md)(val payloadSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), val endIndexNum: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), val lastLen: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [collectFromLocalFileC3](collect-from-local-file-c3.md) | [androidJvm]<br />suspend fun [collectFromLocalFileC3](collect-from-local-file-c3.md)(local: [File](https://developer.android.com/reference/kotlin/java/io/File.html), payloadSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), queue: [OtaCommandQueue](../../com.todoc.todoc_ota_application.data.ota/-ota-command-queue/index.md), startIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html) = 1, targetSlot: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), onEnqueue: (index: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), payload: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html)? = null, onProgress: (collected: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), total: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), percent: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html) = &#123; _, _, _ -&gt; &#125;): [OtaLocalCollector.OtaChunkPlan](-ota-chunk-plan/index.md) |
| [intTo3Bytes](int-to3-bytes.md) | [androidJvm]<br />fun [intTo3Bytes](int-to3-bytes.md)(idx: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)): [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html) |
| [planFor](plan-for.md) | [androidJvm]<br />fun [planFor](plan-for.md)(totalBytes: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html), payloadSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html) = 16): [OtaLocalCollector.OtaChunkPlan](-ota-chunk-plan/index.md) |




