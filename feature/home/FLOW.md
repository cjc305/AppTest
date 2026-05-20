# :feature:home — Internal Flow

> Composition / state / 跳轉的全鏈路。

## Flow 1: First render

```mermaid
flowchart LR
    NH[:app NavHost composable AppDestination.Home] --> HR[HomeRoute]
    HR --> HVM[hiltViewModel HomeViewModel]
    HVM --> INIT[init block]
    INIT --> LOAD[load]
    LOAD --> UC[GetHomeDataUseCase invoke]
    UC --> REPO[HomeRepository.getHomeData]
    REPO --> FAKE[FakeHomeRepository<br/>delay 300ms + mock]
    FAKE -->|AppResult.Success HomeData| UC
    UC --> VM_OK[onSuccess<br/>toLoadedOrEmpty]
    VM_OK -->|empty?| EMPTY[HomeUiState.Empty]
    VM_OK -->|has data| LOADED[HomeUiState.Loaded data]
    LOADED --> SF[state StateFlow update]
    SF --> HR2[HomeRoute collectAsStateWithLifecycle]
    HR2 --> HS[HomeScreen render Loaded branch]
    HS --> LC[HomeLoadedContent LazyColumn]
```

## Flow 2: State machine (HomeUiState)

```mermaid
stateDiagram-v2
    [*] --> Loading: init / load
    Loading --> Loaded: success + non-empty
    Loading --> Empty: success + empty
    Loading --> Error: AppResult.Failure
    Loaded --> Loading: load again (pull-to-refresh)
    Empty --> Loading: load again
    Error --> Loading: onRetry
```

僅 4 個 state。`HomeScreen.when (state)` 對 sealed 編譯器強制 exhaustive — 漏寫 branch = build 失敗（無 fallback warning）。

## Flow 3: User actions

```mermaid
flowchart LR
    U[User taps AppCard in list] --> HS[HomeScreen onAppClick]
    HS --> HR[HomeRoute onAppClick lambda]
    HR --> NH[outer NavHost callback supplied by :app]
    NH --> NAV[navController.navigate AppDestination.AppDetail appId]

    U2[User taps Join on hero card] --> HS2[HomeScreen onJoinMatch]
    HS2 --> HR2[HomeRoute onJoinMatch lambda]
    HR2 --> NH2[onAppClick - V1 alias]
    NH2 --> NAV

    U3[User taps Skip] --> HS3[onSkipMatch]
    HS3 --> HR3[TODO - persist skip + refresh]

    U4[Error retry tap] --> HS4[onRetry]
    HS4 --> VM[viewModel.load]
    VM --> SF[state Loading then re-fetch]
```

## Flow 4: Loaded screen structure (visual hierarchy)

```mermaid
flowchart TB
    SS[ScreenScaffold edge-to-edge]
    SS --> TB[AppTopBar AppTest]
    SS --> LC[LazyColumn contentPadding]
    LC --> GH[GreetingHeader Hi name + tier + credits]
    LC --> SL1[SectionLabel Today]
    LC --> HM{newMatch null?}
    HM -->|no| HMC[HeroMatchCard]
    HM -->|yes| TXT[No new match today...]
    LC --> SL2[SectionLabel Active tests N]
    LC --> AT[items ActiveTestRow with progress]
    LC --> SL3[SectionLabel Your apps N]
    LC --> OA[items OwnedAppRow with progress]
    LC --> SP[bottom Spacer]
```

## Flow 5: When real backend lands (post-APT-V1-R-040)

替換策略：
1. 新 impl `RealHomeRepository @Inject constructor(api: AppTestApi, db: AppDatabase) : HomeRepository`
2. `HomeDataModule.kt` 改 `@Binds bindHomeRepository(impl: RealHomeRepository): HomeRepository`
3. 移除（或保留作 test fixture）`FakeHomeRepository`
4. UI / VM / UseCase / Route — **完全不動**（Clean Architecture 紅利）

驗證：`./gradlew :feature:home:assembleDebug` + 跑既有 unit test 應全綠。
