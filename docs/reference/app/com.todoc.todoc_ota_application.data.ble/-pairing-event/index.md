//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.ble](../index.md)/[PairingEvent](index.md)

# PairingEvent

sealed class [PairingEvent](index.md)

#### Inheritors

| |
|---|
| [Request](-request/index.md) |
| [Cleared](-cleared/index.md) |

## Types

| Name | Summary |
|---|---|
| [Cleared](-cleared/index.md) | [androidJvm]<br />data object [Cleared](-cleared/index.md) : [PairingEvent](index.md) |
| [Request](-request/index.md) | [androidJvm]<br />data class [Request](-request/index.md)(val device: [BluetoothDevice](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothDevice.html), val variant: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html), val passkey: [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-int/index.html)?) : [PairingEvent](index.md) |


