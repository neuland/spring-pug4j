package de.neuland.pug4j.spring.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import de.neuland.pug4j.template.TemplateLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;

public class SpringTemplateLoader implements TemplateLoader, ServletContextAware {

	private ResourceLoader resourceLoader;
	private String encoding = "UTF-8";
	private String suffix = ".pug";
	private String basePath = "";
	private ServletContext context;

	@PostConstruct
	public void init() {
		if(this.resourceLoader == null) {
			this.resourceLoader = new ServletContextResourceLoader(context);
		}
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public long getLastModified(String name) {
		Resource resource = getResource(name);
		try {
			return resource.lastModified();
		} catch (IOException ex) {
			return -1;
		}
	}

	@Override
	public Reader getReader(String name) throws IOException {
		Resource resource = getResource(name);
		return new InputStreamReader(resource.getInputStream(), encoding);
	}

	@Override
	public String getExtension() {
		return suffix.substring(1,suffix.length());
	}

	private Resource getResource(String name) {
		String resourceName = basePath + name;
        if (hasNoExtension(resourceName)) {
			resourceName += suffix;
		}
		return this.resourceLoader.getResource(resourceName);
	}

    private boolean hasNoExtension(String filename) {
        return "".equals(FilenameUtils.getExtension(filename));
    }

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

}
