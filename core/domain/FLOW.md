# :core:domain — Internal Flow

> 本模組無業務流程（只是 contract）。本文檔說明 UseCase / Repository 在跨層呼叫中的位置，以及 cross-feature contract 的 producer/consumer 拓樸。

## Flow 1: UI → UseCase → Repository → adapter

```mermaid
flowchart LR
    UI[Composable] -->|user action| VM[ViewModel]
    VM -->|invoke params| UC[UseCase :core:domain<br/>withContext io]
    UC -->|abstract execute| IMPL[concrete UseCase :feature]
    IMPL -->|method call| REPO[Repository :feature/data 或 :core:data]
    REPO -->|adapter call| EXT[Retrofit / Supabase SDK / Room DAO]
    EXT -.network/DB.-> WIRE[(remote / local)]

    EXT -->|throws| WRAP[runCatchingApp at adapter]
    WRAP -->|AppResult| REPO
    REPO -->|AppResult| IMPL
    IMPL -->|AppResult| UC
    UC -->|AppResult| VM
    VM -->|map to UiState| UI
```

**關鍵契約：** UseCase base 已包 `withContext(io)`，concrete `execute` **不要** 再切 dispatcher（除非有 CPU-bound 計算需 `default`）。

## Flow 2: AuthRepository — cross-feature contract 拓樸

```mermaid
flowchart LR
    subgraph CD[:core:domain/auth]
        IF[AuthRepository interface]
    end
    subgraph FA[:feature:auth]
        IMPL[FakeAuthRepository<br/>未來 RealAuthRepository]
        BIND[Hilt @Binds 在 AuthDataModule]
    end
    subgraph APP[:app]
        MA[MainActivity<br/>observe state for startDestination]
    end
    subgraph FO[:feature:onboarding]
        OVM[OnboardingViewModel<br/>呼叫 markOnboardingComplete]
    end
    subgraph FP[:feature:profile]
        PVM[ProfileViewModel<br/>未來呼叫 signOut]
    end

    IMPL -- implements --> IF
    BIND -- binds impl to --> IF
    MA -- reads --> IF
    OVM -- writes via --> IF
    PVM -- writes via --> IF
```

**Producer/Consumer 純粹：**
- 寫者唯一 — `:feature:auth`
- 讀者多 — `:app` / `:feature:onboarding` / `:feature:profile`
- 所有 module 都只看 `:core:domain` 介面，互不知道對方存在

無此中介層的話：`:feature:onboarding` 要存取 auth → 直接 import `:feature:auth` → 違反 hard rule (no feature-to-feature dep)。

## Flow 3: UseCase 內部 error 流

```mermaid
flowchart LR
    EX[execute body throws] --> CAN{is CancellationException?}
    CAN -->|yes| RT[re-throw]
    CAN -->|no| FT[AppError.fromThrowable t]
    FT --> FAIL[AppResult.Failure]
    EX -->|returns AppResult| PASS[pass through unchanged]
```

Concrete UseCase 內部 **可以** 直接 throw（被 base catch），但慣例上：
- Adapter 邊界以內回 `AppResult` — clean
- 只有真正不可預期的 NPE / IllegalState 才會 propagate 到 throw path

## Sequencing pattern: 多 use case 鏈接

```mermaid
flowchart LR
    A[useCase1] -->|AppResult.Success| B[flatMap to useCase2]
    A -->|AppResult.Failure| ERR[stop, propagate error]
    B -->|AppResult.Success| C[flatMap to useCase3]
    B -->|AppResult.Failure| ERR
    C --> DONE[final AppResult]
```

VM 內部用 `flatMap` 鏈接，**不要** 把多個 use case 塞同一個。一 use case = 一 user-facing action（per UseCase.kt KDoc）。

## 何時把 feature-local repository 移到 :core:domain

```mermaid
flowchart LR
    Q1{兩個以上 feature 都要用?} -->|no| KEEP[留在 feature]
    Q1 -->|yes| Q2{介面穩定 不易變動?}
    Q2 -->|no| KEEP2[暫留 feature, 兩 feature 重複實作 1 次也 OK]
    Q2 -->|yes| MOVE[移介面到 :core:domain<br/>impl 留 feature 或搬 :core:data]
```

YAGNI：**重複一次都好過早期抽象錯方向**。第三個 feature 要用時再考慮搬。
