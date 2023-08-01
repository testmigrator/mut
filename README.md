# mut

a test migration tool


## antlr:s

Parses Java files to generate abstract syntax trees and retrieve class information, among other tasks.

## dao: 

Handles persistence and stores some intermediate results using the MyBatis middleware.

## entity: 

Contains various entity classes, such as testing migration task parameters, API basic information, API mapping information, and code translation information.

## service:

Contains business logic.

## extract:

Extracts API basic information.

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

# Steps:

1. Modify the task.properties file with the appropriate task parameters.
2. Execute ApiMappingMain to compute API mappings.
3. Execute TranslateMain to convert the test code.
