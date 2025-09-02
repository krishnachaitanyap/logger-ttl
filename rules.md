# Java Log Optimization Rules

This repository defines a set of **log optimization rules** for Java codebases.  
The goal is to improve **observability**, reduce **noise**, and ensure **security and performance** in production.

---

## 📌 Why Optimize Logs?
- Avoid log noise that hides critical issues.
- Prevent leakage of sensitive data (passwords, tokens, PII).
- Reduce infrastructure costs (storage, ingestion in ELK/Splunk/Datadog).
- Make logs actionable with proper context.
- Improve performance by avoiding unnecessary log overhead.

---

## 🚀 Rules

### 1. Redundant Logs
- **Description**: Avoid duplicate log messages that provide no new context.  
- **Example**:
  ```java
  log.error("Exception occurred", e);
  log.error("Exception: " + e); // ❌ Redundant
  ```

---

### 2. Incorrect Log Levels
- **Description**: Use correct severity levels:
  - `ERROR`: System failure or unrecoverable state.
  - `WARN`: Recoverable problems, deprecated usage.
  - `INFO`: Major lifecycle events, startup, shutdown.
  - `DEBUG`: Developer troubleshooting, variable states.
  - `TRACE`: Fine-grained debugging.
- **Example**:
  ```java
  log.info("Null pointer exception occurred"); // ❌ Wrong
  log.error("Null pointer exception occurred", e); // ✅ Correct
  ```

---

### 3. Security-Sensitive Data in Logs
- **Description**: Do not log passwords, API keys, tokens, or PII.  
- **Example**:
  ```java
  log.debug("User password: " + password); // ❌ Never log
  ```

---

### 4. High-Frequency Logs
- **Description**: Prevent excessive logs in loops or high TPS methods.  
- **Example**:
  ```java
  for (Item item : items) {
      log.info("Processing item: " + item.getId()); // ❌ Noisy
  }
  ```
- **Fix**: Use aggregated logging (`Processed {} items`).

---

### 5. Unstructured Logs
- **Description**: Logs should be structured with placeholders.  
- **Example**:
  ```java
  log.info("Start processing order " + orderId); // ❌
  log.info("order_processing_started orderId={} userId={}", orderId, userId); // ✅
  ```

---

### 6. Missing Correlation IDs
- **Description**: Always include correlation IDs (trace ID, request ID).  
- **Fix**: Use **MDC (Mapped Diagnostic Context)** to auto-inject.

---

### 7. Logs Without Context
- **Description**: Logs must be actionable.  
- **Example**:
  ```java
  log.error("Transaction failed"); // ❌ Not useful
  log.error("Transaction failed for userId={} orderId={}", userId, orderId); // ✅
  ```

---

### 8. Time-Bound Logs (TTL)
- **Description**: Temporary logs for debugging should expire after a set duration.  
- **Fix**: Add config-based TTL logic for temporary logs.

---

### 9. Production Feedback Loop
- **Description**: Regularly analyze production logs for:
  - Overused `DEBUG` or `INFO` logs.
  - Repetitive error patterns.
- **Fix**: Remove or downgrade noisy logs; consolidate repetitive errors.

---

### 10. Performance-Sensitive Logging
- **Description**: Avoid expensive log operations when log level is disabled.  
- **Example**:
  ```java
  log.debug("User data: " + expensiveToString(user)); // ❌ Always evaluated
  log.debug("User data: {}", user); // ✅ Lazy evaluation
  ```

---

## ✅ Benefits
- Lower logging costs.
- Improved debugging and monitoring.
- Increased developer productivity.
- Stronger security posture.
- Cleaner dashboards in Splunk/ELK/Datadog.

---

## 🔧 Next Steps
- Convert these rules into **SonarQube custom rules** for CI/CD checks.
- Add **log linting scripts** for static analysis.
- Integrate into **LogOptimizer AI** to continuously refine rules from production feedback.

---

---

# AI Log Optimization Rules Prompt for Java Repositories

This document defines detailed rules for analyzing **Java code repositories** to identify logging-related issues.  
Each rule is structured with:
- **Description**: What the rule checks.
- **Rationale**: Why it is important.
- **Detection Method**: How to identify it in code/logs.
- **Example (Bad vs Good)**: Illustrations.
- **Expected Output Format**: JSON output for interpretation.

---

## Rule 1: Redundant Logs

**Description:** Detect duplicate or unnecessary log statements that provide no new information.  
**Rationale:** Redundant logs increase noise, storage costs, and make debugging harder.  
**Detection Method:**  
- Identify repeated log statements within the same method/class.  
- Detect consecutive logs with similar text.  

**Example:**  
```java
// Bad
logger.info("Starting process");
logger.info("Process started");

// Good
logger.info("Process started successfully");
```

**Expected Output (JSON):**  
```json
{
  "file": "Example.java",
  "line": 12,
  "issue": "Redundant log detected",
  "recommendation": "Merge duplicate log statements into one meaningful log."
}
```

---

## Rule 2: Incorrect Log Level

**Description:** Ensure logs are written at the correct severity level.  
**Rationale:** Wrong log levels mislead monitoring systems and can hide critical issues.  
**Detection Method:**  
- Match keywords (`exception`, `error`, `fail`) with `ERROR`.  
- Match state-change or workflow messages with `INFO`.  
- Debugging details with `DEBUG`.  

**Example:**  
```java
// Bad
logger.debug("Database connection failed: " + e.getMessage());

// Good
logger.error("Database connection failed", e);
```

**Expected Output (JSON):**  
```json
{
  "file": "DatabaseService.java",
  "line": 45,
  "issue": "Incorrect log level",
  "recommendation": "Use ERROR instead of DEBUG for failures."
}
```

---

## Rule 3: Security Risk Logs

**Description:** Detect logs that expose sensitive information (passwords, tokens, keys, PII).  
**Rationale:** Logging sensitive data violates compliance (GDPR, PCI DSS) and is a security risk.  
**Detection Method:**  
- Regex patterns for `password`, `secret`, `token`, `key`.  
- Detect `logger` calls inside authentication flows.  

**Example:**  
```java
// Bad
logger.info("User logged in with password: " + password);

// Good
logger.info("User logged in successfully for userId=" + userId);
```

**Expected Output (JSON):**  
```json
{
  "file": "AuthService.java",
  "line": 78,
  "issue": "Sensitive data logged",
  "recommendation": "Do not log passwords, tokens, or PII."
}
```

---

## Rule 4: High-Frequency Logs

**Description:** Detect logs inside loops, recursion, or high-throughput code paths.  
**Rationale:** These logs flood log systems and increase costs.  
**Detection Method:**  
- Identify log statements inside `for`, `while`, or recursive methods.  
- Highlight logs in critical request paths.  

**Example:**  
```java
// Bad
for (Item item : items) {
    logger.info("Processing item: " + item.getId());
}

// Good
logger.info("Processing " + items.size() + " items");
```

**Expected Output (JSON):**  
```json
{
  "file": "OrderProcessor.java",
  "line": 33,
  "issue": "High-frequency log inside loop",
  "recommendation": "Move log outside loop or summarize."
}
```

---

## Rule 5: Missing Context in Logs

**Description:** Ensure logs provide enough context (correlation IDs, request IDs).  
**Rationale:** Without context, logs are hard to trace across distributed systems.  
**Detection Method:**  
- Detect `logger` calls without request/session ID in API services.  
- Suggest structured logging (JSON).  

**Example:**  
```java
// Bad
logger.info("Order processed");

// Good
logger.info("Order processed successfully, orderId=" + orderId + ", userId=" + userId);
```

**Expected Output (JSON):**  
```json
{
  "file": "OrderService.java",
  "line": 90,
  "issue": "Missing log context",
  "recommendation": "Add orderId or correlationId to logs."
}
```

---

## Rule 6: Expired Logs

**Description:** Logs that should expire after a certain period (e.g., feature rollout, debugging).  
**Rationale:** Prevents stale or unnecessary logs staying forever.  
**Detection Method:**  
- Detect log annotations/configs with expiry date.  
- Flag logs without lifecycle management.  

**Example:**  
```java
// Bad
logger.info("Debug log for feature rollout");

// Good (with expiry metadata)
@LogExpiry("90d")
logger.info("Debug log for feature rollout");
```

**Expected Output (JSON):**  
```json
{
  "file": "FeatureRollout.java",
  "line": 25,
  "issue": "Log without expiry",
  "recommendation": "Add expiry metadata to temporary logs."
}
```

---

# Output Format

For every Java file analyzed, output a JSON array of issues:  

```json
[
  {
    "file": "Example.java",
    "line": 12,
    "issue": "Redundant log detected",
    "recommendation": "Merge duplicate log statements into one meaningful log."
  },
  {
    "file": "DatabaseService.java",
    "line": 45,
    "issue": "Incorrect log level",
    "recommendation": "Use ERROR instead of DEBUG for failures."
  }
]
```

---

# Usage

These rules should be executed by an **AI-assisted log review system**.  
For every file in a Java repository, apply these rules and generate a structured JSON report.  
The report can then be consumed by:  
- CI/CD pipelines for log quality gates.  
- Observability platforms for feedback loops.  
- Developers for improving log hygiene.

---

---
