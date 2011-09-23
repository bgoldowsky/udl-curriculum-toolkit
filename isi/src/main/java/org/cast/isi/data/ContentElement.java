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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.cast.cwm.data.PersistedObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.w3c.dom.Element;

/**
 * A datastore representation of a location in the content for this application.  This
 * element could represent a page, a response area, an image, etc.  The total data set
 * of {@link ContentElement} objects is not comprehensive; they are only created if 
 * another data object needs to reference a content location.
 * 
 * @author jbrookover
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Getter 
@Setter
@ToString(of={"id"})
public class ContentElement extends PersistedObject implements Comparable<ContentElement> {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;
	
	private String contentLocation;
	
	private String xmlId;
	
	@Transient
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private transient ContentLoc locObject;
	
	protected ContentElement() { /* Empty constructor for datastore */ }
	
	public ContentElement(ContentLoc loc) {
		this(loc.getLocation(), null);
		this.locObject = loc;
	}
	
	public ContentElement(ContentLoc loc, String xmlId) {
		this(loc.getLocation(), xmlId);
		this.locObject = loc;
	}
	
	public ContentElement(String location, String xmlId) {
		this.contentLocation = location;
		this.xmlId = xmlId;
	}
	
	public ContentLoc getContentLocObject() {
		if (locObject == null)
			locObject = new ContentLoc(contentLocation);
		return locObject;
	}

	public Element getElement() {
		if (xmlId == null)
			return getContentLocObject().getSection().getElement();
		else
			return getContentLocObject().getSection().getXmlDocument().getDocument().getElementById(xmlId);
	}
	
	public int compareTo(ContentElement other) {
		if (other == null)
			return 1;
		int contentLocationDiff = this.getContentLocObject().compareTo(other.getContentLocObject());
		if (contentLocationDiff != 0)
			return contentLocationDiff;
		// They must have the same ContentLoc object; compare ordering of elements.
		if (getElement() == null)
			return -1;
		if (other.getElement() == null)
			return 1;
		return getElement().compareDocumentPosition(other.getElement());
	}
}
