# Testing Report for kkFileView -- Part 3
#### Group 5 kkFileView
Member Name: Xinyi Xu </br>
Github Link: https://github.com/Po11uxx/kkFileView

## 1. Structural (White Box) Testing

### 1.1 Structural Testing Definition
Structural testing, also called **white-box testing**, is a testing approach that derives test cases from the software’s internal implementation structure rather than only from external requirements. In practice, this means designing tests to execute internal statements, branches, methods, and decision paths in source code.

### 1.2 Importance of Structural Testing Important
* It verifies internal control-flow correctness in utility and backend logic.
* It exposes untested conditional branches, especially error-handling and edge-case paths.
* It provides quantitative evidence of test adequacy through coverage metrics.
* It complements functional/partition testing from earlier assignments by validating implementation-level behavior.

For kkFileView project, this is especially valuable since many core modules include complex file/path checks, URL parsing/encoding logic, and branching behavior that can silently fail if not explicitly tested.

## 2. Baseline Coverage Measurement (Before Adding New Tests)

### 2.1 Coverage Tool
Coverage was measured using **JaCoCo**.

### 2.2 Baseline Test Suite Used
To establish the baseline before adding any new structural tests, the existing utility test class was executed:
* `cn.keking.utils.WebUtilsTests`

### 2.3 Baseline Coverage Results

| Metric | Covered | Total | Coverage |
|---|---:|---:|---:|
| Line | 8 | 7018 | 0.11% |
| Branch | 3 | 1542 | 0.19% |
| Method | 2 | 683 | 0.29% |

### 2.4 Examples of Uncovered Code in Baseline
Significant parts of the codebase were uncovered at baseline, including:
* `server/src/main/java/cn/keking/service/FileHandlerService.java` (core file handling/caching logic)
* `server/src/main/java/cn/keking/service/OfficeToPdfService.java` (office conversion logic)
* `server/src/main/java/cn/keking/utils/KkFileUtils.java` (file/path validation and filesystem operations)
* `server/src/main/java/cn/keking/utils/UrlEncoderUtils.java` (URL-encoding branch logic)
* Most branches and methods in `server/src/main/java/cn/keking/utils/WebUtils.java`

## 3. New Structural Test Cases Added

To improve coverage in a meaningful and stable way, I added focused white-box unit tests for utility logic with many branch points.

### 3.1 New Test Files
* `server/src/test/java/cn/keking/utils/WebUtilsStructuralTests.java`
* `server/src/test/java/cn/keking/utils/KkFileUtilsStructuralTests.java`
* `server/src/test/java/cn/keking/utils/UrlEncoderUtilsStructuralTests.java`

### 3.2 What Functionality These Tests Cover

| Test Class | Covered Implementation Areas | What Is Verified |
|---|---|---|
| `WebUtilsStructuralTests` | `WebUtils` URL/file parsing, filename encoding, request-source extraction, base64 decoding, session attribute helpers | Valid/invalid URL handling, encoded parameter extraction, filename extraction for URL/file schemes, `getSourceUrl` precedence across request params, session read/write/remove behavior |
| `KkFileUtilsStructuralTests` | `KkFileUtils` illegal filename checks, numeric checks, protocol checks, suffix/escape helpers, file deletion, directory deletion, existence checks, upload filtering | Path traversal patterns, numeric regex decisions, http/file/ftp protocol branches, recursive delete behavior, file deletion success/failure paths |
| `UrlEncoderUtilsStructuralTests` | `UrlEncoderUtils.hasUrlEncoded` | Encoded vs non-encoded strings, valid/invalid `%XX` sequences, ASCII/non-ASCII path decisions |

### 3.3 Structural Testing Strategy
The new tests were designed to intentionally exercise:
* both true/false outcomes of core conditionals;
* multiple edge cases per branch-heavy helper;
* filesystem behavior for success and failure outcomes;
* parser/decoder branches for normal and malformed inputs.

## 4. Coverage After Adding New Tests

### 4.1 Test Suite Used for After Measurement
* `cn.keking.utils.WebUtilsTests`
* `cn.keking.utils.WebUtilsStructuralTests`
* `cn.keking.utils.KkFileUtilsStructuralTests`
* `cn.keking.utils.UrlEncoderUtilsStructuralTests`

### 4.2 Coverage Results After Improvement

| Metric | Covered | Total | Coverage |
|---|---:|---:|---:|
| Line | 211 | 7018 | 3.01% |
| Branch | 105 | 1542 | 6.81% |
| Method | 37 | 683 | 5.42% |

### 4.3 Before vs After Comparison

| Metric | Before | After | Delta |
|---|---:|---:|---:|
| Covered Lines | 8 | 211 | **+203** |
| Covered Branches | 3 | 105 | **+102** |
| Covered Methods | 2 | 37 | **+35** |

The line-coverage increase (**+203 lines**) exceeds the assignment target of at least 50 additional covered lines.

## 5. Key Covered Areas by New Tests

### 5.1 `WebUtils` Improvements
* URL normalization and filename extraction scenarios
* URL parameter extraction logic
* file-name encoding logic and no-extension paths
* source URL selection logic from request parameters (`url`, `currentUrl`, `urlPath`, `urls`)
* session helper methods and their null/missing-key paths

### 5.2 `KkFileUtils` Improvements
* illegal filename detection patterns
* integer string classification branches
* protocol detection for http/file vs ftp
* filesystem operations (`deleteFileByName`, `deleteDirectory`, `deleteFileByPath`, `isExist`)

### 5.3 `UrlEncoderUtils` Improvements
* branch decisions in `hasUrlEncoded` for legal and illegal URL-encoded patterns
* `%` handling with valid uppercase hex vs invalid lower-case/incomplete cases

## 6. Commands Used
```bash
mvn test -Dtest=WebUtilsStructuralTests
mvn test -Dtest=KkFileUtilsStructuralTests
mvn test -Dtest=UrlEncoderUtilsStructuralTests
```
**Expect Results:** All tests pass if the application correctly handles each partition.