# Testing Report for kkFileView --Part 2
#### Group 5 kkFileView
Member Name: Xinyi Xu </br>
Github Link: https://github.com/Po11uxx/kkFileView

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
