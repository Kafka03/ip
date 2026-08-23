# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: [to be filled]
* IDE and level of expertise: [to be filled]

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.


## Git message standard
Commit message: Subject
 Every commit must have a well-written commit message subject line.

Try to limit the subject line to 50 characters (hard limit: 72 chars)
Rationale: Some tools show only a limited number of characters from the commit message.

 Use the imperative mood in the subject line.

 Good: Add README.md
 Bad: Added README.md
 Bad: Adding README.md
 Capitalize the first letter of the subject line.

 Good: Move index.html file to root
 Bad: move index.html file to root
 Do not end the subject line with a period.

 Good: Update sample data
 Bad: Update sample data.
You may add a <scope>: or <category>: in front, when applicable.

e.g. Person class: Remove static imports
Main.java: Remove blank lines
bug fix: Add space after name
chore: Update release date
There are other commit subject conventions such as the Conventional Commits Format which are more elaborate but have additional benefits.



## C++ to Java → JUnit → JUnit: Intermediate
Given below are some noteworthy JUnit concepts, as per the JUnit 5 User Guide.

Annotations: In addition to the @Test annotation you've seen already, there are many other annotations in JUnit. For example, the @Disabled annotation can be used to disable a test temporarily. [more ...]

Pre/post-test tasks: In order to allow individual test methods to be executed in isolation and to avoid unexpected side effects due to mutable test instance state, JUnit creates a new instance of each test class before executing each test method. It is possible to supply code that should be run before/after every test method/class (e.g., for setting up the environment required by the tests, or cleaning up things after a test is completed) by using test instance lifecycle annotations such as @BeforeEach @AfterAll. [more ...]

Conditional test execution: It is possible to configure tests to run only under certain conditions. For example, @TestOnMac annotation can be used to specify tests that should run on Mac OS only. [more ...]

Assumptions: It is possible to specify assumptions that must hold for a test to be executed (i.e., the test will be skipped if the assumption does not hold). [more ...]

Tagging tests: It is possible to tag tests (e.g., @Tag("slow") so that tests can be selected based on tags. [more ...]

Test execution order: By default, JUnit executes test classes and methods in a deterministic but intentionally nonobvious order. This ensures that subsequent runs of a test suite execute tests in the same order, thereby allowing for repeatable builds. But it is possible to specify a specific testing order. [more ...]

Test hierarchies: Normally, we organize tests into separate test classes. If a more hierarchical structure is needed, the @Nested annotation can be used to express the relationship among groups of tests. [more ...]

Repeated tests: JUnit provides the ability to repeat a test a specified number of times by annotating a method with @RepeatedTest and specifying the total number of repetitions desired. [more ...]

Parameterized tests make it possible to run a test multiple times with different arguments. The parameter values can be supplied using a variety of ways e.g., an array of values, enums, a csv file, etc. [more ...]

Dynamic tests: The @TestFactory annotation can be used to specify factory methods that generate tests dynamically. [more ...]

Timeouts: The @Timeout annotation allows one to declare that a test should fail if its execution time exceeds a given duration. [more ...]

Parallel execution: By default, JUnit tests are run sequentially in a single thread. Running tests in parallel—for example, to speed up execution—is available as an opt-in feature. [more ...]

Extensions: JUnit supports third-party extensions. The built-in TempDirectory extension is used to create and clean up a temporary directory for an individual test or all tests in a test class. [more ...]

