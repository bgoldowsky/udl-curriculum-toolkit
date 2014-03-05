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
package org.cast.isi.component;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * A container for a message tied to a SingleSelectItem.
 * It makes itself visible whenever the associated item is selected.
 * 
 * @author bgoldowsky
 *
 */
public class SingleSelectMessage extends WebMarkupContainer {

	private String itemId;

	private static final long serialVersionUID = 1L;

	/**
	 * Construct with the wicket ID of the SingleSelectItem to be connected.
	 * It is looked for as a sibling of this component.
	 * @param id
	 * @param itemId
	 */
	public SingleSelectMessage(String id, String itemId) {
		super(id);
		this.itemId = itemId;
	}

    @Override
    protected void onConfigure() {
        super.onConfigure();
        Component item =  getParent().get(itemId);
        if (item == null || !(item instanceof SingleSelectItem))
            throw new IllegalArgumentException("'for' attribute of SingleSelectMessage '" + itemId
                    + "' doesn't correspond to a SingleSelectItem; it is " + item);

        setVisible(((SingleSelectItem)item).isSelected());
    }
}
