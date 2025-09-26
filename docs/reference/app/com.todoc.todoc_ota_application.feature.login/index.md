//[app](../../index.md)/[com.todoc.todoc_ota_application.feature.login](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [LoginFragment](-login-fragment/index.md) | [androidJvm]<br />class [LoginFragment](-login-fragment/index.md) : [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html) |
| [LoginUiState](-login-ui-state/index.md) | [androidJvm]<br />data class [LoginUiState](-login-ui-state/index.md)(val loading: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-boolean/index.html) = false, val user: [AppUser](../com.todoc.todoc_ota_application.feature.login.data/-app-user/index.md)? = null, val role: [Role](../com.todoc.todoc_ota_application.feature.login.data/-role/index.md) = Role.UNAUTHORIZED, val error: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null) |
| [LoginViewModel](-login-view-model/index.md) | [androidJvm]<br />class [LoginViewModel](-login-view-model/index.md)(repo: [FirebaseAuthRepository](../com.todoc.todoc_ota_application.feature.login.data/-firebase-auth-repository/index.md)) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html) |
| [LoginViewModelFactory](-login-view-model-factory/index.md) | [androidJvm]<br />class [LoginViewModelFactory](-login-view-model-factory/index.md)(repo: [FirebaseAuthRepository](../com.todoc.todoc_ota_application.feature.login.data/-firebase-auth-repository/index.md)) : [ViewModelProvider.Factory](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModelProvider.Factory.html) |


