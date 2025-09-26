//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.ble](../index.md)/[ServiceState](index.md)

# ServiceState

sealed class [ServiceState](index.md)

#### Inheritors

| |
|---|
| [Idle](-idle/index.md) |
| [Discovering](-discovering/index.md) |
| [Ready](-ready/index.md) |
| [Failed](-failed/index.md) |

## Types

| Name | Summary |
|---|---|
| [Discovering](-discovering/index.md) | [androidJvm]<br />data object [Discovering](-discovering/index.md) : [ServiceState](index.md) |
| [Failed](-failed/index.md) | [androidJvm]<br />data class [Failed](-failed/index.md)(val status: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) : [ServiceState](index.md) |
| [Idle](-idle/index.md) | [androidJvm]<br />data object [Idle](-idle/index.md) : [ServiceState](index.md) |
| [Ready](-ready/index.md) | [androidJvm]<br />data class [Ready](-ready/index.md)(val mtu: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)) : [ServiceState](index.md) |


