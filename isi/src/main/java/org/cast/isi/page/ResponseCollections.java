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
package org.cast.isi.page;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.StringResourceModel;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResponseCollections are groups of response areas that are collected under a response name.
 * These groupings are pre-determined in the xml.  
 * 
 * @author lynnmccormack
 *
 */
@AuthorizeInstantiation("STUDENT")
public class ResponseCollections extends ISIStandardPage {
	
	private String paramCollectionName = null; // The name of the ResponseCollection currently being displayed
	private String pageTitleEnd;
	private UserModel mUser;
	private boolean isTeacher = false;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResponseCollections.class);
	protected static ResponseMetadata responseMetadata = new ResponseMetadata();
	static {
		responseMetadata.addType("HTML");
		responseMetadata.addType("AUDIO");
		responseMetadata.addType("SVG");
		responseMetadata.addType("UPLOAD");
	}
	
	public ResponseCollections(final PageParameters parameters) {
		super(parameters);

		// set teacher flag and target user
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		if (isTeacher) {
			mUser = new UserModel(ISISession.get().getStudent());
		} else {
			mUser = new UserModel(ISISession.get().getUser());			
		}

		pageTitleEnd = (new StringResourceModel("ResponseCollections.pageTitle", this, null, "Collections").getString());
		setPageTitle(pageTitleEnd);
		
		paramCollectionName = parameters.getString("name");
		
		if (StringUtils.isEmpty(paramCollectionName)) {
			add(new WebMarkupContainer("collectionTitle").setVisible(false));
		} else {
			add(new Label("collectionTitle", paramCollectionName));
		}
						
		// components on the left side
		RepeatingView rvCollectionList = new RepeatingView("collectionList");
		add(rvCollectionList);
		
		List<String> listNames = null;
		if (mUser.getObject() != null) {
			listNames = ISIResponseService.get().getResponseCollectionNames(mUser);
		}
		
		if (!(listNames == null)) {
			String collectionName;
			for (String s : listNames) {
				collectionName = s;
				WebMarkupContainer wmc = new WebMarkupContainer(rvCollectionList.newChildId());
				BookmarkablePageLink<Page> bpl = new BookmarkablePageLink<Page>("link", ISIApplication.get().getResponseCollectionsPageClass())
												.setParameter("name", collectionName);
				bpl.add(new Label("name", collectionName));
				
				// if the param collection name is the same as this one set the indicator that this is the item clicked
				if (!StringUtils.isEmpty(paramCollectionName)) {
					if (paramCollectionName.equals(collectionName)) {
						bpl.add(new SimpleAttributeModifier("class", "selected"));
						bpl.setEnabled(false);
					}
				}

				wmc.add(bpl);
				rvCollectionList.add(wmc);
			}
		} else {
			rvCollectionList.add((new WebMarkupContainer("link")).setVisible(false))
							.add(new WebMarkupContainer("name")).setVisible(false);
		}

		// components on the right side of the form		
		WebMarkupContainer wmcNoModels = new WebMarkupContainer("noModels");
		add(wmcNoModels);
		WebMarkupContainer wmcNoModelSelected = new WebMarkupContainer("noModelSelected");
		add(wmcNoModelSelected);
		add(new WebMarkupContainer("noStudentSelected").setVisible(mUser.getObject() == null));   	

		if (mUser.getObject() != null) {
			wmcNoModelSelected.setVisible((StringUtils.isEmpty(paramCollectionName)) &&  
					!((listNames == null) || (listNames.isEmpty())));
			wmcNoModels.setVisible((StringUtils.isEmpty(paramCollectionName)) && 
					((listNames == null) || (listNames.isEmpty())));
		} else {
			wmcNoModelSelected.setVisible(false);
			wmcNoModels.setVisible(false);
		}
		
		RepeatingView rvPromptResponseList = new RepeatingView("promptResponseRepeater");
		add(rvPromptResponseList);

		if (!StringUtils.isEmpty(paramCollectionName)) {
			List<ISIPrompt> listPrompts = ISIResponseService.get().getResponseCollectionPrompts(mUser, paramCollectionName);
			for (ISIPrompt prompt : listPrompts) {
				PromptModel mPrompt = new PromptModel(prompt);

				WebMarkupContainer rvPromptList = new WebMarkupContainer(rvPromptResponseList.newChildId());
				rvPromptResponseList.add(rvPromptList);
				
				String crumbTrail = prompt.getContentElement().getContentLocObject().getSection().getCrumbTrailAsString(1, 1);
				rvPromptList.add(new Label("responseHeader", crumbTrail));
					
				// Prompt Icon
				rvPromptList.add(ISIApplication.get().iconFor(
							prompt.getContentElement().getContentLocObject().getSection().getSectionAncestor(),""));
				
				// Add the title and link to the page where this note is located
				BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("contentLink", prompt.getContentElement().getContentLocObject().getSection());
				link.add(new Label("contentLinkTitle", prompt.getContentElement().getContentLocObject().getSection().getTitle()));
				rvPromptList.add(link);
			
				// Text associated with Prompt
				String question =  prompt.getQuestionHTML();			
				rvPromptList.add(new Label("question", question).setEscapeModelStrings(false));
				
				ContentLoc location = prompt.getContentElement().getContentLocObject();

				ResponseList responseList = new ResponseList("responseList", mPrompt, responseMetadata, location, null);
				responseList.setContext("models");
				responseList.setAllowEdit(!isTeacher);
				responseList.setAllowNotebook(!isTeacher);
				rvPromptList.add(responseList);
			}
		}		
	}
			
	@Override
	public String getPageType() {
		return "mymodels";
	}

	@Override
	public String getPageName() {
		return null;
	}
	
	@Override
	public String getPageViewDetail() {
		return (paramCollectionName != null ? paramCollectionName : null);
	}
}