/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the UDL Curriculum Toolkit:
 * see <http://code.google.com/p/udl-curriculum-toolkit>.
 *
 * The UDL Curriculum Toolkit is free software: you can redistribute and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The UDL Curriculum Toolkit is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
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
