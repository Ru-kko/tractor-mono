# Conventions

> Extreme homogeneity. The code shuld look like it was written by the same person everywhere.

## Java
- **Version**: Java 21
- **Indentation**: 2 spaces
- **Braces**: Opening braces should be on the same line as the declaration. Closing braces should
  be on a new line.
- **Line Length**: Lines should not exceed 120 characters. If a line exceeds this limit, it should be
  broken into multiple lines.
- **Scalar**: Use `springdoc` annotations for API documentation. Ensure that all public methods in controllers
  are documented with `scalar`. 

### Naming Conventions
| Type | Convention | Example |
|------|------------|---------|
| Class | PascalCase | `CartImpl` or `TractorStoreException` |
| Method | camelCase | `getTractorStore()` |
| Variable | camelCase | `tractorStore` |
| Constant | UPPER_CASE | `TRACTOR_STORE` |
| Interface | PascalCase (without "I" prefix) | `TractorStore` |
| Package | lowercase | `com.example.tractorstore` |


### Tests

- **Test Framework**: JUnit 5
- **Test Structure**: Tests should be organized in the same package structure as the main code. Each
  module should have its own test package.
- **Test Naming**: Test classes should be named after the class they are testing, with `Test` as a
  suffix. For example, if you are testing the `Cart` class, the test class should be named
  `CartTest`.

### Comments

By default it is not written in the code. The code should be self-explanatory. If a comment is necessary,
it should be concise and clear. Use Javadoc for public methods and classes.