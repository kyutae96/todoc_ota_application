//[app](../../../index.md)/[com.todoc.todoc_ota_application.feature.login.data](../index.md)/[FirebaseAuthRepository](index.md)

# FirebaseAuthRepository

[androidJvm]\
class [FirebaseAuthRepository](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), auth: FirebaseAuth = FirebaseAuth.getInstance(), db: FirebaseFirestore = FirebaseFirestore.getInstance())

## Constructors

| | |
|---|---|
| [FirebaseAuthRepository](-firebase-auth-repository.md) | [androidJvm]<br />constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), auth: FirebaseAuth = FirebaseAuth.getInstance(), db: FirebaseFirestore = FirebaseFirestore.getInstance()) |

## Properties

| Name | Summary |
|---|---|
| [docReg](doc-reg.md) | [androidJvm]<br />var [docReg](doc-reg.md): ListenerRegistration? |

## Functions

| Name | Summary |
|---|---|
| [authState](auth-state.md) | [androidJvm]<br />fun [authState](auth-state.md)(): Flow&lt;[AppUser](../-app-user/index.md)?&gt;<br />onAuthStateChanged瑜?Flow濡??섑븨 |
| [login](login.md) | [androidJvm]<br />suspend fun [login](login.md)(email: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), password: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html))<br />濡쒓렇?? Firestore 臾몄꽌 ?놁쑝硫??앹꽦 / owner硫?role 蹂댁젙 |
| [logout](logout.md) | [androidJvm]<br />suspend fun [logout](logout.md)() |
| [refreshUserDoc](refresh-user-doc.md) | [androidJvm]<br />suspend fun [refreshUserDoc](refresh-user-doc.md)(): [AppUser](../-app-user/index.md)? |
| [signup](signup.md) | [androidJvm]<br />suspend fun [signup](signup.md)(email: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), password: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html), organization: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin-stdlib/kotlin/-string/index.html))<br />?뚯썝媛?? Auth 留뚮뱾怨?users/&#123;uid&#125; 臾몄꽌 ?앹꽦 (owner硫?admin) |


