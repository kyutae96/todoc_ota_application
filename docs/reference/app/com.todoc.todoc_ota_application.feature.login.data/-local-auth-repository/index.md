//[app](../../../index.md)/[com.todoc.todoc_ota_application.feature.login.data](../index.md)/[LocalAuthRepository](index.md)

# LocalAuthRepository

[androidJvm]\
class [LocalAuthRepository](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

## Constructors

| | |
|---|---|
| [LocalAuthRepository](-local-auth-repository.md) | [androidJvm]<br />constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Types

| Name | Summary |
|---|---|
| [UserInitialPassKey](-user-initial-pass-key/index.md) | [androidJvm]<br />data class [UserInitialPassKey](-user-initial-pass-key/index.md)(val initial: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val passKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [clearAll](clear-all.md) | [androidJvm]<br />suspend fun [clearAll](clear-all.md)() |
| [clearInitial](clear-initial.md) | [androidJvm]<br />suspend fun [clearInitial](clear-initial.md)() |
| [clearPairingDevice](clear-pairing-device.md) | [androidJvm]<br />suspend fun [clearPairingDevice](clear-pairing-device.md)() |
| [clearPasskey](clear-passkey.md) | [androidJvm]<br />suspend fun [clearPasskey](clear-passkey.md)() |
| [getInitialOrNull](get-initial-or-null.md) | [androidJvm]<br />suspend fun [getInitialOrNull](get-initial-or-null.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? |
| [getPairingDevice](get-pairing-device.md) | [androidJvm]<br />suspend fun [getPairingDevice](get-pairing-device.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? |
| [getPassKeyOrNull](get-pass-key-or-null.md) | [androidJvm]<br />suspend fun [getPassKeyOrNull](get-pass-key-or-null.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? |
| [isLoggedIn](is-logged-in.md) | [androidJvm]<br />suspend fun [isLoggedIn](is-logged-in.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) |
| [saveInitial](save-initial.md) | [androidJvm]<br />suspend fun [saveInitial](save-initial.md)(userInitialPassKey: [LocalAuthRepository.UserInitialPassKey](-user-initial-pass-key/index.md)) |
| [savePairingDevice](save-pairing-device.md) | [androidJvm]<br />suspend fun [savePairingDevice](save-pairing-device.md)(pairingDevice: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |
| [savePasskey](save-passkey.md) | [androidJvm]<br />suspend fun [savePasskey](save-passkey.md)(passKey: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)?) |


