//[app](../../../index.md)/[com.todoc.todoc_ota_application.domain.ota](../index.md)/[OtaLocalCollector](index.md)/[collectFromLocalFileC3](collect-from-local-file-c3.md)

# collectFromLocalFileC3

[androidJvm]\
suspend fun [collectFromLocalFileC3](collect-from-local-file-c3.md)(local: [File](https://developer.android.com/reference/kotlin/java/io/File.html), payloadSize: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), queue: [OtaCommandQueue](../../com.todoc.todoc_ota_application.data.ota/-ota-command-queue/index.md), startIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html) = 1, targetSlot: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), onEnqueue: (index: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), payload: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html)? = null, onProgress: (collected: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), total: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), percent: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-unit/index.html) = &#123; _, _, _ -&gt; &#125;): [OtaLocalCollector.OtaChunkPlan](-ota-chunk-plan/index.md)





