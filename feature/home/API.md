# :feature:home — Public API

> 對外可 import。其他 internal class 不列。

## Nav extension (the only public surface)

### `fun NavGraphBuilder.homeGraph(onAppClick: (String) -> Unit)`
```kotlin
homeGraph(onAppClick = { appId -> ... })
```
- 在 NavHost 內掛載 `composable<AppDestination.Home> { HomeRoute(onAppClick) }`
- `onAppClick(appId)` 由 caller 決定怎麼跳（通常 `nav.navigate(AppDestination.AppDetail(appId))`）
- Source: [`nav/HomeNavGraph.kt`](src/main/kotlin/com/apptest/feature/home/nav/HomeNavGraph.kt)

## NOT public (internal — 可改不破合約)

| Type | Path |
|---|---|
| `HomeRoute` | ui/HomeRoute.kt |
| `HomeScreen` | ui/HomeScreen.kt |
| `HomeViewModel` | ui/HomeViewModel.kt |
| `HomeUiState` (sealed: Loading/Error/Empty/Loaded) | ui/HomeUiState.kt |
| `HomeRepository` (interface) | data/HomeRepository.kt |
| `FakeHomeRepository` | data/FakeHomeRepository.kt |
| `HomeDataModule` (Hilt) | data/di/HomeDataModule.kt |
| `GetHomeDataUseCase` | domain/usecase/GetHomeDataUseCase.kt |
| `HomeUser` / `MatchedApp` / `ActiveTest` / `OwnedApp` / `HomeData` | domain/model/HomeModels.kt |

如其他 feature 開始需要 `HomeData` 或 `MatchedApp` — 那些 type 該升級到 `:core:domain`，不是把 `:feature:home` 變成依賴。

## Hilt bindings contributed

| Type | Bound to | Scope | Source |
|---|---|---|---|
| `HomeRepository` | `FakeHomeRepository` (V1) | `@Singleton` | `data/di/HomeDataModule.kt` |
| `HomeViewModel` | self (`@HiltViewModel`) | per-NavGraph | `ui/HomeViewModel.kt` |
| `GetHomeDataUseCase` | self (`@Inject constructor`) | per-VM | `domain/usecase/GetHomeDataUseCase.kt` |
| `FakeHomeRepository` | self (`@Inject constructor` + `@Singleton`) | singleton | `data/FakeHomeRepository.kt` |

需要 `DispatcherProvider` 由 `:app/di/CoreModule` 提供（不在本 module 內）。

## V2 / V3 forecast public surface

- (V2) `homeGraph(onAppClick, onMatchSkip)` — Skip CTA 真正生效時加 callback
- (V2) `homeGraph(onAppClick, currentTab)` — bottom-bar host 需要選 tab 時加
- (V3) 引入 `HomeRefreshTrigger` (Flow) 讓外部觸發 pull-to-refresh

V1 簽名只有 `onAppClick` — 故意極簡，避免 over-engineering。
