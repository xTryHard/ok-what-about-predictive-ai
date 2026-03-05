# predictive-ai-rl-bandit

Contextual multi-armed bandit for adaptive offer optimization — demonstrates reinforcement learning on the JVM using Tribuo regression models.

## What It Does

Simulates a telecom company choosing which retention offer to present to at-risk customers. A contextual bandit learns from customer responses and adapts its strategy over time, compared against a random baseline.

## How It Works

### The Problem

Given a customer (context), choose one of four offers (actions) to maximize acceptance rate:
- `DISCOUNT_5` — 5% discount
- `DISCOUNT_10` — 10% discount
- `FREE_ADDON` — Free add-on service
- `LOYALTY_POINTS` — Loyalty points reward

### The Approach: Epsilon-Greedy Contextual Bandit

`TribuoBanditPolicy` maintains a **separate Tribuo regression model per action**. Each model predicts the expected reward for its action given a customer's features.

**Exploration vs. Exploitation:**
- With probability epsilon, choose a random action (explore)
- Otherwise, run all models and choose the action with the highest predicted reward (exploit)

**Online Learning:**
- After each interaction, the observed reward is added to the winning action's dataset
- Models are retrained periodically as new data accumulates

### The Environment

`SyntheticTelecomRewardModel` simulates customer responses with heuristic rules based on tenure, contract type, and monthly charges. Different customer segments respond better to different offers.

### The Simulation

`BanditSimulator` runs a configurable number of steps, comparing the bandit policy against a `RandomPolicy` baseline. It tracks cumulative reward, acceptance rates, and offer distribution over time.

## Key Classes

| Class | Purpose |
|-------|---------|
| `Policy` | Interface: `OfferAction choose(Customer customer)` |
| `BanditPolicy` | Extends `Policy` with `update(customer, action, reward)` |
| `TribuoBanditPolicy` | Epsilon-greedy contextual bandit with per-action Tribuo regression |
| `RandomPolicy` | Uniform random action selection (baseline) |
| `OfferAction` | Enum of four offer types |
| `RewardModel` | Interface: `RewardOutcome evaluate(customer, action)` |
| `SyntheticTelecomRewardModel` | Heuristic reward model simulating customer behavior |
| `BanditSimulator` | Runs simulation and collects metrics |
| `BanditSimulationResult` | Final results: avg reward, acceptance rate for both policies |
| `CustomerContextVectorizer` | Converts Customer features to `double[]` for regression |
| `TelecomCustomerStream` | Generates synthetic customer stream |
| `RewardOutcome` | Record: `reward` + `accepted` flag |

## Dependencies

- Tribuo 4.3.2 (`tribuo-all` — regression trainers)
- `predictive-ai-core`

## Demo Flow

From the main menu, choose **8) RL: Adaptive Offer Optimization (Bandit)** to run the simulation. The output shows:
- Step-by-step snapshots comparing bandit vs. random reward
- Offer distribution shifts as the bandit learns
- Final acceptance rates and average rewards for both strategies
