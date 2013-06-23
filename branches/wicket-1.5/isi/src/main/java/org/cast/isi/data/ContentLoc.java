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
package org.cast.isi.data;

import com.google.inject.Inject;
import lombok.Getter;
import org.apache.wicket.injection.Injector;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.isi.ISIXmlSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A convenience object that can be used to access a {@link ISIXmlSection} object
 * using a location string.  It maintains a transient reference to the 
 * ISIXmlSection object.
 * 
 * @author jbrookover
 *
 */
public class ContentLoc implements Serializable, Comparable<ContentLoc> {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ContentLoc.class);
	
	@Getter private final String location;
	@Getter private final String fileName;
	@Getter private final String id;
	private transient ISIXmlSection section = null;

	@Inject
	private IXmlService xmlService;


	/**
	 * Constructor from a location string, previously generated
	 * by {@link #getLocation()}.
	 * @param location
	 */
	public ContentLoc(String location) {
        Injector.get().inject(this);
		this.location = location;
		int index = location.lastIndexOf('_');
		if (index < 1 || index>location.length()-2)
			throw new IllegalArgumentException("File location malformed: " + location);
		this.fileName = location.substring(0, index);
		this.id = location.substring(index+1);
	}


    /**
	 * Constructor from an existing {@link XmlSection}.
	 * @param sec
	 */
	public ContentLoc (XmlSection sec) {
        Injector.get().inject(this);
		if (sec == null)
			throw new IllegalArgumentException("sec cannot be null in ContentLoc");
		if (sec.getXmlDocument() == null)
			throw new IllegalArgumentException("sec without document in ContentLoc");
		this.fileName = sec.getXmlDocument().getName();
		this.id = sec.getId();
		this.location = this.fileName + '_' + this.id;
	}

	/** Look up XmlSection for a given content locator.
	 * 
	 * @return
	 */
	public ISIXmlSection getSection() {
		if (section == null) {
			XmlDocument document = xmlService.getDocument(fileName);
			if (fileName == null || document == null) {
				log.error("Locator refers to nonexistent content file: " + this);
				return null;
			}
			section = (ISIXmlSection) document.getById(id);
			if (section == null)
				log.error("Locator refers to nonexistent ID: " + this);
		}
		return section;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContentLoc) {
			ContentLoc that = (ContentLoc) obj;
			return (this.location.equals(that.location));
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "[" + location + "]";
	}

	public int compareTo(ContentLoc other) {
		if (other == null)
			return 1;
		return this.getSection().compareTo(other.getSection());
	}
}
