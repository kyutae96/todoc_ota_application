//[app](../../../index.md)/[com.todoc.todoc_ota_application.feature.login](../index.md)/[LoginUiState](index.md)

# LoginUiState

[androidJvm]\
data class [LoginUiState](index.md)(val loading: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) = false, val user: [AppUser](../../com.todoc.todoc_ota_application.feature.login.data/-app-user/index.md)? = null, val role: [Role](../../com.todoc.todoc_ota_application.feature.login.data/-role/index.md) = Role.UNAUTHORIZED, val error: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null)

## Constructors

| | |
|---|---|
| [LoginUiState](-login-ui-state.md) | [androidJvm]<br />constructor(loading: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) = false, user: [AppUser](../../com.todoc.todoc_ota_application.feature.login.data/-app-user/index.md)? = null, role: [Role](../../com.todoc.todoc_ota_application.feature.login.data/-role/index.md) = Role.UNAUTHORIZED, error: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [error](error.md) | [androidJvm]<br />val [error](error.md): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null |
| [loading](loading.md) | [androidJvm]<br />val [loading](loading.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) = false |
| [role](role.md) | [androidJvm]<br />val [role](role.md): [Role](../../com.todoc.todoc_ota_application.feature.login.data/-role/index.md) |
| [user](user.md) | [androidJvm]<br />val [user](user.md): [AppUser](../../com.todoc.todoc_ota_application.feature.login.data/-app-user/index.md)? = null |

## Functions

| Name | Summary |
|---|---|
| [errorMessage](error-message.md) | [androidJvm]<br />fun [errorMessage](error-message.md)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? |


