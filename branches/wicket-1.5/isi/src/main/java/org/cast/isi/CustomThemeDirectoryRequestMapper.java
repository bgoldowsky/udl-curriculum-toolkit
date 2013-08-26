package org.cast.isi;

import java.io.File;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.resource.IResourceStream;
import org.cast.cwm.ThemeDirectoryRequestMapper;

/**
 * A theme directory mapper that checks a custom theme directory first and then falls back
 * to the original theme directory if the file is not found.
 *
 */
public class CustomThemeDirectoryRequestMapper extends ThemeDirectoryRequestMapper {

	protected final String customThemeDirectory;
	
	public CustomThemeDirectoryRequestMapper(File themeDirectory, File customThemeDirectory, String... prefixes) {
		super(themeDirectory, prefixes);
		this.customThemeDirectory = customThemeDirectory.getAbsolutePath();
	}

	@Override
	public IRequestHandler mapRequest(Request request) {
		String path = request.getUrl().getPath();
		for (String prefix : prefixes) {
			if (path.startsWith(prefix)) {
				IResourceStream customStream = getResourceStream(customThemeDirectory + "/" + path);
				if (customStream==null) {
					// check original theme
					IResourceStream stream = getResourceStream(themeDirectory + "/" + path);
					if (stream==null)
						return null;
					return getResourceStreamRequestHandler(stream);
				}
				return getResourceStreamRequestHandler(customStream);
			}
		}
		return null;
	}

	private IRequestHandler getResourceStreamRequestHandler(IResourceStream stream) {
		ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(stream);
		if (cacheDuration != null)
			handler.setCacheDuration(cacheDuration);
		return handler;
	}

}
