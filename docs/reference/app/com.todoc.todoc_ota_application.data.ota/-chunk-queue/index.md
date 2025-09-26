//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.ota](../index.md)/[ChunkQueue](index.md)

# ChunkQueue

[androidJvm]\
class [ChunkQueue](index.md)

## Constructors

| | |
|---|---|
| [ChunkQueue](-chunk-queue.md) | [androidJvm]<br />constructor() |

## Functions

| Name | Summary |
|---|---|
| [clear](clear.md) | [androidJvm]<br />fun [clear](clear.md)() |
| [dumpForDebug](dump-for-debug.md) | [androidJvm]<br />fun [dumpForDebug](dump-for-debug.md)(tag: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html) = &quot;OTA-QUEUE&quot;)<br />?붾쾭洹?濡쒓렇: ?먯쓽 ?욌?遺꾩쓣 ?μ궗濡??ㅽ봽 |
| [enqueue](enqueue.md) | [androidJvm]<br />fun [enqueue](enqueue.md)(index: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), data: [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)) |
| [peekFirst](peek-first.md) | [androidJvm]<br />fun [peekFirst](peek-first.md)(): [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-pair/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt;?<br />媛???묒? ?몃뜳??蹂닿린(?쒓굅 X) |
| [poll](poll.md) | [androidJvm]<br />fun [poll](poll.md)(): [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-pair/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt;?<br />媛???묒? ?몃뜳???섎굹 諛섑솚+?쒓굅 (?꾩넚 猷⑦봽?먯꽌 ?ъ슜) |
| [removeFromIndexInclusive](remove-from-index-inclusive.md) | [androidJvm]<br />fun [removeFromIndexInclusive](remove-from-index-inclusive.md)(startIndex: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)<br />lastSuccess ?댄썑(>= startIndex) ?湲?以?泥?겕 ??젣 (?ъ쟾??濡ㅻ갚?? |
| [size](size.md) | [androidJvm]<br />fun [size](size.md)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html) |
| [snapshot](snapshot.md) | [androidJvm]<br />fun [snapshot](snapshot.md)(max: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html) = 20): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-pair/index.html)&lt;[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)&gt;&gt;<br />?ㅻ깄???뚮퉬 ???? |


