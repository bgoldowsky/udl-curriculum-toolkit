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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ISIXmlSection extends XmlSection {
	
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIXmlSection.class);
	
	public static enum SectionType { PAGE, SECTION };

	@Getter @Setter protected String subtitle = "";
	
	// The following four structural flags tell the application how to treat this XML element.
	// They are set by XmlDocumentObserver when the file is read.
	// One or more may be set.
	
	/**
	 * True if this is an XML element that will be displayed as a single page in the app.
	 */
	@Getter @Setter protected boolean page = false;

	/**
	 * True if this is considered a "section".
	 * This is expected to be one level above pages in general, although
	 * you can have one-page sections where there is no child element
	 * for the page, so page and section would both be true.
	 */
	@Getter @Setter protected boolean section = false;

	/**
	 * True if this is one level above the "section" level.
	 */
	@Getter @Setter protected boolean superSection = false;
	
	/**
	 * True if this XML element is considered the "chapter" level.
	 * This is, for example, the extent of what is presented in the top navigation bar.
	 */
	@Getter @Setter protected boolean chapter = false;
	
	
	/**
	 * True if a page, or a section that contains such a page, has a response group.
	 */
	@Getter @Setter protected boolean hasResponseGroup = false;

	/**
	 * True if this XML element or a parent has a behavior="lock-response" attribute
	 * This is set in the xml and controls whether the student can change his response after submission.
	 */
	@Getter protected boolean lockResponse;

	/**
	 * True if this XML element or a parent has a behavior="delayed-feedback" attribute
	 * This is set in the xml and controls whether the student sees immediate feedback on his response.
	 */
	@Getter protected boolean delayFeedback;
	
	public void init (XmlDocument document, XmlSection parent, String id, Element elt, String title) {
		super.init(document, parent, id, elt, title);
		Element element = getElement();
		if (element != null) {
			lockResponse = hasLockResponse(parent) || hasAttribute(element, "behavior", "lock-response");
			delayFeedback = hasDelayedFeedback(parent) || hasAttribute(element, "behavior", "delay-feedback");
			NodeList children = element.getChildNodes();
			
			// Get the subtitle
			for (int i=0; i<children.getLength(); i++) {
				if (children.item(i) instanceof Element) {
					Element child = (Element) children.item(i);
					
					// Set the subtitle (unique to ISI?)
					if (child.getLocalName().equals("bridgehead"))
						this.subtitle = child.getTextContent();
				}
			}
			
			// Determine if this element has any response groups
			if (element.getElementsByTagName("responsegroup").getLength() > 0)
				hasResponseGroup = true;
		}
	}

	private boolean hasDelayedFeedback(XmlSection parent) {
		if ((parent == null) || (!(parent instanceof ISIXmlSection)))
			return false;
		return ((ISIXmlSection) parent).isDelayFeedback();
	}

	private boolean hasLockResponse(XmlSection parent) {
		if ((parent == null) || (!(parent instanceof ISIXmlSection)))
			return false;
		return ((ISIXmlSection) parent).isLockResponse();
	}

	@Override
	public ISIXmlSection getParent() {
		return (ISIXmlSection) parent;
	}
	
	@Override
	public ISIXmlSection getNext() {
		return (ISIXmlSection) super.getNext();
	}
	
	@Override
	public ISIXmlSection getPrev() {
		return (ISIXmlSection) super.getPrev();
	}
	
	public ISIXmlSection getNextPage() {
		ISIXmlSection next = getNext();
		if (next != null) {
			return next;
		} else {
			if (getParent().getNext() == null)
				return null;
			else
				return getParent().getNext().firstPage();
		}
	}
	
	public ISIXmlSection getPrevPage() {
		ISIXmlSection prev = getPrev();
		if (prev != null) {
			return prev;
		} else {
			if (getParent().getPrev() == null)
				return null;
			else
				return getParent().getPrev().lastPage();
		}
	}
	
	/** Return ancestor (or self) that is designated as a section */
	public ISIXmlSection getSectionAncestor() {
		if (isSection()) 
			return this;
		if (parent != null)
			return getParent().getSectionAncestor();
		return null;
	}

	/**
	 * Return list of children that are sections, or returns this
	 * object if it is a section itself.
	 * 
	 * @return
	 */
	public List<ISIXmlSection> getSectionChildren() {
		List<ISIXmlSection> secs = new ArrayList<ISIXmlSection>();
		if (isSection()) {
			secs.add(this);
			return secs;
		}
		for (XmlSection s : children) {
			ISIXmlSection is = (ISIXmlSection) s;
			if (is.isSection())
				secs.add(is);
			else if (is.hasChildren())
				secs.addAll(is.getSectionChildren());
		}
		return secs;
	}

	/**
	 * Return first descendant section that is marked as a page.
	 * @return
	 */
	public ISIXmlSection firstPage() {
		if (isPage())
			return this;
		if (children != null) {
			for (XmlSection child : children) {
				ISIXmlSection firstInChild = ((ISIXmlSection)child).firstPage();
				if (firstInChild != null)
					return firstInChild;
			}
		}
		return null;
	}
	
	/**
	 * Return last descendent section that is marked as a page.
	 * @return
	 */
	public ISIXmlSection lastPage() {
		if (isPage())
			return this;
		if (children != null) {
			for (int i = children.size() - 1; i >=0; i--) {
				ISIXmlSection lastInChild = ((ISIXmlSection) children.get(i)).lastPage();
				if (lastInChild != null)
					return lastInChild;
			}
		
		}
		return null;
	}
	
	/**
	 * Return the first descendant page (or yourself) that has a response group
	 * 
	 * @return
	 */
	public ISIXmlSection firstPageWithResponseGroup() {
		if (isPage() && hasResponseGroup)
			return this;
		if (children != null) {
			for (XmlSection child : children) {
				ISIXmlSection firstInChild = ((ISIXmlSection) child).firstPageWithResponseGroup();
				if (firstInChild != null)
					return firstInChild;
			}
		}
		return null;
	}
	
	/**
	 * Return first descendant section that is marked as a section.
	 * @return
	 */
	public ISIXmlSection firstSection() {
		if (isSection())
			return this;
		if (children != null) {
			for (XmlSection child : children) {
				ISIXmlSection firstInChild = ((ISIXmlSection)child).firstSection();
				if (firstInChild != null)
					return firstInChild;
			}
		}
		return null;
	}
	
	/**
	 * Return last descendant section that is marked as a section.
	 * @return
	 */
	public ISIXmlSection lastSection() {
		ISIXmlSection last = null;
		
		if (isSection())
			return this;
		if (children != null) {
			for (XmlSection child : children) {
				ISIXmlSection lastInChild = ((ISIXmlSection) child).lastSection();
				if (lastInChild != null)
					last = lastInChild;
			}
		}
		return last;
	}
	
	/**
	 * Retrieve this section's page ancestor, or null if there is none.
	 * 
	 * @return
	 */
	public ISIXmlSection getPageAncestor() {
		if (isPage()) {
			return this;
		} else if (parent != null){
			return ((ISIXmlSection) parent).getPageAncestor();
		} else {
			return null;
		}
	}

	@Override
	public List<? extends Serializable> getLabels() {
		List<SectionType> labels = new ArrayList<SectionType>();
		if (isPage())
			labels.add(SectionType.PAGE);
		if (isSection())
			labels.add(SectionType.SECTION);
		return labels;
	}

	public String getCrumbTrailAsString(int stripStart, int stripEnd) {
		String crumbTrail = new String();
		List<XmlSectionModel> breadCrumbList = this.getBreadcrumbs(stripStart, stripEnd);
		for (XmlSectionModel sec : breadCrumbList) {
			// if you are at the end, don't add the arrow
			if (sec.equals(breadCrumbList.get(breadCrumbList.size()-1))) {
				crumbTrail = crumbTrail + sec.getObject().getTitle();
			} else {
				crumbTrail = crumbTrail + sec.getObject().getTitle() + " > " ;
			}
		}

		return crumbTrail;
	}
	
	
	/**
	 * @param section
	 * @return the root (level 1) section for this page or section
	 */
	public static ISIXmlSection getRootSection(ISIXmlSection section) {

		for (XmlDocument doc : ISIApplication.get().getStudentContent()) { // For each XML document
			for (XmlSection chapter: doc.getTocSection().getChildren()) { // get all chapters (level1)
				if (chapter.isAncestorOf(section)) { // Determine which chapter is parent
					return (ISIXmlSection) chapter;
				}; 	
			}
		}
		return null;  // there was no chapter found!
	}

	public boolean hasResponseGroup() {
		return hasResponseGroup;
	}
	
	private boolean hasAttribute(Element element, String attribute, String value) {
		return matches(element.getAttribute(attribute), value);
	}

	private boolean matches(String attributeValue, String checkValue) {
		return (attributeValue != null) && (attributeValue.contains(checkValue));
	}

}