//[app](../../index.md)/[com.todoc.todoc_ota_application.feature.login.data](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [AppUser](-app-user/index.md) | [androidJvm]<br />data class [AppUser](-app-user/index.md)(val uid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val email: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val avatar: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), val organization: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html)? = null, val role: [Role](-role/index.md) = Role.UNAUTHORIZED) |
| [FirebaseAuthRepository](-firebase-auth-repository/index.md) | [androidJvm]<br />class [FirebaseAuthRepository](-firebase-auth-repository/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), auth: FirebaseAuth = FirebaseAuth.getInstance(), db: FirebaseFirestore = FirebaseFirestore.getInstance()) |
| [LocalAuthRepository](-local-auth-repository/index.md) | [androidJvm]<br />class [LocalAuthRepository](-local-auth-repository/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |
| [Role](-role/index.md) | [androidJvm]<br />enum [Role](-role/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-enum/index.html)&lt;[Role](-role/index.md)&gt; |


