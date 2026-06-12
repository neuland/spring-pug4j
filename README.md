[![Test Status](https://github.com/neuland/spring-pug4j/actions/workflows/test.yaml/badge.svg)](https://github.com/neuland/spring-pug4j/actions)

# Spring Integration for Pug4J

Spring-pug4j provides seamless integration between Spring Framework and Pug4J templating engine. It enables you to use Pug templates in your Spring MVC applications with full support for Spring's view resolution mechanism.

See [neuland/pug4j](https://github.com/neuland/pug4j) for more information about the Pug4J templating engine.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Features](#features)
- [Spring Boot Auto-Configuration](#spring-boot-auto-configuration)
- [Configuration](#bean-declarations)
  - [Spring XML Configuration](#spring-xml-configuration)
  - [Spring Java Configuration](#spring-java-configuration)
- [Template Loader Path](#template-loader-path)
- [Usage Example](#usage-example)
- [Migration Guide](#migration-guide)
- [Versions](#versions)

## Requirements

- Java 17+
- Spring Framework 6.2+
- Pug4J 3.0.0+

## Spring Boot Auto-Configuration

On Spring Boot (3 or 4), spring-pug4j configures itself: add the dependency, put `.pug` templates into `src/main/resources/templates/`, and return view names from your controllers — no bean configuration required.

All settings live under `spring.pug4j.*` and follow the conventions you know from `spring.thymeleaf.*`:

| Property | Default | Description |
|---|---|---|
| `spring.pug4j.enabled` | `true` | Whether to auto-configure Pug4J view resolution. |
| `spring.pug4j.prefix` | `classpath:/templates/` | Prefix that gets prepended to view names when building a URL. |
| `spring.pug4j.suffix` | `.pug` | Suffix that gets appended to view names when building a URL. |
| `spring.pug4j.encoding` | `UTF-8` | Template files encoding (file reading only). Unlike Thymeleaf, the response charset is not derived from this — it is part of `spring.pug4j.content-type`. |
| `spring.pug4j.cache` | `true` | Whether to enable template caching. Disable during development. |
| `spring.pug4j.mode` | `HTML` | Output mode for templates without an explicit doctype (`HTML`, `XHTML`, `XML`). |
| `spring.pug4j.content-type` | `text/html;charset=UTF-8` | Content-Type written to the HTTP response. |
| `spring.pug4j.pretty-print` | `false` | Whether to pretty-print the rendered output. |
| `spring.pug4j.check-template-location` | `true` | Whether to log a warning when the templates location does not exist. |
| `spring.pug4j.produce-partial-output-while-processing` | `false` | Stream output directly into the response instead of buffering the full page first. Improves time-to-first-byte for pages larger than the servlet container's response buffer — but a failing template may then deliver a partial page instead of a clean error. |
| `spring.pug4j.view-names` | | View names that can be resolved (supports simple wildcards). Unset means all. |
| `spring.pug4j.view-resolver-order` | `Ordered.LOWEST_PRECEDENCE - 5` | Order of the Pug view resolver in the view resolver chain (Thymeleaf's slot). Adjust when combining multiple template engines. |
| `spring.pug4j.render-exceptions` | `false` | Render Pug exceptions as styled HTML error pages instead of propagating them (development only). |
| `spring.pug4j.debug-error-page` | `false` | Render pug4j's debug error page at `/error` for Pug exceptions (development only, see below). |

Every auto-configured bean backs off as soon as you define your own (`TemplateLoader`, `PugEngine`, or `PugViewResolver`), so existing manual configurations keep working unchanged. For the view resolver the back-off matches on the bean type *and* on the conventional bean names `viewResolver`/`pugViewResolver` — Spring evaluates conditions against the declared return type of `@Bean` methods, so prefer declaring `PugViewResolver` (not plain `ViewResolver`) or use one of those bean names. The manual bean declarations below are only needed for plain Spring MVC without Boot, or when you need settings the properties don't cover (e.g. a custom `basePath` or global render variables).

Typical development setup:

```properties
spring.pug4j.cache=false
spring.pug4j.render-exceptions=true
spring.pug4j.debug-error-page=true
```

## Bean Declarations

### Spring XML Configuration

**Note:** Due to the builder pattern used by pug4j 3.0+, **Java configuration is strongly recommended** over XML configuration. If you must use XML, you'll need to create custom factory beans (see example below).

**Recommended: Switch to Java Configuration** (see next section)

If you need XML configuration, here's a working approach using Spring EL:

```xml
<bean id="templateLoader" class="de.neuland.pug4j.spring.template.SpringTemplateLoader">
    <property name="templateLoaderPath" value="classpath:/templates" />
</bean>

<!-- Create PugEngine using Spring Expression Language -->
<bean id="pugEngine"
      factory-bean="pugEngineBuilder"
      factory-method="build">
</bean>

<bean id="pugEngineBuilder"
      class="de.neuland.pug4j.PugEngine"
      factory-method="builder">
    <property name="templateLoader" ref="templateLoader" />
    <property name="caching" value="false" />
</bean>

<!-- Create RenderContext using Spring Expression Language -->
<bean id="renderContext"
      factory-bean="renderContextBuilder"
      factory-method="build">
</bean>

<bean id="renderContextBuilder"
      class="de.neuland.pug4j.RenderContext"
      factory-method="builder">
    <property name="prettyPrint" value="false" />
</bean>

<bean id="viewResolver" class="de.neuland.pug4j.spring.view.PugViewResolver">
    <property name="engine" ref="pugEngine" />
    <property name="renderContext" ref="renderContext" />  <!-- Optional -->
    <!-- rendering nice html formatted error pages for development -->
    <property name="renderExceptions" value="true" />
</bean>
```

### Spring Java Configuration

```java
@Configuration
public class PugConfig {

    @Bean
    public SpringTemplateLoader templateLoader() {
        SpringTemplateLoader templateLoader = new SpringTemplateLoader();
        templateLoader.setTemplateLoaderPath("classpath:/templates");
        templateLoader.setEncoding("UTF-8");
        templateLoader.setSuffix(".pug");
        return templateLoader;
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
    public PugViewResolver viewResolver() {
        PugViewResolver viewResolver = new PugViewResolver();
        viewResolver.setEngine(pugEngine());
        viewResolver.setRenderContext(renderContext());  // Optional
        return viewResolver;
    }
}
```

**Note:** If `RenderContext` is not set, the view resolver will use default settings (prettyPrint=false, defaultMode=HTML, no global variables).

### Default Mode

pug4j 3.0.0 changed its own default mode to `Mode.XHTML` (matching pug.js: for templates without a doctype, void tags self-close like `<input/>` and boolean attributes render as `checked="checked"`). For backwards compatibility, spring-pug4j keeps `Mode.HTML` (terse output like `<input checked>`) as its default. Templates with an explicit doctype are not affected.

To opt into the pug.js-conformant behavior:

```java
PugViewResolver viewResolver = new PugViewResolver();
viewResolver.setEngine(pugEngine());
viewResolver.setDefaultMode(Pug4J.Mode.XHTML);
```

If a full `RenderContext` is set via `setRenderContext(...)`, its `defaultMode` wins and `setDefaultMode(...)` is ignored.

### Debug Error Page (Spring Boot)

When running on Spring Boot, spring-pug4j can auto-configure an `ErrorViewResolver` that renders pug4j's styled debug error page (template source context, file path, line/column) whenever a request fails with a `PugException`. The regular Spring Boot error pipeline (status code, logging, `/error`) stays untouched — only the rendered error view is replaced; non-Pug errors fall through to the default error handling.

The page exposes template source and paths, so it is **disabled by default**. Enable it for development only:

```properties
spring.pug4j.debug-error-page=true
```

(The pre-3.6.0 property name `pug4j.spring.debug-error-page` is deprecated but still honored.)

The property works on both Spring Boot 3 and Spring Boot 4. Boot 4 moved the `ErrorViewResolver` interface to `org.springframework.boot.webmvc.autoconfigure.error` (artifact `spring-boot-webmvc`); spring-pug4j ships resolvers for both locations and auto-configures the one matching your classpath — no extra setup needed.

**Spring Security note:** the debug page is rendered via the error dispatch to `/error`. If Spring Security blocks that dispatch, template errors surface as a bare 403 instead of the debug page. Permit the error dispatch in your security configuration:

```java
http.authorizeHttpRequests(auth -> auth
    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
    // ... your other rules
);
```

## Template Loader Path

SpringTemplateLoader uses Spring Framework's `ResourceLoader` for loading templates. You can use any Spring resource location:

- **Classpath**: `classpath:/templates` - points to a templates folder in your resources
- **File system**: `file:/path/to/templates` - absolute file system path
- **Relative**: `/templates` - relative to the application context

For more information, see [Spring ResourceLoader documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#resources-resourceloader).

## Usage Example

### Create a Pug Template

Create a file `src/main/resources/templates/index.pug`:

```pug
doctype html
html
  head
    title= title
  body
    h1= message
    p Welcome to #{name}!
```

### Create a Controller

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Spring Pug4J");
        model.addAttribute("message", "Hello World");
        model.addAttribute("name", "Spring-pug4j");
        return "index";  // resolves to index.pug
    }
}
```

The view name `"index"` will be resolved to `classpath:/templates/index.pug` and rendered with the model attributes.

## Migration Guide

### Migrating from spring-pug4j 3.4.x to 3.5.0

Version 3.5.0 migrates from the deprecated `PugConfiguration` API (deprecated in pug4j 3.0.0) to the new `PugEngine` and `RenderContext` APIs.

#### Changes Required

**Before (spring-pug4j 3.x with pug4j 2.x):**

```java
@Bean
public PugConfiguration pugConfiguration() {
    PugConfiguration configuration = new PugConfiguration();
    configuration.setCaching(false);
    configuration.setPrettyPrint(false);
    configuration.setTemplateLoader(templateLoader());
    return configuration;
}

@Bean
public ViewResolver viewResolver() {
    PugViewResolver viewResolver = new PugViewResolver();
    viewResolver.setConfiguration(pugConfiguration());
    return viewResolver;
}
```

**After (spring-pug4j 3.5.0 with pug4j 3.0+):**

```java
@Bean
public PugEngine pugEngine() {
    return PugEngine.builder()
        .templateLoader(templateLoader())
        .caching(false)
        .build();
}

@Bean
public RenderContext renderContext() {
    return RenderContext.builder()
        .prettyPrint(false)
        .build();
}

@Bean
public ViewResolver viewResolver() {
    PugViewResolver viewResolver = new PugViewResolver();
    viewResolver.setEngine(pugEngine());
    viewResolver.setRenderContext(renderContext());  // Optional
    return viewResolver;
}
```

#### Key Changes

1. **Separation of Concerns**: Template loading/caching (`PugEngine`) is now separate from render-time options (`RenderContext`)
2. **Builder Pattern**: Both `PugEngine` and `RenderContext` use fluent builder APIs
3. **Property Changes**:
   - `setConfiguration()` → `setEngine()` and `setRenderContext()`
   - `prettyPrint` moved from `PugConfiguration` to `RenderContext`
   - `mode` moved from `PugConfiguration` to `RenderContext`
   - Caching options remain in `PugEngine`

#### XML Configuration Migration

**Our Strong Recommendation**: Migrate XML configuration to Java configuration, as the builder pattern in pug4j 3.0+ works much more naturally with Java config.

**Before (spring-pug4j 3.x):**

```xml
<bean id="pugConfiguration" class="de.neuland.pug4j.PugConfiguration">
    <property name="prettyPrint" value="false" />
    <property name="caching" value="false" />
    <property name="templateLoader" ref="templateLoader" />
</bean>

<bean id="viewResolver" class="de.neuland.pug4j.spring.view.PugViewResolver">
    <property name="configuration" ref="pugConfiguration" />
</bean>
```

**After - Option A: Java Configuration (Recommended):**

```java
@Configuration
public class PugConfig {
    @Bean
    public PugEngine pugEngine() {
        return PugEngine.builder()
            .templateLoader(templateLoader())
            .caching(false)
            .build();
    }

    @Bean
    public RenderContext renderContext() {
        return RenderContext.builder()
            .prettyPrint(false)
            .build();
    }

    @Bean
    public ViewResolver viewResolver() {
        PugViewResolver resolver = new PugViewResolver();
        resolver.setEngine(pugEngine());
        resolver.setRenderContext(renderContext());
        return resolver;
    }
}
```

**After - Option B: XML Configuration (More Complex):**

```xml
<!-- Create builder and call build() method -->
<bean id="pugEngineBuilder"
      class="de.neuland.pug4j.PugEngine"
      factory-method="builder">
    <property name="templateLoader" ref="templateLoader" />
    <property name="caching" value="false" />
</bean>

<bean id="pugEngine"
      factory-bean="pugEngineBuilder"
      factory-method="build" />

<bean id="renderContextBuilder"
      class="de.neuland.pug4j.RenderContext"
      factory-method="builder">
    <property name="prettyPrint" value="false" />
</bean>

<bean id="renderContext"
      factory-bean="renderContextBuilder"
      factory-method="build" />

<bean id="viewResolver" class="de.neuland.pug4j.spring.view.PugViewResolver">
    <property name="engine" ref="pugEngine" />
    <property name="renderContext" ref="renderContext" />
</bean>
```

## Versions

### 3.6.0
* Optional streaming output (`spring.pug4j.produce-partial-output-while-processing=true`, or `setProducePartialOutputWhileProcessing` on the resolver) for faster time-to-first-byte on large pages; default stays buffered so failing templates never deliver partial pages
* Full Spring Boot auto-configuration: templates render from `classpath:/templates/` with zero bean configuration; all settings under `spring.pug4j.*` following `spring.thymeleaf.*` conventions (with IDE completion via configuration metadata); auto-configured beans back off when you define your own (for the view resolver also via the conventional bean names `viewResolver`/`pugViewResolver`, so beans declared with a supertype return type are recognized); resolver position configurable via `spring.pug4j.view-resolver-order`
* Property prefix moved to `spring.pug4j.*` — the pre-3.6.0 `pug4j.spring.debug-error-page` is deprecated but still honored
* Debug error page now works on Spring Boot 4: Boot 4 relocated the `ErrorViewResolver` interface to `org.springframework.boot.webmvc.autoconfigure.error` (artifact `spring-boot-webmvc`); spring-pug4j ships resolvers for both locations and auto-configures the one matching the classpath (no new mandatory runtime dependencies)
* `PugView` no longer presets the response Content-Type before rendering: a propagated render error previously left `text/html` behind and broke content negotiation in the error dispatch (Spring Boot's JSON error response failed with `HttpMessageNotWritableException`)
* Documented the Spring Security setup required for the debug error page (`dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()`)
* Removed the unused `PugView.getEncoding()`/`setEncoding()` accessors — they had no effect; template decoding is handled by `SpringTemplateLoader`, the response charset by the content type

### 3.5.0
* **Breaking**: Migrated from deprecated `PugConfiguration` to new `PugEngine` + `RenderContext` APIs
* Updated to pug4j 3.0.0
* Eliminated deprecation warnings (error pages now use `PugErrorRenderer` instead of the deprecated `PugException.toHtmlString`)
* `PugViewResolver.setDefaultMode(Mode)` for templates without a doctype — defaults to `Mode.HTML` for backwards-compatible output (pug4j 3.0.0 itself defaults to `Mode.XHTML`)
* `PugView` renders into a buffer first, so failing templates never deliver partial pages; render errors propagate to Spring's error handling when `renderExceptions` is off
* New auto-configured Spring Boot debug error page (`pug4j.spring.debug-error-page=true`, off by default)
* `PugViewResolver` now fails fast with a clear error message if no `PugEngine` is configured (previously a `NullPointerException` at the first request)
* See [Migration Guide](#migration-guide) above for upgrade instructions

### 3.4.1
* Updated to pug4j 2.4.1

### 3.4.0
* Updated to pug4j 2.4.0
* Switched publishing to the Maven Central Portal

### 3.3.1
* Updated to pug4j 2.3.1
* Updated to Spring Framework 6.2

### 3.3.0
* Updated to pug4j 2.3.0
* Updated to Spring Framework 6.1
* Updated dependencies

### 3.2.0
* Updated to pug4j 2.2.0
* Updated dependencies

### 3.1.0
* Updated dependencies (thanks dbelyaev)
* Updated to pug4j 2.1.0

### 3.0.0
* Switched to jakarta.servlet-api
* Support for Spring Framework 6
* Requires Java 17

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>de.neuland-bfi</groupId>
  <artifactId>spring-pug4j</artifactId>
  <version>3.5.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'de.neuland-bfi:spring-pug4j:3.5.0'
```

## Features

- 🎨 **Full Pug Template Support**: Use Pug's elegant syntax in your Spring MVC applications
- 🔄 **Spring Integration**: Seamless integration with Spring's view resolution mechanism
- 🚀 **Performance**: Built-in template caching for production environments
- 🛠️ **Development Friendly**: Rich error pages with template context during development
- 🌍 **Resource Loading**: Support for classpath, file system, and URL-based template loading
- ⚙️ **Configurable**: Fine-grained control over rendering options via `RenderContext`

## Author

- Stefan Kuper / [planetk](https://github.com/planetk)
- Michael Geers / [naltatis](https://github.com/naltatis)
- Christoph Blömer / [chbloemer](https://github.com/chbloemer)

## License

The MIT License

Copyright (C) 2012-2026 [neuland Büro für Informatik](http://www.neuland-bfi.de/), Bremen, Germany

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
