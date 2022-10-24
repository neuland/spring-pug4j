[![Test Status](https://github.com/neuland/spring-pug4j/actions/workflows/test.yaml/badge.svg)](https://github.com/neuland/spring-pug4j/actions)

# A Spring Integration for Pug4J

See [neuland/pug4j](https://github.com/neuland/pug4j) for more information.

## Bean Declarations

applicationContext.xml

```xml
<bean id="templateLoader" class="de.neuland.pug4j.spring.template.SpringTemplateLoader">
	<property name="templateLoaderPath" value="classpath:/templates" />
</bean>

<bean id="expressionHandler" class="de.neuland.pug4j.expression.GraalJsExpressionHandler">
</bean>

<bean id="pugConfiguration" class="de.neuland.pug4j.PugConfiguration">
	<property name="prettyPrint" value="false" />
	<property name="caching" value="false" />
	<property name="templateLoader" ref="templateLoader" />
	<!--<property name="expressionHandler" ref="expressionHandler" />-->
</bean>

<bean id="viewResolver" class="de.neuland.pug4j.spring.view.PugViewResolver">
	<property name="configuration" ref="pugConfiguration" />
	<!-- rendering nice html formatted error pages for development -->
	<property name="renderExceptions" value="true" />
</bean>
```
Or, if you are using Spring JavaConfig:

```java
import de.neuland.pug4j.expression.GraalJsExpressionHandler;

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
    public PugConfiguration pugConfiguration() {
        PugConfiguration configuration = new PugConfiguration();
        configuration.setCaching(false);
        configuration.setTemplateLoader(templateLoader());
        //To use the new GraalJsExpressionHandler add this:
        //configuration.setExpressionHandler(new GraalJsExpressionHandler());
        return configuration;
    }

    @Bean
    public ViewResolver viewResolver() {
        PugViewResolver viewResolver = new PugViewResolver();
        viewResolver.setConfiguration(pugConfiguration());
        return viewResolver;
    }
}
```
### TemplateLoaderPath
SpringTemplateLoader uses the ResourceLoader of the Spring Framework. For more information how to configure the templateLoaderPath see [ResourceLoader](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/resources.html#resources-resourceloader)

Most people use something like 'classpath:/templates' which points to a templates folder in your resources.

## Usage

### via Maven

Just add following dependency definitions to your `pom.xml`.

```xml
<dependency>
  <groupId>de.neuland-bfi</groupId>
  <artifactId>spring-pug4j</artifactId>
  <version>2.0.6</version>
</dependency>
```

## Author

- Stefan Kuper / [planetk](https://github.com/planetk)
- Michael Geers / [naltatis](https://github.com/naltatis)
- Christoph Blömer / [chbloemer](https://github.com/chbloemer)

## License

The MIT License

Copyright (C) 2012-2022 [neuland Büro für Informatik](http://www.neuland-bfi.de/), Bremen, Germany

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
