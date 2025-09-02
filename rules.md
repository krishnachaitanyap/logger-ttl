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
Rule 1: Redundant Logs

Instruction: Detect duplicate or unnecessary logs.

Criteria:

Logs that repeat the same variable/message in the same method/class.

Multiple logs without contextual difference.

Example:

log.info("User created successfully");
log.info("User created successfully");


Expected Output:

{
  "rule": "Redundant Log",
  "line": 23,
  "message": "Duplicate log with same text",
  "recommendation": "Remove one of the logs"
}

Rule 2: Incorrect Log Level

Instruction: Ensure logs use the appropriate log level.

Criteria:

ERROR: for exceptions, failures, unexpected conditions.

WARN: for deprecated features, recoverable issues.

INFO: for high-level flow, successful ops.

DEBUG: for variable values, state changes.

TRACE: for very fine-grained details.

Example (incorrect):

try {
    saveUser(user);
} catch (Exception e) {
    log.info("Failed to save user", e); // Wrong
}


Expected Output:

{
  "rule": "Incorrect Log Level",
  "line": 56,
  "message": "Exception logged at INFO instead of ERROR",
  "recommendation": "Use log.error for exceptions"
}

Rule 3: Security-Sensitive Logs

Instruction: Detect logs that may leak sensitive data.

Criteria:

Logs containing passwords, tokens, API keys, PII.

Full object dumps that may contain confidential data.

Example (bad):

log.debug("User login request: " + request.toString());


Expected Output:

{
  "rule": "Security Risk",
  "line": 77,
  "message": "Possible sensitive data (request object) logged",
  "recommendation": "Avoid logging full objects containing user data"
}

Rule 4: High-Frequency Logs

Instruction: Detect logs inside tight loops or frequent paths.

Criteria:

Logging inside for/while loops.

Logging in methods called at high TPS (e.g., filters, interceptors).

Example (bad):

for (Item item : items) {
    log.debug("Processing item: " + item.getId());
}


Expected Output:

{
  "rule": "High Frequency Log",
  "line": 33,
  "message": "Log inside loop may flood logs under load",
  "recommendation": "Aggregate or sample logs instead of per-item logging"
}

Rule 5: Placeholder Misuse

Instruction: Detect inefficient string concatenation in logs.

Criteria:

Avoid "log.debug("Value: " + obj)"

Prefer "log.debug("Value: {}", obj)"

Example (bad):

log.debug("Processing item " + item.getId());


Expected Output:

{
  "rule": "Inefficient Log Construction",
  "line": 21,
  "message": "String concatenation in log statement",
  "recommendation": "Use parameterized logging instead"
}
---
