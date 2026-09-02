# Resource Planning Ledger


A resource planning and accounting system based on Fowler's *Analysis Patterns*:
an operations team plans work as a tree of **proposed actions**, allocates **resources** (assets and
consumables) to them, drives each action through a **lifecycle state machine**, and every completion
posts a balanced **double-entry ledger** transaction with a full audit trail. Chapter 8 (Planning) supplies
protocols, plans, proposed vs. implemented actions and allocations; Chapter 6 (Inventory and Accounting)
supplies accounts, transactions, entries, posting rules and the audit log.

## Intent covered

- **F1–F2** — knowledge-level catalogues: protocols with ordered steps and inter-step dependencies;
  resource types (`ASSET` / `CONSUMABLE`) with unit of measure, unit cost and a linked pool account.
  Protocols and resource types are edited only through their catalogues — never as a side effect of
  running a plan.
- **F3** — plan management: a plan is instantiated from a protocol (one `ProposedAction` per step, with the
  dependency structure copied) or built from scratch, and is itself a composite tree of sub-plans and leaves.
- **F4–F5** — action lifecycle driven by the State pattern; `implement()` creates a linked
  `ImplementedAction` recording actual start, party and location, and the UI shows the plan-vs-reality diff.
- **F6** — `GENERAL` / `SPECIFIC` allocations, with named asset identifier and time period for specific ones.
- **F7–F9** — `complete()` posts a transaction that withdraws from the pool account and deposits into a
  per-action usage account, each entry carrying both a *charged* and a *booked* timestamp; a posting rule
  fires eagerly and writes an alert entry when a pool balance goes below zero; the ledger view shows entries
  and balances per account.
- **F10** — plan summary report driven by a depth-first iterator over the composite tree, visiting leaves
  and sub-plans uniformly through `PlanNode`.
- **Week 2 changes** — an approval gate (`PENDING_APPROVAL`) and reopen flow with reversal entries;
  time-based ledger entries for specific asset allocations; filtered and depth-limited iterators; and
  visitor-based plan metrics. Each change lands in the pattern it was aimed at, leaving the rest untouched.
- Four-layer separation is enforced: `client/` (HTTP + DTOs only) → `manager/` (use-case orchestration) →
  `engine/` (replaceable algorithms) → `resourceaccess/` (JPA access). Engines do not call engines;
  repositories publish no events; controllers hold no business logic.

## Design patterns

**State** — `domain/state/ActionState` is implemented by seven stateless singleton beans:
`ProposedState`, `PendingApprovalState`, `InProgressState`, `SuspendedState`, `CompletedState`,
`ReopenedState`, `AbandonedState`. All mutable data lives in `ActionContext`, which wraps the JPA entity and
an `ActionCallbacks` reference (implemented by `ActionManager`) so a transition can trigger ledger posting and
audit logging. Unsupported transitions throw `IllegalStateTransitionException`. Week 2's approval gate and
reopen flow added two state classes and one changed line in `ProposedState.implement()`; nothing else moved.

**Composite** — `domain/composite/PlanNode` is implemented by both `ProposedAction` (leaf) and `Plan`
(composite). `Plan.getStatus()` is *derived* from its descendants — `COMPLETED` only when all leaves are,
`IN_PROGRESS` when some are, `SUSPENDED` when any is suspended and none in progress, `ABANDONED` when all
are — and `getTotalAllocatedQuantity(ResourceType)` sums recursively. Callers never branch on leaf vs. sub-plan.

**Iterator** — three `java.util.Iterator<PlanNode>` implementations traverse the same tree, so any consumer
written against the interface works with all three. `DepthFirstPlanIterator` is the base traversal (used by the
report, dependency checks, and `Plan`'s own derived status); `FilteredPlanIterator` wraps it with a
`Predicate<PlanNode>` and skips non-matching nodes; `LazySubtreeIterator` carries an explicit depth stack and
stops descending at a depth limit, yielding the sub-plan itself instead of its children for the collapsed UI view.
Clients iterate the tree only through these — never by recursing into children by hand.

**Template Method** — `engine/AbstractLedgerEntryGenerator.generateEntries()` is `final` and fixes the
skeleton: select allocations → validate → create transaction → build withdrawal/deposit → post → `afterPost()`
hook. `postEntries()` and `createTransaction()` are also `final`, so conservation (balanced entries), posting-rule
evaluation and audit logging always run. Three subclasses supply only the varying parts:
`ConsumableLedgerEntryGenerator` (consumable allocations, positive quantity),
`AssetLedgerEntryGenerator` (specific asset allocations, non-null positive time period, hours as the entry
amount via the `entryAmount()` hook), and `ReversalLedgerEntryGenerator` (negated amounts to restore the
pool balance on reopen). Both Week 2 ledger changes were absorbed as new subclasses; the abstract class was
never edited.

**Visitor** — `domain/composite/PlanNodeVisitor` extends the Composite: leaves call `visitLeaf(this)`,
composites call `visitComposite(this)` and then `accept(v)` on each child. `CompletionRatioVisitor`,
`ResourceCostVisitor` and `RiskScoreVisitor` compute completion percentage, `quantity × unitCost`, and a
count of suspended/abandoned leaves, all behind `GET /api/plans/{id}/metrics`, which works for any node id —
sub-plan or leaf. Adding a metric means adding a visitor, not touching the node classes.

*(`engine/LedgerEntryEngine` is a thin facade the manager calls — `generate()` runs the consumable then the
asset generator on completion, `generateReversal()` runs the reversal generator on reopen — keeping
`ActionManager` free of generator sequencing and preserving the "engines do not call engines" rule.)*

## Run locally

```bash
docker compose up --build
```

Brings up PostgreSQL 16 (host port **5433**) and the backend on **8080**, with schema and seed data applied
on start. Backend alone, against a local PostgreSQL:

```bash
cd backend && mvn spring-boot:run
```

Then open `frontend/dashboard.html`, `plan-view.html`, `action-detail.html`, `ledger-view.html`,
`report.html` — the single `API_BASE` line in [frontend/config.js](frontend/config.js) points the UI at the backend.

## Docker

```bash
docker build -t rpl backend
```

```bash
docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/rpl rpl:latest
```

## Tests

```bash
cd backend && mvn test
```

79 JUnit 5 + Mockito unit tests (no `@SpringBootTest`) covering every legal and illegal state transition, the
composite `getStatus()` derivation, all three iterators' traversal order, the consumable/asset/reversal
generators, the posting rule, and the three visitors.

## Stack

Java 17 · Spring Boot 3.x (Web, Validation, Data JPA) · PostgreSQL 16 · Hibernate · Maven ·
multi-stage Docker + Docker Compose · plain HTML/CSS/JS frontend.

## API

| Method | Path | Description |
|---|---|---|
| GET/POST/PUT/DELETE | `/api/protocols` | Protocol catalogue (`POST /{id}/steps` adds a step) |
| GET/POST/DELETE | `/api/resource-types` | Resource-type catalogue |
| POST | `/api/plans` | Create a plan, optionally from a protocol |
| GET | `/api/plans/{id}` | Plan tree with derived statuses (optional depth limit) |
| GET | `/api/plans/{id}/report` | Depth-first summary report (optional status filter) |
| GET | `/api/plans/{id}/metrics` | Completion ratio, resource cost, risk score for any node |
| POST | `/api/actions/{id}/implement`, `/complete`, `/suspend`, `/resume`, `/abandon` | Lifecycle transitions |
| POST | `/api/actions/{id}/submitforapproval`, `/approve`, `/reject`, `/reopen` | Approval gate and reopen flow |
| GET | `/api/actions/{id}/suspensions` | Suspension history for an action |
| POST | `/api/actions/{id}/allocations` | Attach a resource allocation |
| GET | `/api/accounts` | All accounts with balances |
| GET | `/api/accounts/{id}/entries` | Ledger entries with booked and charged dates |
| GET | `/api/audit-log` | Audit trail |
