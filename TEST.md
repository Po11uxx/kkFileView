# Testing Report for kkFileView
#### Group 5 kkFileView
Member Name: Xinyi Xu </br>
Github Link: https://github.com/Po11uxx/kkFileView

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
Test cases were added to [server/src/test/java/cn/keking/ImagePreviewTests.java](https://github.com/Po11uxx/kkFileView/blob/9b567f9bf2b5e5fab92e64ecdb69a00cd21f71d1/server/src/test/java/cn/keking/ImagePreviewTests.java#L11) . The tests use Spring Boot’s @SpringBootTest to load the application context and validate preview responses.
**Test Execution**: Tests are run via Maven
```bash
mvn test -Dtest=ImagePreviewTests
```
**Expect Results:** All tests pass if the application correctly handles each partition.
