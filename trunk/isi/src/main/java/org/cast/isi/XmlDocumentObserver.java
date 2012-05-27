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

import org.cast.cwm.xml.IDocumentObserver;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;

/**
 * ISI's IDocumentObserver class that identifies and labels ISIXmlSections 
 * with 'section', 'page', and 'hasResponseGroup' labels.
 * 
 * @author jbrookover
 *
 */
public class XmlDocumentObserver implements IDocumentObserver {

	String sectionElementName = ISIApplication.get().getSectionElement();
	String pageElementName = ISIApplication.get().getPageElement();
	
	private static final long serialVersionUID = 1L;

	public void xmlUpdated(XmlDocument doc) {
		identifySections(doc);
	}

	/** Determine which ISIXmlSections should be marked as 'section' and 'page' and whether this section contains a response group */
	protected void identifySections (final XmlDocument doc) {
		for (XmlSection chapter : doc.getTocSection().getChildren()) {
			((ISIXmlSection)chapter).setChapter(true);
			for (XmlSection sec : chapter.getChildren()) {
				ISIXmlSection xs = ((ISIXmlSection)sec);
				recurseSections(xs);
			}
		}
	}
	
	private void recurseSections(ISIXmlSection xs) {
		// Are you a supersection?
		if (xs.hasChildren() && atSectionLevel((ISIXmlSection) xs.getChild(0)))
			xs.setSuperSection(true);
		// Are you a one-page section?  If so, you're a section and a page...
		if (xs.getSectionAncestor() == null && (!xs.hasChildren() || atSectionLevel(xs)))
			xs.setSection(true);
		if (xs.hasChildren()) {
			if (atPageLevel(xs))
				xs.setPage(true);
			for (XmlSection child : xs.getChildren()) {
				recurseSections((ISIXmlSection) child);
				if (((ISIXmlSection)child).hasResponseGroup()) {
					xs.setHasResponseGroup(true);
				}
			}
			
		} else if (xs.getPageAncestor() == null) {
			xs.setPage(true);
		}
	}

	private boolean atSectionLevel (ISIXmlSection xs) {
		return xs.getElement().getLocalName().equals(sectionElementName);
	}

	private boolean atPageLevel (ISIXmlSection xs) {
		return xs.getElement().getLocalName().equals(pageElementName);
	}

}
