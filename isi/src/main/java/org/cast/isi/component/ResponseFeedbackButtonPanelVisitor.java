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

import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.isi.panel.ResponseFeedbackButtonPanel;

/**
 * * Visits @ResponseFeedbackButtonPanel and determines what kind of feedback exists
 *
 */
public class ResponseFeedbackButtonPanelVisitor implements IVisitor<ResponseFeedbackButtonPanel, Void> {

	public String state = null;
	
	public ResponseFeedbackButtonPanelVisitor() {
	}

    public void component(ResponseFeedbackButtonPanel component, IVisit visit) {
        String currentState = component.getState();

        // once a new feedback is found - stop, this will be the indicator
        if (currentState.equals("new")) {
            state="new";
            visit.stop();
        } else if (currentState.equals("old") && (state == null)) {
            state="old";
        }
    }
}
