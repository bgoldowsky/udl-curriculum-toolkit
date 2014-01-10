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
package org.cast.isi.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.file.File;
import org.cast.cwm.dav.DavResource;
import org.cast.cwm.xml.FileResource;

/**
 * A request mapper for authored content files.
 * 
 * Cache duration for files can be set, will default to {@link IResourceSettings#getDefaultCacheDuration()}.
 * Files are checked against the PackageResourceGuard before being delivered.
 *  
 */
public class ContentDirectoryMapper extends AbstractMapper {

	protected final String contentDirectory;

	protected final String davServer;
 	
	protected static final int COMPATIBILITY_SCORE = 10;

	public static final String CONTENT_DIRECTORY_MAPPER_PREFIX = "contentdir";

	public ContentDirectoryMapper(String contentDirectory, String davServer) {
		super();
		this.contentDirectory = contentDirectory;
		this.davServer = davServer;
	}

	// Mapper finds the URL that starts with the content
	public int getCompatibilityScore(Request request) {
		String requestUrlString = request.getUrl().getPath();
		if (requestUrlString.startsWith(CONTENT_DIRECTORY_MAPPER_PREFIX))
			return COMPATIBILITY_SCORE;
		return 0;
	}

	public IRequestHandler mapRequest(Request request) {		
		String path = request.getUrl().getPath();
		if (!path.startsWith(CONTENT_DIRECTORY_MAPPER_PREFIX))
			return null;

		// strip off the prefix
		path = path.substring(CONTENT_DIRECTORY_MAPPER_PREFIX.length());		
		
        if (davServer != null) {
            DavResource contentResource = new DavResource(davServer, contentDirectory + path);
            return new ResourceRequestHandler(contentResource, null);
        } else {
        	String filePath = contentDirectory + path;
        	File contentFile = new File(filePath);
            FileResource contentResource = new FileResource(contentFile);
            return new ResourceRequestHandler(contentResource, null);
        }		
	}

	public Url mapHandler(IRequestHandler requestHandler) {
		return null;
	}
}