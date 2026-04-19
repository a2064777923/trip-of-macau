# Phase 23 Design: Reward Domain Split and Acquisition Presentation System

## Objective

Replace the current overloaded reward configuration flow with a three-surface reward control plane:

1. `兌換獎勵物品管理`
2. `遊戲內獎勵配置`
3. `獎勵規則與演出中心`

The result should support composite conditions, reusable reward rules, title-like honors, and cinematic acquisition flows without forcing operators to hand-edit JSON for routine work.

## Information Architecture

### Admin navigation

- `收集物與獎勵`
  - `兌換獎勵物品管理`
  - `遊戲內獎勵配置`
  - `榮譽與稱號`
    - filtered view over `遊戲內獎勵配置`
  - `獎勵規則與演出中心`

### Surface ownership

#### 1. 兌換獎勵物品管理

Owns:
- physical or partner prizes
- voucher and code rewards
- virtual redeemable goods
- stock, quota, and fulfillment
- redemption condition groups

#### 2. 遊戲內獎勵配置

Owns:
- badges
- titles
- city currencies
- city fragments
- unlock passes
- voice packs
- cosmetic or privilege-style rewards
- grant conditions and grant behavior

#### 3. 獎勵規則與演出中心

Owns:
- shared reward rules
- condition groups and condition summaries
- interaction-linked reward rules
- reward-acquisition presentation templates
- conflict and overlap inspection
- enable or disable controls

## Canonical Domain Model

### Top-level entities

#### `redeemable_prize`

Represents an item the user redeems explicitly.

Core fields:
- code
- localized name / subtitle / description
- prize type
- fulfillment mode
- cover asset / gallery / media
- stock policy
- publish status
- scope bindings
- redemption rule references
- operator notes

Prize types:
- merchandise
- postcard
- coupon
- ticket
- code
- virtual item pack

Fulfillment modes:
- offline_pickup
- postal_delivery
- virtual_issue
- voucher_code

#### `game_reward`

Represents a reward granted by gameplay or interactions.

Core fields:
- code
- localized name / description
- reward type
- rarity / display level
- icon / animation / gallery / audio
- stackable flag
- max owned
- can equip
- can consume
- scope bindings
- acquisition rule references
- acquisition presentation reference
- visibility and sorting

Reward types:
- badge
- title
- city_currency
- city_fragment
- unlock_pass
- voice_pack
- cosmetic
- collectible_bonus
- privilege

#### `reward_rule`

Shared rule definition referenced by redeemable prizes, game rewards, or interaction behaviors.

Core fields:
- code
- name
- rule type
- status
- summary text
- source bindings
- condition group root
- effect bindings
- referenced entities

Rule types:
- redemption_rule
- grant_rule
- interaction_grant_rule
- composite_rule

#### `reward_presentation`

Defines what happens visually or audibly when a reward is obtained or redeemed.

Core fields:
- code
- presentation type
- priority level
- first_time_only
- skippable
- minimum_display_ms
- interrupt_policy
- queue_policy
- asset references
- audio references
- subtitle / narration text
- fallback presentation mode

### Supporting entities

- `reward_condition_group`
- `reward_condition`
- `reward_rule_binding`
- `reward_fulfillment_config`
- `reward_stock_policy`
- `reward_inventory_item`
- `reward_redemption_order`
- `reward_code_pool`
- `reward_presentation_step`

## Condition Builder Model

### Group semantics

Each group has:
- `mode = all | any | at_least`
- `requiredCount` when `mode = at_least`
- list of child conditions

One level of nested groups is enough for this phase. Do not allow unlimited nesting.

### Condition families

#### Numeric progress
- stamp count
- city exploration percentage
- sub-map exploration percentage
- indoor exploration percentage
- number of completed interactions
- number of owned fragments

#### Content unlock state
- unlocked scene
- unlocked story chapter
- completed storyline
- completed mission
- owned badge
- owned title
- owned reward fragment

#### Interaction history
- triggered specific interaction behavior
- triggered a behavior group
- visited a POI
- stayed in an indoor scene for N seconds
- reached a map node

#### Time and scope
- time window
- recurring weekday pattern
- limited event period
- city / map / indoor scope

### Operator-facing summary

Every saved rule must generate a human-readable summary, for example:

`澳門探索進度 >= 80%，且已觸發「夜場入口互動」，即可獲得「夜場引路人」稱號。`

## Interaction Synchronization Design

### Non-negotiable rule

Do not duplicate reward-trigger conditions into both interaction JSON and reward JSON.

### Reference flow

- `interaction_behavior` may reference one or more `reward_rule` records
- `game_reward` may reference one or more `reward_rule` records
- `redeemable_prize` may reference one or more `reward_rule` records

### UX implication

- When the operator adds a reward condition from the interaction editor, the UI should:
  - pick an existing rule or quick-create one
  - show that shared rule in the reward rule center
  - show linked interactions in the reward page
- When the operator edits or deletes a linked rule from the reward side:
  - the interaction side should reflect the same rule state immediately on reload
  - deletion should first show affected interactions and rewards

## Acquisition Presentation System

### Presentation modes

- none
- toast
- popup_card
- fullscreen_animation
- fullscreen_video
- fullscreen_video_with_overlay
- sequence

### Presentation assets

- icon
- static background
- animation asset
- video asset
- SFX asset
- voice-over asset
- BGM asset
- subtitle text
- fallback image

### Playback policy

- first-time only
- replay on repeated acquisition
- downgrade repeat acquisition to toast
- skippable
- minimum watch duration
- can interrupt current interaction
- queue after current interaction
- block map input while presenting

### Example

Legendary title acquisition:
- fullscreen video intro
- title emblem animation overlay
- acquisition SFX
- narrated line
- final modal card with equip button

## Fulfillment Design for Redeemable Prizes

### Offline pickup

Fields:
- pickup city
- pickup venue
- pickup schedule
- verification method
- required redemption code

### Postal delivery

Fields:
- address collection required
- supported regions
- shipping fee mode
- dispatch SLA
- tracking reference

### Virtual issuance

Fields:
- direct asset grant
- delayed grant
- unlock target
- account binding policy

### Voucher/code

Fields:
- code pool
- single-use or reusable
- claim timeout
- provider notes

## Admin UI Pattern

### List pages

Use:
- top statistic cards
- horizontal filter bar with explicit labels
- compact but readable result list
- condition summary chips
- right-side detail drawer or step editor

### Redeemable prize filters

- type
- fulfillment mode
- city
- status
- stock state
- limited-time flag

### Game reward filters

- reward type
- rarity
- city
- map / indoor scope
- linked interaction
- status

### Rule center filters

- rule type
- linked reward type
- linked interaction
- condition family
- enabled status
- presentation severity

## API Sketch

### Admin backend

- `GET /api/admin/v1/redeemable-prizes`
- `POST /api/admin/v1/redeemable-prizes`
- `PUT /api/admin/v1/redeemable-prizes/{id}`
- `GET /api/admin/v1/game-rewards`
- `POST /api/admin/v1/game-rewards`
- `PUT /api/admin/v1/game-rewards/{id}`
- `GET /api/admin/v1/reward-rules`
- `POST /api/admin/v1/reward-rules`
- `PUT /api/admin/v1/reward-rules/{id}`
- `GET /api/admin/v1/reward-presentations`
- `POST /api/admin/v1/reward-presentations`
- `PUT /api/admin/v1/reward-presentations/{id}`
- `GET /api/admin/v1/reward-governance/overview`
- `GET /api/admin/v1/reward-governance/conflicts`

### Public/backend alignment

Likely follow-on endpoints or expansions:
- inventory and owned reward summary
- eligible redemption view
- reward acquisition presentation payload
- redemption submission and fulfillment status
- title equip / unequip
- city currency / fragment counters

## Implementation Strategy

### Recommended execution split

If executed later, split into three waves:

1. Domain split and backend contract foundation
2. Admin IA rebuild and operator authoring UX
3. Public/runtime alignment plus rule synchronization and acquisition presentation payloads

### Migration guidance

- Do not mutate the current reward table into an ambiguous hybrid.
- Migrate current records into either `redeemable_prize` or `game_reward`.
- Treat badges and titles as `game_reward` specializations, not separate roots.

## Risks

- If interaction-linked reward rules are duplicated instead of referenced, the system will drift immediately.
- If cinematic acquisition fields are bolted into the reward content table without a presentation model, fullscreen reward flows will become impossible to govern.
- If physical-fulfillment concerns are mixed into in-game rewards, operator UX will become more confusing than the current state.
