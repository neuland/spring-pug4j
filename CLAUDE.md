# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring-pug4j is a Spring Framework integration library for Pug4J (formerly Jade4J), providing Spring MVC view resolution for Pug templates. The library acts as a bridge between Spring's view resolution mechanism and the Pug templating engine.

**Key Details:**
- Current version: 3.3.2-SNAPSHOT
- Requires Java 17+
- Spring Framework 6.2+ (Jakarta EE with jakarta.servlet-api 6.0)
- Build tool: Maven
- Testing: JUnit 4, Mockito

## Build Commands

```bash
# Compile the project
mvn compile

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=SpringTemplateLoaderTest

# Run tests with debug logging
mvn --batch-mode test -D"org.slf4j.simpleLogger.defaultLogLevel"="error" -D"org.slf4j.simpleLogger.log.de.neuland.pug4j"="debug"

# Full build with verification
mvn --batch-mode --update-snapshots verify

# Package the library
mvn package

# Install to local Maven repository
mvn install
```

## Architecture

The library consists of three core components that integrate Spring MVC with Pug4J:

### 1. SpringTemplateLoader (`de.neuland.pug4j.spring.template.SpringTemplateLoader`)
- Implements `TemplateLoader` from pug4j and `ResourceLoaderAware` from Spring
- Responsible for loading Pug template files using Spring's `ResourceLoader` abstraction
- Handles path resolution, including:
  - Classpath resources (e.g., `classpath:/templates`)
  - File system paths (including Windows paths like `file:C:/templates`)
  - Relative and absolute paths
  - Path normalization (converts Windows backslashes to Unix forward slashes)
- Automatically appends `.pug` suffix to template names if no extension is provided
- Configurable properties:
  - `templateLoaderPath`: Base path for template lookup (default: "")
  - `basePath`: Additional base path component (default: "")
  - `encoding`: Character encoding (default: "UTF-8")
  - `suffix`: Template file extension (default: ".pug")

### 2. PugViewResolver (`de.neuland.pug4j.spring.view.PugViewResolver`)
- Extends Spring's `AbstractTemplateViewResolver`
- Registers itself as a view resolver in Spring MVC's view resolution chain
- Creates `PugView` instances for resolved view names
- Configures each view with:
  - The shared `PugConfiguration` instance
  - Content type (default: "text/html;charset=UTF-8")
  - Exception rendering mode for development

### 3. PugView (`de.neuland.pug4j.spring.view.PugView`)
- Extends Spring's `AbstractTemplateView`
- Executes template rendering by:
  - Retrieving compiled template from `PugConfiguration`
  - Merging Spring MVC model data with the template
  - Writing rendered HTML to the HTTP response
- Features a development-friendly exception rendering mode (`renderExceptions`):
  - When enabled, catches `PugException` and renders formatted HTML error pages
  - When disabled, logs errors and allows Spring's standard error handling

### Component Interaction Flow

1. Spring MVC receives a request and determines a logical view name (e.g., "index")
2. `PugViewResolver.buildView()` creates a `PugView` instance
3. `PugView.renderMergedTemplateModel()` is invoked with the model data
4. `PugView` retrieves the compiled template via `PugConfiguration.getTemplate()`
5. `PugConfiguration` uses `SpringTemplateLoader.getReader()` to load the template source
6. Template is compiled (or retrieved from cache) and rendered with the model
7. Output is written to the HTTP response

## Configuration Patterns

The library supports both XML and Java-based Spring configuration:

**XML Configuration:**
```xml
<bean id="templateLoader" class="de.neuland.pug4j.spring.template.SpringTemplateLoader">
    <property name="templateLoaderPath" value="classpath:/templates" />
</bean>

<bean id="pugConfiguration" class="de.neuland.pug4j.PugConfiguration">
    <property name="caching" value="false" />
    <property name="templateLoader" ref="templateLoader" />
</bean>

<bean id="viewResolver" class="de.neuland.pug4j.spring.view.PugViewResolver">
    <property name="configuration" ref="pugConfiguration" />
    <property name="renderExceptions" value="true" />
</bean>
```

**Java Configuration:**
```java
@Configuration
public class PugConfig {
    @Bean
    public SpringTemplateLoader templateLoader() {
        SpringTemplateLoader loader = new SpringTemplateLoader();
        loader.setTemplateLoaderPath("classpath:/templates");
        loader.setEncoding("UTF-8");
        loader.setSuffix(".pug");
        return loader;
    }

    @Bean
    public PugConfiguration pugConfiguration() {
        PugConfiguration config = new PugConfiguration();
        config.setCaching(false);
        config.setTemplateLoader(templateLoader());
        return config;
    }

    @Bean
    public ViewResolver viewResolver() {
        PugViewResolver resolver = new PugViewResolver();
        resolver.setConfiguration(pugConfiguration());
        return resolver;
    }
}
```

## Release Process

This project uses the Maven Release Plugin for releases:

```bash
# Prepare release (updates versions, creates tag)
mvn release:prepare

# Perform release (builds and deploys to repository)
mvn release:perform
```

Releases are signed with GPG when the `performRelease` property is set, and deployed to Sonatype OSS repository.

## Testing Notes

- Tests use JUnit 4 and Mockito for mocking
- `SpringTemplateLoaderTest` focuses on path resolution edge cases:
  - Windows path handling (backslash to forward slash conversion)
  - Relative paths with `..` navigation
  - Empty template loader paths
  - Various combinations of `templateLoaderPath`, `basePath`, and template names
- CI runs on both Ubuntu and Windows to ensure cross-platform compatibility
- Timezone is set to "Europe/Berlin" in CI environment

## Important Implementation Details

- **Path Normalization**: All file paths are converted to Unix-style (forward slashes) using `FilenameUtils.separatorsToUnix()` to ensure cross-platform compatibility
- **Extension Handling**: The `SpringTemplateLoader.getExtension()` method returns the suffix without the leading dot (e.g., "pug" not ".pug")
- **Resource Loading**: Uses Spring's `ResourceLoader` abstraction, supporting classpath, file system, and URL-based resources
- **Error Handling**: In development mode (`renderExceptions=true`), template compilation errors are rendered as formatted HTML pages; in production mode, errors are logged and handled by Spring's error handling