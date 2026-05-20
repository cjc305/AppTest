# :feature:profile — Dependencies

## I depend on
| Module | Why |
|---|---|
| `:core:common` | AppResult / AppError / ReputationTier |
| `:core:designsystem` | tokens + AppText/AppIcon |
| `:core:ui` | ScreenScaffold/TopBar/Card/Button/ProgressBar/AppTierBadge/AppListItem + three-state |
| `:core:domain` | NoParamUseCase base |
| `:core:navigation` | AppDestination.Profile |

## Modules depending on me
`:app` only via `profileGraph(...)`.

## How to replace Fake → real
1. RealProfileRepository wraps `GET /me` + `GET /me/reputation` + `GET /me/reputation/history` + proofs query
2. di ProfileDataModule 改 @Binds
3. UI/VM 不動

## File budget
- ProfileScreen ~115
- ProfileViewModel ~33
- ProfileHeader ~50
- ReputationBreakdownCard ~52
- StatsCard ~43
- 其餘 < 40
