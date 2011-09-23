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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

/**
 * A serializer filter that removes any elements with a wicket:id, but leaves their contents.
 * Used in ISI to remove dynamic components like glossary links when in a context that they 
 * cannot be processed (raw HTML is being extracted and output to page).
 * 
 * Singleton class since no state is kept, only one instance is ever needed.
 * 
 * @author bgoldowsky
 *
 */

public class DynamicComponentRemoverFilter implements LSSerializerFilter {
	
	private static DynamicComponentRemoverFilter instance = new DynamicComponentRemoverFilter();
	
	public static DynamicComponentRemoverFilter get() {
		return instance;
	}
	
	private DynamicComponentRemoverFilter() {
	}

	public int getWhatToShow() {
		return NodeFilter.SHOW_ELEMENT;
	}

	// Skip any nodes with a wicket:id.  This means the tag won't be output, but its contents will.
	public short acceptNode(Node n) {
		if (((Element) n).hasAttributeNS("http://wicket.apache.org", "id"))
			return NodeFilter.FILTER_SKIP;
		else
			return NodeFilter.FILTER_ACCEPT;
	}
	
}