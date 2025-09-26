//[app](../../../index.md)/[com.todoc.todoc_ota_application.feature.login](../index.md)/[LoginViewModel](index.md)

# LoginViewModel

[androidJvm]\
class [LoginViewModel](index.md)(repo: [FirebaseAuthRepository](../../com.todoc.todoc_ota_application.feature.login.data/-firebase-auth-repository/index.md)) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)

## Constructors

| | |
|---|---|
| [LoginViewModel](-login-view-model.md) | [androidJvm]<br />constructor(repo: [FirebaseAuthRepository](../../com.todoc.todoc_ota_application.feature.login.data/-firebase-auth-repository/index.md)) |

## Properties

| Name | Summary |
|---|---|
| [state](state.md) | [androidJvm]<br />val [state](state.md): StateFlow&lt;[LoginUiState](../-login-ui-state/index.md)&gt; |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.todoc.todoc_ota_application.feature.main/-main-view-model/index.md#264516373%2FFunctions%2F355705176) | [androidJvm]<br />open fun [addCloseable](../../com.todoc.todoc_ota_application.feature.main/-main-view-model/index.md#264516373%2FFunctions%2F355705176)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)p0: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [login](login.md) | [androidJvm]<br />fun [login](login.md)(email: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), pw: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |
| [logout](logout.md) | [androidJvm]<br />fun [logout](logout.md)() |
| [refresh](refresh.md) | [androidJvm]<br />fun [refresh](refresh.md)() |
| [signup](signup.md) | [androidJvm]<br />fun [signup](signup.md)(email: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), pw: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), org: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)) |


