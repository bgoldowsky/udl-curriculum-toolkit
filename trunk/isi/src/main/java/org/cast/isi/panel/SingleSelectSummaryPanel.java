package org.cast.isi.panel;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Response;
import org.cast.isi.page.PeriodResponsePage;

public class SingleSelectSummaryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public SingleSelectSummaryPanel(String id) {
		super(id);
	}

	protected class StudentList extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		protected StudentList(String id, List<Response> responses) {
			super(id);
			RepeatingView byStudent = new RepeatingView("byStudent");
			add (byStudent);
			for (Response r : responses) {
				WebMarkupContainer container2 = new WebMarkupContainer(byStudent.newChildId());
				byStudent.add(container2);
				container2.add(new Label("name", r.getUser().getFullName()));
			}
			setOutputMarkupId(true);			
		}
		
		@Override
		public boolean isVisible() {
			return ((PeriodResponsePage)getPage()).isShowNames();
		}
	}

	protected class Icon extends Image {

		private static final long serialVersionUID = 1L;

		protected Icon(String id, String imageFile, String altText) {
			super(id);
			setImageResourceReference(new ResourceReference(Application.class, imageFile));
			add(new AttributeModifier("alt", true, new Model<String>(altText)));
			add(new AttributeModifier("title", true, new Model<String>(altText)));
		}

	}

}