//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.storage](../index.md)/[RangeStream](index.md)

# RangeStream

[androidJvm]\
object [RangeStream](index.md)

## Functions

| Name | Summary |
|---|---|
| [readRange](read-range.md) | [androidJvm]<br />suspend fun [readRange](read-range.md)(path: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), start: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html), endInclusive: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html)): [ByteArray](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-byte-array/index.html)<br />start..endInclusive 踰붿쐞瑜??뺥솗???대젮諛쏅뒗???쒕쾭 Range GET, HTTP 206). |
| [totalBytes](total-bytes.md) | [androidJvm]<br />suspend fun [totalBytes](total-bytes.md)(path: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)): [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-long/index.html)<br />?뚯씪 珥??ъ씠利? GET Range(0-0)濡?Content-Range?먯꽌 ?뚯떛 (ex: &quot;bytes 0-0/123456&quot;) |


