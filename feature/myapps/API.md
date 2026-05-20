# :feature:myapps — Public API

> 對外可 import。Internal 不列。

## Nav extension (the only public surface)

### `fun NavGraphBuilder.myAppsGraph(onNavigateToEditor, onNavigateUp)`
```kotlin
myAppsGraph(
    onNavigateToEditor: (appId: String?) -> Unit,   // null = create flow
    onNavigateUp: () -> Unit,
)
```
- 註冊 `composable<AppDestination.MyApps>` + `composable<AppDestination.AppEditor>` 兩個 entries
- 從 list 點 [+] FAB 呼叫 `onNavigateToEditor(null)` (create)
- 從 list 點 row 呼叫 `onNavigateToEditor(appId)` (edit)
- Editor save 成功或取消都呼叫 `onNavigateUp()`
- Source: [`nav/MyAppsNavGraph.kt`](src/main/kotlin/com/apptest/feature/myapps/nav/MyAppsNavGraph.kt)

## NOT public (internal)

| Type | Path |
|---|---|
| `MyAppsRoute` / `MyAppsScreen` / `MyAppsViewModel` / `MyAppsUiState` | ui/list/ |
| `AppEditorRoute` / `AppEditorScreen` / `AppEditorViewModel` / `AppEditorUiState` | ui/editor/ |
| `MyAppsRepository` (interface) + `FakeMyAppsRepository` + `MyAppsDataModule` | data/ |
| `GetMyAppsUseCase` / `SaveAppUseCase` | domain/usecase/ |
| `PlayOptInUrlValidator` (pure object) | domain/ |
| `OwnedAppRow` / `OwnedAppStatus` / `AppDraft` / `PlayUrlValidation` | domain/model/ |

If `OwnedAppRow` ends up needed by ≥ 2 features → lift to `:core:domain` (not deepen
this module's exports).

## Hilt bindings contributed

| Type | Bound to | Scope |
|---|---|---|
| `MyAppsRepository` | `FakeMyAppsRepository` (V1) | `@Singleton` |
| `MyAppsViewModel` | self (`@HiltViewModel`) | per-NavGraph |
| `AppEditorViewModel` | self (`@HiltViewModel`) | per-NavGraph; reads `SavedStateHandle` for `AppDestination.AppEditor` route args |
| `FakeMyAppsRepository` | self (`@Inject` + `@Singleton`) | singleton |
| `GetMyAppsUseCase` / `SaveAppUseCase` | self (`@Inject constructor`) | per-VM |

需要 `DispatcherProvider`（透過 `SaveAppUseCase` 繼承 `UseCase`）— 由 `:app/di/CoreModule`
提供（不在本 module 重複定義）。

## V2 / V3 forecast public surface

- `myAppsGraph(..., onNavigateToStats)` — V1 沒 stats view，V2 加時擴展 lambda
- `OwnedAppRowMapper` — V2 真 backend 接 Supabase row → 此 model 時 expose 給 mapper test
