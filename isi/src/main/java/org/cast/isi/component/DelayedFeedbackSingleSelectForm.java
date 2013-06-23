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

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISIDateLabel;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.panel.ISectionStatusChangeListener;
import org.cast.isi.service.ISectionService;

import java.util.Date;

/**
 * A multiple choice form.  This will have a {@link RadioGroup}&lt;String&gt; child with
 * several {@link DelayedFeedbackSingleSelectItem} children.
 *  
 * @author Don Roby
 *
 */
@Slf4j
public abstract class DelayedFeedbackSingleSelectForm extends SingleSelectForm implements ISectionStatusChangeListener, ISingleSelectItemChangeListener {

	private static final long serialVersionUID = 1L;

	@Inject
	protected ISectionService sectionService;

	@Getter
	protected String location;
	
	@Getter 
	@Setter
	protected boolean showDateTime = false;
	
	protected boolean lockResponse;
	
	public DelayedFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		super(id, mcPrompt);
	}

	public DelayedFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt,
			IModel<User> userModel, IModel<User> targetUserModel) {
		super(id, mcPrompt, userModel, targetUserModel);
	}

	public DelayedFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt,
			IModel<XmlSection> currentSectionModel) {
		super(id, mcPrompt);
		ISIXmlSection section = getIsiXmlSection(currentSectionModel);
		location = new ContentLoc(section.getSectionAncestor()).getLocation();
		lockResponse = (section != null) && section.isLockResponse();
	}
	
	public void onSectionCompleteChange(AjaxRequestTarget target, String location) {
		if (location.equals(getLocation()))
			target.addComponent(this);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (mResponse.getObject() == null)
			mResponse = responseService.newSingleSelectResponse(mTargetUser, getModel());
		add(new ResponseModelRadioGroup("radioGroup", mResponse));
	}
	
	@Override
	protected void onBeforeRender() {
		setEnabled(!((isComplete()  && isLockResponse())));
		super.onBeforeRender();
	}

	protected boolean isLockResponse() {
		return lockResponse;
	}

	protected boolean isComplete() {
		return nullSafeBoolean(sectionService.sectionIsCompleted(getUser(), location));
	}

	protected boolean isReviewed() {
		return nullSafeBoolean(sectionService.sectionIsReviewed(getUser(), location));			
	}

	private boolean nullSafeBoolean(Boolean b) {
		return (b != null) && b;
	}

	private ISIXmlSection getIsiXmlSection(IModel<XmlSection> model) {
		
		XmlSection section = model.getObject();
		if ((section == null) || (!(section instanceof ISIXmlSection)))
			return null;
		return (ISIXmlSection) section;
	}

	public void onSelectionChanged(AjaxRequestTarget target, SingleSelectItem selectedItem) {
		if (locateChild(selectedItem)) {
			log.debug("In form for {}, Single Select Option Clicked: {}", mResponse.getObject(), selectedItem.getDefaultModelObject());
			// Save Response
			responseService.saveSingleSelectResponse(mResponse, selectedItem.getModel().getObject(), selectedItem.isCorrect(), ((ISIBasePage)getPage()).getPageName());
			updateResponseModel();
			refreshListeners(target);
		}
	}

	private boolean locateChild(final SingleSelectItem selectedItem) {
		Object found = visitChildren(SingleSelectItem.class, new IVisitor<Component, Component>(){
            public void component(Component component, IVisit<Component> visit) {
                if (component == selectedItem) {
                    visit.stop(component);
                }
            }
        });
		
		return (found != null);
	}

	public class ResponseModelRadioGroup extends RadioGroup<String>  {

		private static final long serialVersionUID = 1L;

		public ResponseModelRadioGroup(String id, IModel<Response> mResponse) {
			super(id, new PropertyModel<String>(mResponse, "text"));
			ISIDateLabel date = new ISIDateLabel("date", new PropertyModel<Date>(mResponse, "lastUpdated"));
			date.setVisible(showDateTime);
			add(date);
		}
		
	}

}