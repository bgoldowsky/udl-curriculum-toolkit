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

import java.util.Arrays;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISIDateLabel;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.panel.ISectionCompleteToggleListener;
import org.cast.isi.panel.StudentScorePanel;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

/**
 * A multiple choice form.  This will have a {@link RadioGroup}&lt;String&gt; child with
 * several {@link DelayedFeedbackSingleSelectItem} children.
 *  
 * @author Don Roby
 *
 */
@Slf4j
public class DelayedFeedbackSingleSelectForm extends SingleSelectForm implements ISectionCompleteToggleListener, ISingleSelectItemChangeListener {

	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;

	@Getter
	private String location;
	
	@Getter 
	@Setter
	private boolean showDateTime = false;
	
	private boolean lockResponse;
	
	public DelayedFeedbackSingleSelectForm(String id, IModel<Prompt> mcPrompt, IModel<XmlSection> currentSectionModel) {
		super(id, mcPrompt);
		ISIXmlSection section = getIsiXmlSection(currentSectionModel);
		location = new ContentLoc(section.getSectionAncestor()).getLocation();
		lockResponse = (section != null) && section.isLockResponse();
	}
	
	private ISIXmlSection getIsiXmlSection(IModel<XmlSection> model) {
		
		XmlSection section = model.getObject();
		if ((section == null) || (!(section instanceof ISIXmlSection)))
			return null;
		return (ISIXmlSection) section;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (mResponse.getObject() == null)
			mResponse = responseService.newSingleSelectResponse(mTargetUser, getModel());
		add(new StudentScorePanel("mcScore", Arrays.asList(mResponse)){

			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isVisible() {
				return isReviewed();
			}
		});
		RadioGroup<String> radioGroup = new RadioGroup<String>("radioGroup", new Model<String>(mResponse.getObject().getText()));
		add(radioGroup);

		// Last-updated timestamp
		ISIDateLabel date = new ISIDateLabel("date", new PropertyModel<Date>(mResponse, "lastUpdated"));
		date.setVisible(showDateTime);
		radioGroup.add(date);
	}
	
	
	@Override
	protected void onBeforeRender() {
		if (isComplete()  && isLockResponse())
			setEnabled(false);
		super.onBeforeRender();
	}

	private boolean isLockResponse() {
		return lockResponse;
	}

	private boolean isComplete() {
		return nullSafeBoolean(sectionService.sectionIsCompleted(getUser(), location));
	}

	private boolean isReviewed() {
		return nullSafeBoolean(sectionService.sectionIsReviewed(getUser(), location));			
	}

	private boolean nullSafeBoolean(Boolean b) {
		return (b != null) && b;
	}

	public void onSelectionChanged(AjaxRequestTarget target, SingleSelectItem selectedItem) {
		log.debug("Single Select Option Clicked: {}", selectedItem.getDefaultModelObject());
		get("radioGroup:selectNone").setVisible(false);
		// Save Response
		responseService.saveSingleSelectResponse(mResponse, selectedItem.getModel().getObject(), selectedItem.isCorrect(), ((ISIBasePage)getPage()).getPageName());
	}

}