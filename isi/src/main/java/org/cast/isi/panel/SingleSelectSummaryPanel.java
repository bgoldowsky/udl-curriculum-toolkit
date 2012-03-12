package org.cast.isi.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.cast.cwm.data.Response;
import org.cast.isi.page.PeriodResponsePage;

public class SingleSelectSummaryPanel extends Panel {
	
	@Getter @Setter
	protected boolean showNames = false;
	
	private static final long serialVersionUID = 1L;

	public SingleSelectSummaryPanel (String wicketId, Map<Integer, List<Response>> responseMap) {
		super(wicketId);
		setOutputMarkupId(true);
		
		RepeatingView byTries = new RepeatingView("byTries");
		add(byTries);
		
		// Put into order 1...N tries, then 0 (which means never correctly answered)
		List<Integer> sorted = new ArrayList<Integer>(responseMap.keySet());
		Collections.sort(sorted);
		if (sorted.get(0) == 0) {
			sorted.remove(0);
			sorted.add(0);
		}
		
		for (Integer tries : sorted) {
			WebMarkupContainer container = new WebMarkupContainer(byTries.newChildId());
			byTries.add(container);
			List<Response> responses = responseMap.get(tries);
			container.add(new SingleSelectScoreIndicator("score", new HibernateObjectModel<Response>(responses.get(0)), tries>0));
			container.add(new Label("count", String.valueOf(responses.size())));
			
			WebMarkupContainer studentList = new WebMarkupContainer("studentList") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() {
					return ((PeriodResponsePage)getPage()).isShowNames();
				}
			};
			studentList.setOutputMarkupId(true);			
			container.add(studentList);
			
			RepeatingView byStudent = new RepeatingView("byStudent");
			studentList.add (byStudent);
			for (Response r : responses) {
				WebMarkupContainer container2 = new WebMarkupContainer(byStudent.newChildId());
				byStudent.add(container2);
				container2.add(new Label("name", r.getUser().getFullName()));
			}
		}
		
		
		
	}

}
