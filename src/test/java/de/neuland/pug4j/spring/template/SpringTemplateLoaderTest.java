package de.neuland.pug4j.spring.template;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SpringTemplateLoaderTest {

    private ResourceLoader resourceLoader;

    @Before
    public void setUp() throws Exception {
        resourceLoader = mock(ResourceLoader.class);
        Resource resource = mock(Resource.class);
        String initialString = "text";
        InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        when(resource.getInputStream()).thenReturn(targetStream);
        when(resourceLoader.getResource(any())).thenReturn(resource);

    }

    @Test
    public void shouldCreateCorrectExtension() throws Exception {
        //given
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".jade");
        springTemplateLoader.setTemplateLoaderPath("classpath:/templates");

        //when
        String extension = springTemplateLoader.getExtension();
        Reader reader = springTemplateLoader.getReader("test");

        //then
        verify(resourceLoader).getResource(eq("classpath:/templates/test.jade"));
        assertEquals("jade",extension);
    }
    @Test
    public void shouldCreateCorrectExtensionPug() throws Exception {
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".pug");
        String extension = springTemplateLoader.getExtension();
        assertEquals("pug",extension);
    }
    @Test
    public void shouldCreateCorrectResourcePath() throws Exception {
        //given
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".jade");
        springTemplateLoader.setBase("templates");
        springTemplateLoader.setTemplateLoaderPath("classpath:/");

        //when
        String extension = springTemplateLoader.getExtension();
        Reader reader = springTemplateLoader.getReader("/test");

        //then
        verify(resourceLoader).getResource(eq("classpath:/templates/test.jade"));
        assertEquals("jade",extension);
    }
    @Test
    public void shouldCreateCorrectResourcePathOnEmptyTemplateLoaderPath() throws Exception {
        //given
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".jade");
        springTemplateLoader.setBase("templates");
        springTemplateLoader.setTemplateLoaderPath("");

        //when
        String extension = springTemplateLoader.getExtension();
        Reader reader = springTemplateLoader.getReader("classpath:/templates/test.jade");

        //then
        verify(resourceLoader).getResource(eq("classpath:/templates/test.jade"));
        assertEquals("jade",extension);
    }

    @Test
    public void shouldCreateCorrectResourcePathWithRelativeFilename() throws Exception {
        //given
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".jade");
        springTemplateLoader.setBase("");
        springTemplateLoader.setTemplateLoaderPath("classpath:/templates");

        //when
        String extension = springTemplateLoader.getExtension();
        Reader reader = springTemplateLoader.getReader("test.jade");

        //then
        verify(resourceLoader).getResource(eq("classpath:/templates/test.jade"));
        assertEquals("jade",extension);
    }

    @Test
    public void shouldCreateCorrectResourcePathWithWindowsLetterPath() throws Exception {
        //given
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".jade");
        springTemplateLoader.setBase("");
        springTemplateLoader.setTemplateLoaderPath("file:C:\\templates");

        //when
        String extension = springTemplateLoader.getExtension();
        Reader reader = springTemplateLoader.getReader("test.jade");

        //then
        verify(resourceLoader).getResource(eq("file:C:/templates/test.jade"));
        assertEquals("jade",extension);
    }

    @Test
    public void shouldCreateCorrectResourcePathWithDotPath() throws Exception {
        //given
        SpringTemplateLoader springTemplateLoader = new SpringTemplateLoader();
        springTemplateLoader.setResourceLoader(resourceLoader);
        springTemplateLoader.setSuffix(".jade");
        springTemplateLoader.setBase("");
        springTemplateLoader.setTemplateLoaderPath("classpath:/templates");

        //when
        String extension = springTemplateLoader.getExtension();
        Reader reader = springTemplateLoader.getReader("../test.jade");

        //then
        verify(resourceLoader).getResource(eq("classpath:/test.jade"));
        assertEquals("jade",extension);
    }

}