# MUT: Human-in-the-Loop Unit Test Migration

a test migration tool

Abstract
> Test migration, which enables the reuse of test cases crafted with knowledge and creativity by testers across various platforms and programming languages, has exhibited effectiveness in mobile app testing. However, unit test migration at the source code level has not garnered adequate attention and exploration. In this paper, we propose a novel cross-language and cross-platform test migration methodology, named MUT, which consists of four modules: code mapping, test case filtering, test case translation, and test case adaptation. MUT initially calculates code mappings to establish associations between source and target projects, and identifies suitable unit tests for migration from the source project. Then, MUT's code translation component generates a syntax tree by parsing the code to be migrated and progressively converts each node in the tree, ultimately generating the target tests, which are compiled and executed in the target project. Moreover, we develop a web tool to assist developers in test migration. The effectiveness of our approach has been validated on five prevalent functional domain projects within the open-source community. We migrate a total of 550 unit tests and submitted pull requests to augment test code in the target projects on GitHub. By the time of this paper submission, 253 of these tests have already been merged into the projects (including 197 unit tests in the Luliyucoordinate-LeetCode project and 56 unit tests in the Rangerlee-HtmlParser project). Through running these tests, we identify 5 bugs, and 2 functional defects, and submitted corresponding issues to the project. The evaluation substantiates that MUT's test migration is both viable and beneficial across programming languages and different projects.

# Usage:
1. Modify the task.properties file with the appropriate task parameters.
2. Execute ApiMappingMain to compute API mappings.
3. Execute TranslateMain to convert the test code.

# Components:

## ANTLR:
Parses Java files to generate abstract syntax trees and retrieve class information, among other tasks.

## entity: 
Contains various entity classes, such as testing migration task parameters, API basic information, API mapping information, and code translation information.

## service:
Contains business logic.

## extract:
Extracts API basic information.

## dao: 
Handles persistence and stores some intermediate results using the MyBatis middleware.

## invocation:
Retrieves the function call chain.

## preprocess: 
Handles code preprocessing tasks, such as converting between camel case and underscore naming conventions, extracting stems, and eliminating stop words.

## translate: 
Houses the code translation engine, which includes modules for handling BNF code syntax rules, loading mapping rules, and applying replacement rules.

## resource:
Contains various resources used in the system.

## mappingRule:
Stores generated and supplemented mapping rules.

## task.properties:
Holds parameters for the testing migration task.


