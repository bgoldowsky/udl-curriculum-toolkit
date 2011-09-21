/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ISI's IDocumentObserver class that identifies and labels ISIXmlSections 
 * with 'section', 'page', and 'hasResponseGroup' labels.
 * 
 * @author jbrookover
 *
 */
public class XmlDocumentObserver implements IDocumentObserver {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XmlDocumentObserver.class);

	public void xmlUpdated(XmlDocument doc) {
		identifySections(doc);
	}

	/** Determine which ISIXmlSections should be marked as 'section' and 'page' and whether this section contains a response group */
	protected void identifySections (final XmlDocument doc) {
		for (XmlSection chapter : doc.getTocSection().getChildren()) {
			for (XmlSection sec : chapter.getChildren()) {
				ISIXmlSection xs = ((ISIXmlSection)sec);
				recurseSections(xs);
			}
		}
	}
	
	private void recurseSections(ISIXmlSection xs) {
		// Are you a one-page section?  If so, you're a section and a page...
		if (xs.getSectionAncestor() == null && (!xs.hasChildren() || xs.getElement().getLocalName().equals(ISIApplication.get().getSectionElement())))
			xs.setSection(true);
		if (xs.hasChildren()) {
			if (xs.getElement().getLocalName().equals(ISIApplication.get().getPageElement()))
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

}
