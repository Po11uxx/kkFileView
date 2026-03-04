# kkFileView Testing Summary Report
Member Name: Xinyi Xu </br>
Github Link: https://github.com/Po11uxx/kkFileView

## Table of Contents
 - Part 1: Functional Testing and Partitioning
 - Part 2: Functional Testing and Finite State Machines
 - Part 3: White Box Testing and Coverage
 - Part 4: Continuous Integration
 - Part 5: Testable Design and Mocking
 - Part 6: Static Analyzers

---

## Part 1: Functional Testing and Partitioning

## 1. Introduction
### 1.1 Project Overview
The software project under test is kkFileView, an open-source document online preview solution forked from the original repository kekingcn/kkFileView (hosted at https://github.com/kekingcn/kkFileView).
Built on the Spring Boot framework, kkFileView is designed to provide seamless online preview capabilities for a comprehensive range of file formats, spanning office documents, images, audio/video, 
3D models, CAD files, and more. Its core purpose is to eliminate the need for local software installation to view files by rendering them directly in web browsers, making it a valuable tool for web applications,
document management systems, and collaborative platforms.

### 1.2 Key Project Details
* **Languages & Frameworks**: Primarily written in Java, leveraging the Spring Boot framework for backend development. Supplementary technologies include **Freemarker** (template engine), **Redisson** (distributed locking), **Jodconverter** (document conversion), and **ffmpeg** (video transcoding). Frontend components use standard web technologies (HTML/CSS/JavaScript) with libraries like pdf.js for PDF rendering.
* **Code Size**: While exact Lines of Code are not explicitly reported, the project’s structure—including a server module, Docker configuration, documentation, and dependency management via pom.xml—indicates a medium-sized codebase (estimated 20,000–30,000 LOC). 
* **Dependencies**: Optional Redis for caching, OpenOffice/LibreOffice for office document conversion , and ffmpeg for video transcoding.
* **License**: Open-source, enabling free use, modification, and distribution.

## 2. Build and Deployment Process
To set up and run kkFileView locally, the following steps were executed, adhering to the project’s "Quick Start" guidelines and supplementary configuration requirements:
### 2.1 Prerequisites
* Java Development Kit (JDK): Version 8 or higher (tested with JDK 25, as the project supports Java 25 per recent commits).
* Build Tool: Apache Maven (included via pom.xml for dependency management and compilation).
* Optional Dependencies:
  * Redis (for distributed caching, skipped for local testing).
  * LibreOffice/OpenOffice (auto-installed on Ubuntu 22.04; for Windows, bundled with the project).
  * ffmpeg (for video preview, installed via system package manager: sudo apt install ffmpeg on Linux).
### 2.2 Build Steps
#### 1. Fork the Repository     
#### 2. Clone the Forked Repository
```bash
git clone https://github.com/TeamTestLab/kkFileView.git
cd kkFileView
```
#### 3. Resolve Dependencies
Maven automatically downloads required dependencies (Spring Boot, Jodconverter, pdfbox, etc.) when building. No manual dependency resolution was needed.
#### 4.Build the Project
Execute the Maven build command to compile source code and package the application:
bash
```bash
mvn clean package
```
#### 5.Run the Application
For development mode: Execute the main method in **server/src/main/java/cn/keking/ServerMain.java** via an IDE (IntelliJ IDEA/Eclipse) or command line:
```bash 
java -jar server/target/kkFileView-4.4.0.jar
```
For production mode: Use the provided Docker configuration (build and run via Dockerfile), or deploy the JAR file to a server.
### 2.3 Verification
Upon successful startup, the application listens on port 8012. Accessing http://localhost:8012/ in a web browser loads the kkFileView demo homepage, confirming the application is running. The demo page allows uploading files or entering file URLs to test preview functionality.

## 3. Existing Testing Practices and Frameworks
### 3.1 Overview of Existing Tests
A review of the kkFileView codebase automated test coverage, with all existing test cases concentrated in the **server/src/test/java/cn/keking** directory. The test suite consists of three JUnit 5 test classes, each focused on narrow functional areas, as summarized below:
#### 3.1.1 Test Framework & Structure
The project uses JUnit 5 (Jupiter) as its core testing framework:
* WebUtilsTests: Unit tests for URL filename encoding logic in the WebUtils utility class (2 test methods).
* EncodingTests: Integration tests for character encoding detection via the EncodingDetects utility (1 test method).
* ServerMainTests: Minimal integration test to validate Spring Boot application context loading (1 test method).
#### 3.1.2 Test Coverage & Scope
The existing tests are focused on utility functions and basic application initialization, with no coverage of core business logic:

| Test Class | Purpose | Coverage Limitations |
| ----------- |----------- | -------------------- |
| WebUtilsTests | Validate UTF-8 encoding of special characters (e.g., #, &) in filenames within URLs, distinguishing between filename encoding and parameter encoding. | Only tests 2 edge cases for URL encoding; no coverage of other WebUtils methods (e.g., URL decoding, path validation). |
| EncodingTests | Verify automatic detection of file character encodings (e.g., UTF-8, GBK) using 29 test data files in testData/. | Outputs results to console (no assertions) – tests are "observational" rather than validation-focused; relies on external test data files not validated for consistency. |
| ServerMainTests | Confirm the Spring Boot application context loads without errors. | A "smoke test" with no validation of core functionality (e.g., preview API endpoints, file conversion logic). |
#### 3.1.3 Testing Approach and Limitations
* **Manual Testing Dominance**: Core functionality (e.g. file preview for Office/PDF/zip files, format conversion, error handling for corrupted files) relies entirely on manual validation via the demo homepage, community bug reports, and ad-hoc testing. 
* **No Functional/End-to-End Tests**: There are no tests for core user journeys (e.g., uploading a DOCX file → converting to PDF → rendering in the browser) or edge cases (e.g., large files, corrupted files, unsupported formats).

## 4. Systematic Functional Testing and Partition Testing
### 4.1 Key Concepts
* **Systematic Functional Testing**: A methodical process to validate that each feature of the software behaves as specified. It involves defining test cases based on functional requirements, ensuring all user interactions and edge cases are covered.
* **Partition Testing**: The process of dividing the input domain of a feature into valid partitions (inputs that the software is expected to handle correctly) and invalid partitions (inputs that should be rejected or trigger error handling). Partitions are defined based on shared characteristics (e.g., file format, size, encoding). Boundary values (e.g., minimum/maximum supported file size) are often tested alongside representative values to catch edge-case defects.
### 4.2 Partition Testing
Since I'm the whole team myself, the following tests were implemented on my own.
#### 4.2.1 Feature 1: Office Document Preview 
* Feature Description: kkFileView supports previewing mainstream Office formats (DOC, DOCX, XLS, XLSX, PPT, PPTX) and domestic WPS formats (WPS, DPS, ET). The feature converts these files to PDF or images for web rendering.
* Partitioning Scheme: Based on two dimensions: (1) File Format Validity (valid/invalid Office/WPS formats) and (2) File Integrity (intact/corrupted files).

| Partition ID | Partition Description | Type (Valid/Invalid) | Rationale                                                                                                               |
|--------------|-----------------------|----------------------|-------------------------------------------------------------------------------------------------------------------------|
|P1-1	|Valid Office format (e.g., DOCX, XLSX)|	Valid	| These formats are explicitly supported per the project documentation.                                                   |
|P1-2	|Valid WPS format (e.g., WPS, ET)	|Valid	| Domestic WPS formats are a core supported category.                                                                     |
|P1-3	|Invalid Office/WPS format (e.g., TXT, PNG)	|Invalid	| These formats belong to other supported categories (plain text/images) and should not be processed as Office/WPS files. |
|P1-4	|Corrupted valid Office file (e.g., truncated DOCX)	|Invalid	| The software should detect corruption and return an error instead of crashing.                                          |
* Representative and Boundary Values:
  * P1-1: Sample.docx (intact, 168KB, contains text and images) – Representative of valid Office files.
  * P1-2: Sample.wps (intact, 12KB, WPS text document) – Representative of valid WPS files.
  * P1-3: Sample.txt (intact, 4KB, plain text) – Representative of invalid Office/WPS formats.
  * P1-4: Sample.xlsx (truncated, 8KB, incomplete XLSX structure) – Representative of corrupted valid formats.
##### JUnit Test Cases
Test cases were added to [server/src/test/java/cn/keking/OfficePreviewTests.java](https://github.com/Po11uxx/kkFileView/blob/9b567f9bf2b5e5fab92e64ecdb69a00cd21f71d1/server/src/test/java/cn/keking/OfficePreviewTests.java#L12) . The tests use Spring Boot’s @SpringBootTest to load the application context and validate preview responses.
**Test Execution**: Tests are run via Maven
```bash
mvn test -Dtest=OfficePreviewTests
```
**Expect Results:** All tests pass if the application correctly handles each partition.

#### 4.3.2 Feature 2: Image Preview
**Feature Description** : kkFileView supports previewing common image formats (JPG, PNG, GIF, BMP) with client-side interactive transformations—including rotation (90°/180°/270° increments), zoom, and mirroring.
##### Partitioning Scheme
The testing approach focuses on server-side responsibilities like format validation, file delivery, performance. Partitions are defined based on:
* Image Format Validity
* Image Dimensional/Size Characteristics 

| Partition ID | Partition Description  | Type (Valid/Invalid)  | Rationale                                               |
|--------------|------------------------|-----------------------|-----------------------------------------------|
|P2-1	|Valid image format (JPG/PNG/GIF/BMP)	|Valid| 	Core supported formats for front-end preview/rotation. |
|P2-2	|Invalid image format (ZIP/DOCX/XLSX)	|Invalid| 	Non-image formats should display a different preview page. |
|P2-3	|Small image (≤1MB, ≤1920x1080)	|Valid| 	Typical web-friendly size; baseline for performance testing.                 |
|P2-4	|Large image (≥10MB, ≥4000x3000)	|Valid| 	Boundary case for server delivery speed and front-end rendering performance. |
##### JUnit Test Cases
Test cases were added to [server/src/test/java/cn/keking/ImagePreviewTests.java](https://github.com/Po11uxx/kkFileView/blob/4705b72d50eb72394899f7381833e9f08cd861d2/server/src/test/java/cn/keking/OfficeFSMTest.java#L15C1-L15C2) . The tests use Spring Boot’s @SpringBootTest to load the application context and validate preview responses.
**Test Execution**: Tests are run via Maven
```bash
mvn test -Dtest=ImagePreviewTests
```
**Expect Results:** All tests pass if the application correctly handles each partition.

---

## Part 2: Functional Testing and Finite State Machines

## 1. Finite Models for Testing

### 1.1 Value of Finite Models in Software Testing
**Finite models**, Finite State Machines (FSMs), provide a structured, mathematical framework to model software behavior 
as a set of discrete states, transitions between states, and triggering events. It can be useful for testing in several ways as follows.

#### Reducing Ambiguity in Test Scope
FSMs formalize expected behavior that would otherwise be left to manual testing guesswork. 
This eliminates "test coverage gaps" by explicitly mapping all possible state transitions for a feature.

#### Enabling Systematic Test Design
Instead of writing arbitrary test cases, FSMs drive transition coverage, testing every possible state change, 
and state coverage, verifying the system is in the correct state after each event.

#### Simplifying Regression Detection
When the FSM of a feature is well-documented, changes to code can be cross-referenced against the model to identify unintended 
state transitions. This makes regression testing more targeted and efficient.

## 2. FSM Modeling Instantiation in kkViewFile
Among all the core functions in kkFileView, I selected the **Office file preview** workflow—which processes DOCX, XLSX, and PPTX files 
by converting them to PDF and then rendering the final preview—as the most suitable candidate for finite state machine modeling.

### 2.1 Office File Preview Workflow
The Office file preview workflow is chosen for FSM modeling because: 
* **Discrete, observable states:** "Idle", "File Validated", "Preview Success", "Error".
* **Well-defined events:**  "submit preview request", "conversion complete", "file corrupted".
* Core business feature with high user impact.
* Lacks systematic testing.
* Transitions are non-trivial.

### 2.2 FSM definition of Office File Preview
| Component	| Description                                                                                                                                                                                                                  |
| --------- |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| States (S)	| Idle, File Validated, Conversion In Progress, Preview Success, Error                                                                                  | 
| Events (E)	| Submit Preview Request, Validation Passed, Validation Failed, Conversion Started, Conversion Succeeded, Conversion Failed, Corrupted File Detected, Duplicate Request Detected, Close Preview, Retry Preview | 
| Transitions (T)	| Rules for state changes                                                                                                                                             | 
| Initial State	| Idle                                                                                                                                                                          | 
| Final States	| Preview Success, Error, Idle                                                                                                                                                                                                 |

#### FSM Behavior Explanation
**Core States**
* Idle: The system is ready to accept a new preview request when there's no ongoing operations.
* File Validated: The uploaded file is a supported Office format (DOCX/XLSX/PPTX) and passes basic integrity checks like not empty or truncated.
* Conversion In Progress: LibreOffice is converting the Office file to PDF.
* Preview Success: PDF conversion is complete, and the preview page is rendered for the user.

**Error Substates**
* Invalid Format Error: File is not a supported Office format (ZIP, TXT).
* Corrupted File Error: File is a supported format but structurally.
* Conversion Failed Error: Valid file but conversion to PDF fails.
* Duplicate Request Error: New preview request for a file already in conversion.

**Key Transitions**
* Idle → File Validated: Triggered when a user submits a preview request.
* File Validated → Conversion In Progress: Triggered when the system starts PDF conversion, only if file is valid and uncorrupted.
* Conversion In Progress → Preview Success: Triggered when LibreOffice returns a valid PDF file.
* Any Error → Idle: Triggered by "Close Preview" (user action) or "Retry Preview" (only for recoverable errors like Conversion Failed).
* Duplicate Request Error → ConversionInProgress: Retry after a 2-second delay to avoid race conditions with ongoing conversion.

#### Visual FSM Diagram
![Visual FSM Diagram](visualFSM.png)

## 3 Test Cases Based on FSM of Office File Preview
#### Partitioning Scheme
Based on the above analysis of Office File Preview, I split 7 transition cases to be tested in the following Junit tests.

| Test Case ID	 | Test Scenario	| Key Assertions                                                                                   |
|---------------| -------------- |--------------------------------------------------------------------------------------------------|
| TC-1	        | Valid DOCX → Download → Preview Success	| 1. File exists in FILE_DIR (Download success) 2. No password prompt 3. PDF preview page returned |
| TC-2	        | Cache Hit → Skip Download	| 1. File exists 2. Timestamp unchanged (no re-download) 3. Preview success                        |
| TC-3	        | Compressed File → Skip Download	| 1. File does NOT exist in FILE_DIR 2. Response contains zip preview container                    |
| TC-4	        | Password-Protected File → Download Success + Password Prompt	| 1. File exists (Download success) 2. Response contains "needFilePassword"                        |
| TC-5	        | Force Cache Update → Re-Download	| 1. File timestamp changes 2. Preview success after re-download                                   |
| TC-6	        | XLSX Web Preview → No PDF Conversion	| 1. XLSX should be downloaded.                                                                    |
| TC-7	        | Non-Existent File → Download Failed	| 1. Fail to download 2. File does NOT exist                                                       |

##### JUnit Test Cases
Test cases were added to [server/src/test/java/cn/keking/OfficeFSMTest.java](https://github.com/Po11uxx/kkFileView/blob/9b567f9bf2b5e5fab92e64ecdb69a00cd21f71d1/server/src/test/java/cn/keking/ImagePreviewTests.java#L11) . The tests use Spring Boot’s @SpringBootTest to load the application context and validate preview responses.
**Test Execution**: Tests are run via Maven
```bash
mvn test -Dtest=OfficeFSMTest
```
**Expect Results:** All tests pass if the application correctly handles each partition.

---

## Part 3: White Box Testing and Coverage

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
Coverage was substantially improved for:
* URL normalization and filename extraction scenarios
* URL parameter extraction logic
* file-name encoding logic and no-extension paths
* source URL selection logic from request parameters (`url`, `currentUrl`, `urlPath`, `urls`)
* session helper methods and their null/missing-key paths

### 5.2 `KkFileUtils` Improvements
Coverage was substantially improved for:
* illegal filename detection patterns
* integer string classification branches
* protocol detection for http/file vs ftp
* filesystem operations (`deleteFileByName`, `deleteDirectory`, `deleteFileByPath`, `isExist`)

### 5.3 `UrlEncoderUtils` Improvements
Coverage was substantially improved for:
* branch decisions in `hasUrlEncoded` for legal and illegal URL-encoded patterns
* `%` handling with valid uppercase hex vs invalid lower-case/incomplete cases

## 6. Commands Used

Baseline / improved structural test execution:
```bash
mvn -q -o -pl server -Dtest=cn.keking.utils.WebUtilsTests test
mvn -q -o -pl server -Dtest=cn.keking.utils.WebUtilsTests,cn.keking.utils.WebUtilsStructuralTests,cn.keking.utils.KkFileUtilsStructuralTests,cn.keking.utils.UrlEncoderUtilsStructuralTests test
```

Coverage data was then collected with JaCoCo runtime agent and summarized from the generated execution data.

## 7. Conclusion
This assignment strengthened kkFileView’s structural test quality by moving from minimal internal-path coverage to meaningful branch and method coverage in critical utility logic.  
The resulting test suite now validates significantly more internal behavior and provides stronger regression confidence for future code changes.

---

## Part 4: Continuous Integration
## 1. Continuous Integration (CI)

### 1.1 What Continuous Integration Is
Continuous Integration (CI) is the development practice of frequently integrating code changes into a shared repository and automatically running build and test pipelines on each push/pull request.

### 1.2 Purpose of CI
* Detect integration problems early.
* Prevent broken code from being merged into the main branch.
* Provide automatic and repeatable build/test verification.
* Reduce manual effort for regression checking.

For kkFileView, CI is especially important because the project combines Spring Boot backend logic, file-preview processing, and many utility modules. Automated checks help ensure code quality remains stable as features evolve.

## 2. CI Platform Used
The project uses **GitHub Actions** as the cloud CI platform.

Configuration file:
* `.github/workflows/maven.yml`

## 3. CI Pipeline Design

### 3.1 Trigger Conditions
The workflow is triggered on:
* push to `master` or `main`
* pull request to `master` or `main`
* manual dispatch (`workflow_dispatch`)

### 3.2 CI Jobs
The workflow contains two jobs:

1. `test`
* Installs JDK 21
* Runs stable automated tests:
  * `cn.keking.utils.WebUtilsTests`
  * `cn.keking.utils.WebUtilsStructuralTests`
  * `cn.keking.utils.KkFileUtilsStructuralTests`
  * `cn.keking.utils.UrlEncoderUtilsStructuralTests`
* Uploads `surefire-reports` as CI artifacts

2. `build`
* Depends on `test` success (`needs: test`)
* Builds package with Maven (`-DskipTests package`)
* Uploads generated package artifacts from `server/target`

## 4. Commands Used for Verification

### 4.1 Local Test Command 
```bash
mvn -B -o -pl server -Dtest=cn.keking.utils.WebUtilsTests,cn.keking.utils.WebUtilsStructuralTests,cn.keking.utils.KkFileUtilsStructuralTests,cn.keking.utils.UrlEncoderUtilsStructuralTests test
```

### 4.2 Full Local Test Command 
```bash
mvn -B -o -pl server test -DskipTests=false
```

### 4.3 Local Build Command (Offline Limitation Encountered)
```bash
mvn -B -o -DskipTests package
```

## 5. Verification Results

### 5.1 Stable Test Suite Result
* Status: **PASS**
* Total tests run: **14**
* Failures: **0**
* Errors: **0**

### 5.2 Full Test Suite Result
* Status: **FAIL** in current local environment
* Main issues observed:
  * LibreOffice/JODConverter process startup failures (`exit code 134`) for integration tests.
  * Mockito plugin initialization error (`Could not initialize plugin: org.mockito.plugins.MockMaker`) in some Spring Boot test contexts.

### 5.3 Offline Build Result
* Status: **FAIL** in current local environment due missing Maven plugin artifacts in offline mode (`-o`).
* This is an environment/cache limitation rather than a source-code compile error.

## 6. Issues Encountered and How They Were Handled

### 6.1 Issue: Environment-dependent integration tests
Some existing tests depend on native/OS runtime behavior (LibreOffice process startup). This can fail depending on local host setup.

Handling:
* CI test stage currently targets stable utility/structural tests to provide reliable signal on each commit.
* Full integration tests are documented as known environment-sensitive tests.

### 6.2 Issue: Offline Maven artifact availability
Running build in offline mode failed because some plugin artifacts were not pre-cached locally.

Handling:
* GitHub Actions runs online with Maven cache support, so required artifacts can be restored/downloaded.

## 7. Screenshots

1. Workflow run summary (both jobs visible)
   ![CI Run Summary](./ci-run-summary.png)

2. `test` job log showing test execution and pass result
   ![CI Test Job](./ci-test-pass.png)

3. `build` job log showing successful packaging
   ![CI Build Job](./ci-build-pass.png)

4. Uploaded artifacts list (`surefire-reports`, package artifacts)
   ![CI Artifacts](./ci-artifacts.png)


## 8. Conclusion
This assignment adds a concrete CI pipeline for kkFileView using GitHub Actions. The workflow now automatically runs stable automated tests and then builds package artifacts for every relevant code change, improving integration safety and development efficiency.

---

## Part 5: Testable Design and Mocking

## 1. Testable Design

### 1.1 What makes a design testable
A testable design usually has the following goals:
* **Dependency isolation**: business logic depends on interfaces or overridable collaborators instead of hardwired global/static resources.
* **Deterministic execution**: logic can be executed in small, finite units without long sleeps, infinite loops, or uncontrolled threads.
* **Observable behavior**: outputs, state changes, and collaborator interactions can be verified with assertions.
* **Low setup cost**: tests do not require heavy runtime infrastructure (network, external processes, or full Spring context) unless explicitly needed.

### 1.2 Existing stubbing example in this project
Existing stubbing appears in:
* `server/src/test/java/cn/keking/utils/WebUtilsStructuralTests.java`

Example:
* `MockHttpServletRequest` is used in `getSourceUrlFromRequestVariants()` and `multipartAndSessionAccessChecks()`.
* This class is a test double implementing request behavior needed by `WebUtils.getSourceUrl(...)`, `WebUtils.setSessionAttr(...)`, etc.
* Why used: it avoids running a real servlet container while still providing controllable request/session inputs.

### 1.3 New stubbing-based test implemented
New test file:
* `server/src/test/java/cn/keking/service/FileConvertQueueTaskStubbingTests.java`

What is stubbed:
* `CacheService.takeQueueTask()` is stubbed by `StubCacheService` (interface stub).
* `FilePreviewFactory.get(...)` is stubbed by `StubFilePreviewFactory` (subclass stub).
* `FileHandlerService.getFileAttribute(...)` is stubbed by `StubFileHandlerService` (subclass override).

Result:
* The test executes queue conversion logic using stubs instead of real queue/cache/conversion pipeline.
* It verifies conversion path invocation (`runOnce_shouldUseStubbedDependenciesForOfficeConversion`) and non-conversion path skipping (`runOnce_shouldSkipPreviewForTypeWithoutConversion`).

### 1.4 Bad testable design
#### Bad Design Identified
* `FileConvertQueueTask.ConvertTask.run()` originally contained an infinite `while(true)` loop and direct `sleep(10s)` in exception handling.
* This makes unit testing difficult because tests cannot safely run one deterministic cycle and may block on timing.
* This prevents reliable tests for one-cycle queue behavior.
* This also prevents fast failure-path tests without real waiting.

#### Opinions on Improvement
* Split infinite-loop orchestration from single-step business logic.
* Keep sleep behavior in an overridable hook.

#### Implemented Improved Version
* Updated `server/src/main/java/cn/keking/service/FileConvertQueueTask.java`
* Added:
  * `runOnce()` for one deterministic processing step.
  * `sleepOnFailure()` hook.
* Kept original logic comments in `run()` and preserved behavior by calling `runOnce()` inside the loop.

#### New Test for Newly Testable Functionality
* `runOnce_shouldBeDeterministicWhenQueueThrows()` verifies failure handling with overridden `sleepOnFailure()` and no real 10-second delay.

## 2. Mocking

### 2.1 Definition and Advantages of Mocking
Mocking creates programmable collaborator objects (e.g., with Mockito) to:
* verify **interaction behavior** (who was called, with what parameters, how many times),
* simulate branches/exceptions hard to reproduce with real dependencies,
* isolate the unit under test from heavy infrastructure.

### 2.2 Feature selected for mocking
#### Selected feature
* `OfficeFilePreviewImpl.getPreviewType(...)`
* File: `server/src/main/java/cn/keking/service/impl/OfficeFilePreviewImpl.java`

#### Why mocking is suitable
* The method delegates to `FileHandlerService.pdf2jpg(...)` and fallback `OtherFilePreviewImpl.notSupportedFile(...)`.
* Without mocking, tests would require real PDF conversion/filesystem setup and cannot precisely verify collaborator interactions.

### 2.3 Mockito test implemented
#### New test file
* `server/src/test/java/cn/keking/service/impl/OfficeFilePreviewMockingTests.java`

#### Test cases
1. `getPreviewType_shouldCallPdf2jpgAndPopulateModel`
* Uses Mockito to mock `FileHandlerService` and `OtherFilePreviewImpl`.
* Verifies `pdf2jpg(...)` is invoked with expected args.
* Verifies model is populated and fallback collaborator is not touched.

2. `getPreviewType_shouldDelegateToOtherPreviewWhenImageListEmpty`
* Mocks `pdf2jpg(...)` to return empty list.
* Verifies behavior interaction: `otherFilePreview.notSupportedFile(...)` is called.
* Verifies returned page is fallback page.

## 3. Local verification command
```bash
mvn -pl server -Dtest=cn.keking.service.FileConvertQueueTaskStubbingTests,cn.keking.service.impl.OfficeFilePreviewMockingTests test
```

---

## Part 6: Static Analyzers

## 1. Static Analysis: Goals, Purpose, and Usage

### 1.1 What static analyzers are
Static analysis tools analyze source code (or bytecode/intermediate representations) **without executing the program**. They detect potential defects, security risks, code smells, and maintainability problems using rule-based checks and data/control-flow analysis.

### 1.2 Goals and purposes
* Detect problems early before runtime testing or production deployment.
* Catch security weaknesses (e.g., unsafe SSL trust, untrusted input usage).
* Enforce coding quality and maintainability standards.
* Reduce regression risk by continuously scanning every push/PR.

### 1.3 Typical use in CI
* Run automatically on push/pull request.
* Publish findings as alerts/artifacts.
* Triage findings into real defects vs acceptable risk/false positives.
* Track findings trend across commits.

## 2. Tools Enabled in This Project

### 2.1 GitHub CodeQL (Code Scanning + Code Quality)
Enabled with GitHub Actions workflow:
* `.github/workflows/codeql.yml`

Key setup:
* Query suite: `security-and-quality`
* Language: `java-kotlin`
* Trigger: push/pull_request/workflow_dispatch on `master` and `main`

Reference links:
* https://docs.github.com/en/code-security/code-scanning/introduction-to-code-scanning/about-code-scanning-with-codeql
* https://docs.github.com/en/code-security/code-scanning/automatically-scanning-your-code-for-vulnerabilities-and-errors/configuring-code-scanning
* https://docs.github.com/en/code-security/code-scanning/using-codeql-code-scanning-with-your-existing-ci-system

### 2.2 Additional Static Analyzer: PMD
Enabled with GitHub Actions workflow:
* `.github/workflows/pmd.yml`

Key setup:
* Maven goal: `org.apache.maven.plugins:maven-pmd-plugin:3.26.0:pmd`
* Auto-generated report: `server/target/pmd.xml`
* Workflow summary includes total finding count and files-with-findings count.

## 3. Findings Overview and Counts

### 3.1 Count sources
Counts should be collected from:
* CodeQL: GitHub `Security -> Code scanning alerts` and `Code quality` views.
* PMD: `pmd` workflow step summary + uploaded `pmd.xml` artifact.

### 3.2 Actual run results
Based on the GitHub Actions run after push:
* Before push, CodeQL reported one medium setup/configuration issue:
  * `Workflow does not contain permissions`
* After push, CodeQL reported:
  * **47 new alerts** in total (mixed severities: error/high/medium/warning).
  * Representative high/error findings include:
    * `Server-side request forgery` (`java/ssrf`) in `FtpUtils.java`
    * `Log Injection` in `OnlinePreviewController.java`
    * `Insertion of sensitive information into log files` (`java/sensitive-log`) in `FtpUtils.java`
  * Representative medium finding:
    * `Executing a command with a relative path` (`java/relative-path-command`) in `OfficePluginManager.java`
  * Representative warning finding:
    * `Useless comparison test` (`java/constant-comparison`) in `SimpleEncodingDetects.java`
* PMD reported:
  * Total findings: **78**
  * Files with findings: **19**
  * Report path: `server/target/pmd.xml`

### 3.3 Result table

| Result Source | Where to Read | Finding Count |
|---|---|---:|
| CodeQL standard findings (code scanning) | GitHub Code Scanning Alerts | 47 new alerts |
| CodeQL code quality findings | GitHub Code Quality view | 1 medium finding observed before push (`Workflow does not contain permissions`) |
| PMD findings | `PMD Static Analysis` workflow summary (`pmd.xml`) | 78 findings across 19 files |

## 4. Warning Deep Dive 

### 4.1 CodeQL standard finding example
#### Candidate warning
* **Server-side request forgery (SSRF)** (`java/ssrf`)
* File: `server/src/main/java/cn/keking/utils/FtpUtils.java` (around line 27)

#### Why this warning appears
The FTP connection host value can be influenced by external input and is used directly in:
* `ftpClient.connect(host, port);`

#### Is this an actual problem?
* **Yes, it's a real problem**, but the `application.properties` regulates host allowlisting.
* Without strict host allowlisting/validation, attacker-controlled input can force server-side network access to internal or sensitive endpoints.
* Recommended mitigation: allowlist domains/IP ranges, block private/internal CIDRs, and validate protocol/host before connecting.

### 4.2 CodeQL code quality finding example
#### Candidate warning
* **Executing a command with a relative path** (`java/relative-path-command`)
* File: `server/src/main/java/cn/keking/service/OfficePluginManager.java` (around line 116)

#### Why this warning appears
The code executes:
* `Runtime.getRuntime().exec(new String[]{"sh", "-c", ...})`
using a relative executable path (`sh`), which depends on `PATH`.

#### Is this an actual problem?
* **Yes, in hardened deployment contexts.**
* If environment variables are tampered with, a different executable may be resolved.
* Mitigation: use absolute command paths (for example `/bin/sh`) or safer process APIs.

### 4.3 PMD finding example
#### Candidate warning
* **Avoid using `System.out.println` in server code** (`SystemPrintln` style warning)
* File: `server/src/main/java/cn/keking/web/controller/OnlinePreviewController.java`

#### Why this warning appears
`System.out.println(e);` is used in exception handling logic of a web controller.

#### Is this an actual problem?
* **Yes, for maintainability/operations quality.**
* `System.out` bypasses unified logging controls (level, format, correlation IDs).
* Replace with `logger.error("...", e)`.

## 5. High-Level Comparison 

### 5.1 Similarities
* All of them identify potential defects without executing application behavior.
* All can run continuously in CI and provide actionable locations.

### 5.2 Differences in purpose and signal
* **CodeQL standard findings**: stronger focus on security and correctness paths using semantic/data-flow analysis.
* **CodeQL code quality findings**: focuses more on maintainability/reliability patterns and coding quality.
* **PMD**: rule-based style/design/static checks; fast and broad for code hygiene.

### 5.3 Overlap and complementarity
* Overlap exists in secure coding hygiene (for example, unsafe logging patterns).
* CodeQL provided deeper security-oriented findings here (SSRF, log injection, sensitive log, path expression issues).
* PMD report (`pmd.xml`) complements CodeQL by reporting broader rule-based maintainability/style violations at scale.

### 5.4 Strengths and weaknesses
|        | Strength                                                                 | Weakness                                                    |
|--------|--------------------------------------------------------------------------|-------------------------------------------------------------|
| CodeQL | deep semantic analysis, security-oriented precision, rich trace context. | setup/runtime heavier; some findings require triage effort. |
| PMD    | simple integration, fast feedback, broad hygiene rules. | more rule-noise/false positives for context-specific patterns. |

## 6. Reproduction Steps

### 6.1 Push and run workflows
After pushing current changes, run:
* `CodeQL Analysis` workflow
* `PMD Static Analysis` workflow

### 6.2 Collect counts for final submission
* Open GitHub `Security -> Code scanning alerts` and Code Quality view to record CodeQL counts.
* Open the `PMD Static Analysis` run summary to record total PMD findings.
* Update Section 3.3 table with exact numbers.

### 6.3 Note on CodeQL workflow status = failure
In this run, the `CodeQL Analysis` workflow status was `failure` due to three concrete issues from the log:
* `autobuild` failed (`autobuild.sh` exit code 1), so CodeQL requested custom build steps.
* SARIF upload reported configuration conflict: advanced CodeQL workflow cannot be processed when repository `default setup` is enabled at the same time.
* Workflow used `github/codeql-action@v3`, which has a deprecation warning and should be upgraded to `@v4`.

Fixes applied in this repo:
* Replaced `autobuild` with explicit Maven build step in `codeql.yml`.
* Upgraded CodeQL action from `v3` to `v4`.

Still required in GitHub settings:
* Disable CodeQL `default setup` if you want to keep this custom workflow (advanced setup), otherwise remove this workflow and use only default setup.

## 7. Conclusion
This assignment adds reproducible static-analysis infrastructure to kkFileView by enabling GitHub CodeQL (security + quality) and PMD. The project now has automated static checks at PR/push time and a clear workflow for triaging findings into real defects versus acceptable risk.
