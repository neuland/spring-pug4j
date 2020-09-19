package de.neuland.pug4j.spring.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;

import de.neuland.pug4j.template.TemplateLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.apache.commons.io.FilenameUtils;

public class SpringTemplateLoader implements TemplateLoader, ResourceLoaderAware {

	private ResourceLoader resourceLoader;
	private String encoding = "UTF-8";
	private String suffix = ".pug";
	private String templateLoaderPath = "";
	private String base = "";


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
		String resourceName = getResourceName(name);
        if (hasNoExtension(resourceName)) {
			resourceName += suffix;
		}
		return this.resourceLoader.getResource(resourceName);
	}

	private String getResourceName(String name){
		if(!StringUtils.isBlank(templateLoaderPath))
			if(Paths.get(name).isAbsolute()) {
				return Paths.get(templateLoaderPath+ base +name.substring(1)).toString();
			}else
				return Paths.get(templateLoaderPath).resolve(name).toString();
		else
			return name;
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

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setTemplateLoaderPath(String templateLoaderPath) {
		if(templateLoaderPath.endsWith("/"))
			this.templateLoaderPath = templateLoaderPath;
		else
			this.templateLoaderPath = templateLoaderPath+"/";
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
