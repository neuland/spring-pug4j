# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring-pug4j is a Spring Framework integration library for Pug4J (formerly Jade4J), providing Spring MVC view resolution for Pug templates. The library acts as a bridge between Spring's view resolution mechanism and the Pug templating engine.

**Key Details:**
- Current version: 3.5.0-SNAPSHOT
- Requires Java 17+
- Spring Framework 6.2+ (Jakarta EE with jakarta.servlet-api 6.0)
- Pug4J 3.0.0+ (using new `PugEngine` and `RenderContext` APIs)
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
  - The shared `PugEngine` instance for template loading and compilation
  - Optional `RenderContext` for render-time settings (prettyPrint, mode, global variables)
  - Content type (default: "text/html;charset=UTF-8")
  - Exception rendering mode for development

### 3. PugView (`de.neuland.pug4j.spring.view.PugView`)
- Extends Spring's `AbstractTemplateView`
- Executes template rendering by:
  - Retrieving compiled template from `PugEngine`
  - Merging Spring MVC model data with the template
  - Rendering into a `StringWriter` buffer first, so render errors never deliver partial pages
  - Writing the buffered HTML to the HTTP response on success
  - Optional streaming mode (`producePartialOutputWhileProcessing`, default off): renders directly into the response writer for faster time-to-first-byte on large pages. Template loading/parsing errors still leave the response untouched (they occur before the first byte), but a mid-render failure leaves a partial page behind
  - Setting the response Content-Type only when actually writing (overrides `applyContentType` as no-op): a propagated render exception must not leave a preset Content-Type behind, or content negotiation in the error dispatch breaks (e.g. Spring Boot's JSON error response fails with `HttpMessageNotWritableException`)
- Features a development-friendly exception rendering mode (`renderExceptions`):
  - When enabled, catches `PugException` and renders the styled error page via `PugErrorRenderer.renderHtml()`
  - When disabled, exceptions propagate to Spring's standard error handling

### 4. PugAutoConfiguration (`de.neuland.pug4j.spring.boot.PugAutoConfiguration`)
- Full Spring Boot auto-configuration (Boot 3 and 4): registers `SpringTemplateLoader`, `PugEngine`, and `PugViewResolver` with zero manual bean configuration
- All settings under `spring.pug4j.*` (`PugProperties`), names mirroring `spring.thymeleaf.*`: `prefix`, `suffix`, `encoding`, `cache`, `mode`, `content-type`, `pretty-print`, `check-template-location`, `view-names`, `produce-partial-output-while-processing`, plus pug-specific `render-exceptions` and `debug-error-page`
- Every bean is `@ConditionalOnMissingBean` (loader on `TemplateLoader`, engine on `PugEngine`, resolver on `PugViewResolver`), so manual configurations win; `spring.pug4j.enabled=false` disables everything
- View resolver order is `LOWEST_PRECEDENCE - 5` (Thymeleaf's slot); missing templates fall through to other resolvers via `PugView.checkResource()`
- `spring-boot-configuration-processor` (optional) generates metadata for IDE completion; `additional-spring-configuration-metadata.json` marks the legacy `pug4j.spring.debug-error-page` deprecated

### 5. PugDebugErrorViewResolver (`de.neuland.pug4j.spring.boot.PugDebugErrorViewResolver`)
- Spring Boot `ErrorViewResolver`, auto-configured by `PugDebugErrorViewResolverAutoConfiguration`
- Renders pug4j's debug error page (via `PugErrorRenderer`) at `/error` when the request failed with a `PugException` (unwraps the cause chain of the servlet error attribute)
- Leaves the Spring Boot error pipeline intact (status, logging); returns `null` for non-Pug errors
- Disabled by default; enable with `spring.pug4j.debug-error-page=true` (development only — exposes template source and paths). The pre-3.5.1 `pug4j.spring.debug-error-page` is deprecated but still honored (`PugDebugErrorPageCondition`, an `AnyNestedCondition` ORing both prefixes)
- Spring Boot dependency is `optional` in the pom; plain Spring MVC users are unaffected
- Spring Boot 4 relocated `ErrorViewResolver` to `org.springframework.boot.webmvc.autoconfigure.error` (artifact `spring-boot-webmvc`). `Boot4PugDebugErrorViewResolver` + `Boot4PugDebugErrorViewResolverAutoConfiguration` cover that path; both resolvers share their logic via the package-private `PugDebugErrorPage` helper. Exactly one auto-configuration activates per classpath (`@ConditionalOnClass` on the respective interface; the Boot 4 config guards by class *name* to avoid loading a missing type). `spring-boot-webmvc:4.0.x` is in the pom as `optional` with all transitives excluded — compile-only, no consumer exposure

### Component Interaction Flow

1. Spring MVC receives a request and determines a logical view name (e.g., "index")
2. `PugViewResolver.buildView()` creates a `PugView` instance and injects `PugEngine` and `RenderContext`
3. `PugView.renderMergedTemplateModel()` is invoked with the model data
4. `PugView` retrieves the compiled template via `PugEngine.getTemplate()`
5. `PugEngine` uses `SpringTemplateLoader.getReader()` to load the template source
6. Template is compiled (or retrieved from cache) and rendered with the model and `RenderContext`
7. Output is written to the HTTP response

## Configuration Patterns

The library supports both XML and Java-based Spring configuration using the pug4j 3.0 API:

**XML Configuration:**
```xml
<bean id="templateLoader" class="de.neuland.pug4j.spring.template.SpringTemplateLoader">
    <property name="templateLoaderPath" value="classpath:/templates" />
</bean>

<!-- Create PugEngine builder, then call build() -->
<bean id="pugEngineBuilder" class="de.neuland.pug4j.PugEngine" factory-method="builder">
    <property name="templateLoader" ref="templateLoader" />
    <property name="caching" value="false" />
</bean>

<bean id="pugEngine" factory-bean="pugEngineBuilder" factory-method="build" />

<!-- Optional: Configure render context for pretty-printing, mode, etc. -->
<bean id="renderContextBuilder" class="de.neuland.pug4j.RenderContext" factory-method="builder">
    <property name="prettyPrint" value="false" />
</bean>

<bean id="renderContext" factory-bean="renderContextBuilder" factory-method="build" />

<bean id="viewResolver" class="de.neuland.pug4j.spring.view.PugViewResolver">
    <property name="engine" ref="pugEngine" />
    <property name="renderContext" ref="renderContext" />  <!-- Optional -->
    <property name="renderExceptions" value="true" />
</bean>
```

**Note:** Due to the builder pattern, Java configuration is recommended over XML for cleaner syntax.

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
    public PugEngine pugEngine() {
        return PugEngine.builder()
            .templateLoader(templateLoader())
            .caching(false)
            .build();
    }

    @Bean
    public RenderContext renderContext() {
        // Optional: customize rendering settings
        return RenderContext.builder()
            .prettyPrint(false)
            .build();
    }

    @Bean
    public ViewResolver viewResolver() {
        PugViewResolver resolver = new PugViewResolver();
        resolver.setEngine(pugEngine());
        resolver.setRenderContext(renderContext());  // Optional
        return resolver;
    }
}
```

**Note:** If `RenderContext` is not set, the resolver builds one with:
- `prettyPrint = false`
- `defaultMode = Mode.HTML` (backwards-compatible; pug4j 3.0.0's own `RenderContext.defaults()` uses `Mode.XHTML`)
- No global variables

The default mode for doctype-less templates is configurable via `PugViewResolver.setDefaultMode(Mode)`; an explicitly set `RenderContext` takes precedence.

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