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

import java.util.SortedSet;
import java.util.TreeSet;

import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.User;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISISession;
import org.cast.isi.panel.PeriodStudentSelectPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Panel used by a teacher to add a new Period for the current Site.  Teacher is 
 * automatically added to the new Period.
 * 
 * @author lynnmccormack
 *
 */
public class AddPeriodPanel extends Panel {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AddPeriodPanel.class);
	protected NewPeriodForm newPeriodForm;
	private String addPeriodPanelMarkupId;

	@Inject
	private IEventService eventService;

	public AddPeriodPanel(String wicketId) {
		super(wicketId);
		
		this.setOutputMarkupPlaceholderTag(true);
		newPeriodForm = new NewPeriodForm("newPeriodForm");
		add(newPeriodForm);
		
		addPeriodPanelMarkupId = this.getMarkupId();
		this.setOutputMarkupId(true);
	}
	
	/**
	 * A form to add a new period.
	 */
	protected class NewPeriodForm extends Form<Period> {
		private static final long serialVersionUID = 1L;

		public NewPeriodForm(String wicketId) {
			super(wicketId);
			setDefaultModel(new HibernateObjectModel<Period>(SiteService.get().newPeriod()));
			
			TextField<String> periodName = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
			add(periodName);
			
			// Ensure that no two periods in the same site have the same name.
			periodName.add(new UniqueDataFieldValidator<String>(Period.class, "name").limitScope("site", ISISession.get().getCurrentSiteModel()));
			periodName.add(new SimpleAttributeModifier("maxlength", "32"));
			periodName.setRequired(true);
			periodName.setOutputMarkupId(true);

			final FeedbackPanel feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			feedback.setMaxMessages(1);
			feedback.setOutputMarkupId(true);
			add(feedback);
			
			add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					target.appendJavascript("$('#" + addPeriodPanelMarkupId + "').hide();");
				}
			});
			
			add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					if (target != null) {
						// update the period dropdown
						target.addChildren(getPage(), PeriodStudentSelectPanel.class);
						
						// replace the form
						NewPeriodForm tempNewPeriodForm =  new NewPeriodForm("newPeriodForm");
						tempNewPeriodForm.setOutputMarkupId(true);
						newPeriodForm.replaceWith(tempNewPeriodForm);
						newPeriodForm = tempNewPeriodForm;
						target.addComponent(newPeriodForm);
						
						// hide the form
						target.appendJavascript("$('#" + addPeriodPanelMarkupId + "').hide();");
					}	
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					if (target != null)
						target.addComponent(feedback);
				}
				
			});
			
		}


		@Override
		protected void onSubmit() {
			Period period = this.getModelObject();
			IModel<User> mUser = UserService.get().getById(ISISession.get().getUser().getId());

			// Add the new period
			period.setSite(ISISession.get().getCurrentSiteModel().getObject());
			SortedSet<User> userSet = new TreeSet<User>();
			userSet.add(mUser.getObject());
			period.setUsers(userSet);
			Databinder.getHibernateSession().save(period);
			
			// Update the teacher to connect with the new period
			User user = mUser.getObject();
			SortedSet<Period> periodSet = new TreeSet<Period>();
			periodSet.addAll(user.getPeriods());
			user.setPeriods(periodSet);
			user.getPeriods().add(period);
			Databinder.getHibernateSession().update(user);
			
			eventService.saveEvent("period:addPeriod", String.valueOf(getModelObject().getId()) + " added by userid " + String.valueOf(user.getId()), null);
		}
	}
}