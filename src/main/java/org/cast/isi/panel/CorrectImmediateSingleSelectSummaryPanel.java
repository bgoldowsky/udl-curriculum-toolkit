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
package org.cast.isi.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.cast.cwm.data.Response;

public class CorrectImmediateSingleSelectSummaryPanel extends SingleSelectSummaryPanel {
	
	private static final long serialVersionUID = 1L;

	public CorrectImmediateSingleSelectSummaryPanel (String wicketId, Map<Integer, List<Response>> responseMap) {
		super(wicketId);
		setOutputMarkupId(true);
		
		RepeatingView byTries = new RepeatingView("byTries");
		add(byTries);
		
		List<Integer> sorted = new ArrayList<Integer>(responseMap.keySet());
		Collections.sort(sorted);
		
		for (Integer tries : sorted) {
			WebMarkupContainer container = new WebMarkupContainer(byTries.newChildId());
			byTries.add(container);
			List<Response> responses = responseMap.get(tries);
			container.add(new SingleSelectScoreIndicator("score", new HibernateObjectModel<Response>(responses.get(0)), tries>0));
			container.add(new Label("count", String.valueOf(responses.size())));
			container.add(new StudentList("studentList", responses));
			
		}
	}

}
