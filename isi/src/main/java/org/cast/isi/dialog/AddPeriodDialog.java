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
package org.cast.isi.dialog;

import java.util.SortedSet;
import java.util.TreeSet;

import net.databinder.hib.Databinder;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.User;
import org.cast.cwm.data.validator.UniqueDataFieldValidator;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISISession;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.panel.PeriodStudentSelectPanel;
import org.cwm.db.service.IModelProvider;

import com.google.inject.Inject;

/**
 * Panel used by a teacher to add a new Period for the current Site.  Teacher is 
 * automatically added to the new Period.
 * 
 * @author lynnmccormack
 *
 */
public class AddPeriodDialog extends AbstractISIAjaxDialog<Void> {

	private static final long serialVersionUID = 1L;


	@Inject
	private IEventService eventService;

	@Inject
	protected ISiteService siteService;
	
	@Inject
	protected IModelProvider modelProvider;
	
	@Inject
	protected ICwmSessionService cwmSessionService;

	public AddPeriodDialog() {
		super();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setTitle((new StringResourceModel("addPeriod.title", this, null, "Add a New Period").getString()));		

		if (getPage() instanceof ISIBasePage) {
			dialogBorder.setPageName(((ISIBasePage) getPage()).getPageName());
			dialogBorder.setEventDetail("add period popup");
		} else {
			// Not on a base page
			dialogBorder.setLogEvents(false);
		}		

		dialogBorder.getBodyContainer().add(new NewPeriodForm("newPeriodForm"));
	}

	/**
	 * A form to add a new period.
	 */
	protected class NewPeriodForm extends Form<Period> {
		private static final long serialVersionUID = 1L;

		public NewPeriodForm(String wicketId) {
			super(wicketId);
			setDefaultModel(modelProvider.modelOf(siteService.newPeriod()));
			
			TextField<String> periodName = new TextField<String>("periodName", new PropertyModel<String>(getModel(), "name"));
			add(periodName);
			
			// Ensure that no two periods in the same site have the same name.
			periodName.add(new UniqueDataFieldValidator<String>(Period.class, "name").limitScope("site", cwmSessionService.getCurrentSiteModel()));
			periodName.add(new AttributeModifier("maxlength", "32"));
			periodName.setRequired(true);
			periodName.setOutputMarkupId(true);

			FeedbackPanel feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
			feedback.setMaxMessages(1);
			feedback.setOutputMarkupId(true);
			add(feedback);
			
			add(new AjaxFallbackLink<Object>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					target.appendJavaScript(dialogBorder.getCloseString());
				}
			});
			
			add(new AjaxSubmitLink("save") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					if (target != null) {
						// update the period dropdown
						target.addChildren(getPage(), PeriodStudentSelectPanel.class);
						target.appendJavaScript(dialogBorder.getCloseString());
					}	
				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					if (target != null)
						target.add(getParent().get("feedback"));
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