//[app](../../../index.md)/[com.todoc.todoc_ota_application.data.ble](../index.md)/[BleScanner](index.md)

# BleScanner

[androidJvm]\
class [BleScanner](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

## Constructors

| | |
|---|---|
| [BleScanner](-ble-scanner.md) | [androidJvm]<br />constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Properties

| Name | Summary |
|---|---|
| [adapter](adapter.md) | [androidJvm]<br />var [adapter](adapter.md): [BluetoothAdapter](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothAdapter.html)? |
| [bluetoothManager](bluetooth-manager.md) | [androidJvm]<br />var [bluetoothManager](bluetooth-manager.md): [BluetoothManager](https://developer.android.com/reference/kotlin/android/bluetooth/BluetoothManager.html) |
| [devices](devices.md) | [androidJvm]<br />val [devices](devices.md): StateFlow&lt;[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ScanResult](https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanResult.html)&gt;&gt; |
| [scanning](scanning.md) | [androidJvm]<br />val [scanning](scanning.md): StateFlow&lt;[ScanningState](../-scanning-state/index.md)&gt; |

## Functions

| Name | Summary |
|---|---|
| [start](start.md) | [androidJvm]<br />fun [start](start.md)() |
| [stop](stop.md) | [androidJvm]<br />fun [stop](stop.md)() |


