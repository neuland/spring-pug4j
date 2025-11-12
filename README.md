[![Test Status](https://github.com/neuland/spring-pug4j/actions/workflows/test.yaml/badge.svg)](https://github.com/neuland/spring-pug4j/actions)

# Spring Integration for Pug4J

Spring-pug4j provides seamless integration between Spring Framework and Pug4J templating engine. It enables you to use Pug templates in your Spring MVC applications with full support for Spring's view resolution mechanism.

See [neuland/pug4j](https://github.com/neuland/pug4j) for more information about the Pug4J templating engine.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Features](#features)
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
    public ViewResolver viewResolver() {
        PugViewResolver viewResolver = new PugViewResolver();
        viewResolver.setEngine(pugEngine());
        viewResolver.setRenderContext(renderContext());  // Optional
        return viewResolver;
    }
}
```

**Note:** If `RenderContext` is not set, the view resolver will use default settings (prettyPrint=false, defaultMode=HTML, no global variables).

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

### Migrating from spring-pug4j 3.x to 4.0.0

Version 4.0.0 migrates from the deprecated `PugConfiguration` API (deprecated in pug4j 3.0.0) to the new `PugEngine` and `RenderContext` APIs.

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

**After (spring-pug4j 4.0.0 with pug4j 3.0+):**

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

### 4.0.0
* **Breaking**: Migrated from deprecated `PugConfiguration` to new `PugEngine` + `RenderContext` APIs
* Updated to pug4j 3.0.0
* Eliminated deprecation warnings
* See [Migration Guide](#migration-guide) above for upgrade instructions

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
  <version>4.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'de.neuland-bfi:spring-pug4j:4.0.0'
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

Copyright (C) 2012-2025 [neuland Büro für Informatik](http://www.neuland-bfi.de/), Bremen, Germany

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
