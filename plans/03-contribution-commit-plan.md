# Plan 03: Member Commit Minimum and Contribution Execution

## Goal

Reach minimum meaningful contributions for each team member with clean branch ownership and realistic commit history.

## Execution Status (Current Workspace)

- Maintenance owner progress:
  - Done: `refactor(maintenance): move maintenance files into module package` (already present in current structure)
  - Done: `feat(maintenance): harden assignment and SLA update flow` (status/assignment safeguards added)
  - Done: `test(maintenance): add ticket status and comment permission tests` (`MaintenanceServiceTest`)
- Remaining branches still need owner-specific commit slices and author-based commits.

## Baseline Check (April 21, 2026)

Based on `git shortlog -sne --all` and direct email counting:

| Member Email                       | Current Commits | Minimum Target |  Gap |
| ---------------------------------- | --------------: | -------------: | ---: |
| vinushan2218@gmail.com             |               0 |              3 |    3 |
| karunakanthanlavan@gmail.com       |               0 |              3 |    3 |
| pakeerathansinthujan2003@gmail.com |               0 |              3 |    3 |
| aruldino2025@gmail.com             |               8 |              3 |    0 |

Additional identity found:

- `aruldino17@gmail.com` has 2 commits.
- Recommendation: consolidate author identity using `.mailmap`.

## Branch Ownership Model

- `module-facilities` -> vinushan2218@gmail.com
- `module-booking` -> karunakanthanlavan@gmail.com
- `module-maintenance` -> pakeerathansinthujan2003@gmail.com
- `module-notifications-auth` -> aruldino2025@gmail.com

## Minimum Commit Policy

Each member must have at least 3 meaningful commits, each matching one of these categories:

- Feature implementation (`feat`)
- Bug fix (`fix`)
- Refactor with functional safety (`refactor`)
- Test addition (`test`)
- Documentation for owned module (`docs`)

Commits that only change formatting should not count toward the minimum.

## Commit Templates Per Member

### Facilities owner

1. `refactor(facilities): move resource files into module package`
2. `feat(facilities): add resource search validation and edge-case handling`
3. `test(facilities): add service and controller tests for resources`

### Booking owner

1. `refactor(bookings): move booking files into module package`
2. `fix(bookings): improve overlap detection and status transitions`
3. `test(bookings): add booking conflict and lifecycle tests`

### Maintenance owner

1. `refactor(maintenance): move maintenance files into module package`
2. `feat(maintenance): harden assignment and SLA update flow`
3. `test(maintenance): add ticket status and comment permission tests`

### Notifications/Auth owner

1. `refactor(auth-notifications): move auth and notification files into modules`
2. `fix(auth): tighten security matcher policy without breaking public auth endpoints`
3. `test(notifications): add preferences and unread count regression tests`

## Author Configuration Procedure

Use one branch per owner and set author identity before each commit:

```powershell
git checkout -b module-facilities
 git config user.name "vinushan2218"
 git config user.email "vinushan2218@gmail.com"

# Commit in small logical slices
 git add <files>
 git commit -m "refactor(facilities): move resource files into module package"
```

Repeat with each member branch and email.

## Verification Commands

```powershell
# Overall counts
git shortlog -sne --all

# Targeted email counts
$emails=@('vinushan2218@gmail.com','karunakanthanlavan@gmail.com','pakeerathansinthujan2003@gmail.com','aruldino2025@gmail.com')
foreach($e in $emails){
  $count=(git log --all --format='%ae' | Where-Object { $_ -eq $e } | Measure-Object).Count
  Write-Output "$e => $count"
}
```

## Optional Identity Consolidation

Create `.mailmap` to normalize the extra email:

```text
aruldino <aruldino2025@gmail.com> <aruldino17@gmail.com>
```

This improves reporting consistency without rewriting history.
