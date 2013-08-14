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

import java.util.ArrayList;
import java.util.List;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.UserModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.ResponseViewerFactory;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.data.ScoreCounts;
import org.cast.isi.panel.ResponseCollectionSummary;
import org.cast.isi.panel.StudentScorePanel;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * ResponseCollections are groups of response areas that are collected under a response name.
 * These groupings are pre-determined in the xml.  
 * 
 * @author lynnmccormack
 *
 */
@AuthorizeInstantiation("STUDENT")
public class ResponseCollections extends ISIStandardPage {
	
	private static final long serialVersionUID = 1L;

	protected String paramCollectionName = null; // The name of the ResponseCollection currently being displayed
	protected String pageTitleEnd;
	protected UserModel mUser;
	protected boolean isTeacher = false;
	
	@Inject
	protected IISIResponseService responseService;
	
	@Inject
	protected IFeatureService featureService;
	
	protected static final Logger log = LoggerFactory.getLogger(ResponseCollections.class);
	protected static ResponseMetadata responseMetadata = new ResponseMetadata();
	static {
		responseMetadata.addType("HTML");
		responseMetadata.addType("AUDIO");
		responseMetadata.addType("SVG");
		responseMetadata.addType("UPLOAD");
	}

	private static final ResponseViewerFactory factory = new ResponseViewerFactory();

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
		
		paramCollectionName = parameters.get("name").toString();
		
		if (!haveSelectedCollection()) {
			add(new WebMarkupContainer("collectionTitle").setVisible(false));
		} else {
			add(new Label("collectionTitle", paramCollectionName));
		}
						
		List<String> listNames = getCollectionNames(mUser);
		
		// components on the left side
		add(makeCollectionNameRepeater(listNames));

		// components on the right side of the form		
		WebMarkupContainer wmcNoModels = new WebMarkupContainer("noModels");
		add(wmcNoModels);
		WebMarkupContainer wmcNoModelSelected = new WebMarkupContainer("noModelSelected");
		add(wmcNoModelSelected);
		add(new WebMarkupContainer("noStudentSelected").setVisible(mUser.getObject() == null));   	

		if (mUser.getObject() != null) {
			wmcNoModelSelected.setVisible(!haveSelectedCollection() && haveCollections(listNames));
			wmcNoModels.setVisible(!haveSelectedCollection() && !haveCollections(listNames));
		} else {
			wmcNoModelSelected.setVisible(false);
			wmcNoModels.setVisible(false);
		}
		
		if (haveSelectedCollection()) {
			add(makeSummary("promptResponseSummary"));
			add(makePromptResponseRepeater("promptResponseRepeater"));
		}
		else {
			add(new EmptyPanel("promptResponseSummary"));
			add(new RepeatingView("promptResponseRepeater"));
		}
		
	}

	@Override
	public void reloadForPeriodStudentChange(final PageParameters parameters) {
		PageParameters newParams = new PageParameters(parameters);
		IModel<User> targetUserModel = ISISession.get().getTargetUserModel();
		List<String> collectionNames = getCollectionNames(targetUserModel);
		if (!(collectionNames.contains(paramCollectionName)))
			newParams.remove("name");
		super.reloadForPeriodStudentChange(newParams);
	}			

	protected Component makeSummary(String id) {
		if (featureService.isCollectionsScoreSummaryOn())
			return new ResponseCollectionSummary(id, getScoreCounts());
		else return new EmptyPanel(id);
	}

	protected ScoreCounts getScoreCounts() {
		IModel<List<ISIResponse>> responses =  responseService.getAllResponsesForCollectionByStudent(paramCollectionName, mUser);
		return ScoreCounts.forResponses("questions", responses);
	}

	protected boolean haveCollections(List<String> listNames) {
		return (listNames != null) && !(listNames.isEmpty());
	}

	protected boolean haveSelectedCollection() {
		return StringUtils.isNotEmpty(paramCollectionName);
	}

	protected RepeatingView makePromptResponseRepeater(String id) {
		RepeatingView rvPromptResponseList = new RepeatingView(id);
		for (ISIPrompt prompt : responseService.getResponseCollectionPrompts(mUser, paramCollectionName)) {
			rvPromptResponseList.add(makePromptContainer(rvPromptResponseList.newChildId(), prompt));
		}
		return rvPromptResponseList;
	}

	protected RepeatingView makeCollectionNameRepeater(List<String> listNames) {
		RepeatingView rvCollectionList = new RepeatingView("collectionList");
		
		for (String collectionName : listNames) {
			WebMarkupContainer wmc = new WebMarkupContainer(rvCollectionList.newChildId());
			wmc.add(makeCollectionLink(collectionName));
			rvCollectionList.add(wmc);
		}
		return rvCollectionList;
	}

	protected WebMarkupContainer makePromptContainer(String newChildId, ISIPrompt prompt) {
		WebMarkupContainer rvPromptList = new WebMarkupContainer(newChildId);
		
		ISIXmlSection section = getSection(prompt);
		rvPromptList.add(new Label("responseHeader", section.getCrumbTrailAsString(1, 1)));
			
		// Prompt Icon
		rvPromptList.add(ISIApplication.get().iconFor(section.getSectionAncestor()));
		
		// Add the title and link to the page where this note is located
		BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("contentLink", section);
		link.add(new Label("contentLinkTitle", section.getTitle()));
		rvPromptList.add(link);
		List<ISIResponse> responses = getResponsesFor(prompt);

		// Show the score
		rvPromptList.add(new StudentScorePanel("responseScore", getModels(responses)));
		
		// Text associated with Prompt
		rvPromptList.add(factory.makeQuestionTextComponent("question", prompt));
		
		rvPromptList.add(makeResponseListView(prompt, responses));
		return rvPromptList;
	}

	protected List<IModel<Response>> getModels(List<ISIResponse> responses) {
		List<IModel<Response>> result = new ArrayList<IModel<Response>>();
		for (ISIResponse response: responses) {
			result.add(new HibernateObjectModel<Response>(response));
		}
		return result;
	}

	protected ISIXmlSection getSection(ISIPrompt prompt) {
		return prompt.getContentElement().getContentLocObject().getSection();
	}

	protected BookmarkablePageLink<Page> makeCollectionLink(String collectionName) {
		BookmarkablePageLink<Page> bpl = new BookmarkablePageLink<Page>("link", ISIApplication.get().getResponseCollectionsPageClass());
		bpl.getPageParameters().add("name", collectionName);
		bpl.add(new Label("name", collectionName));
		
		// if the param collection name is the same as this one set the indicator that this is the item clicked
		if (haveSelectedCollection()) {
			if (paramCollectionName.equals(collectionName)) {
				bpl.add(new AttributeModifier("class", "selected"));
				bpl.setEnabled(false);
			}
		}
		return bpl;
	}

	protected List<String> getCollectionNames(IModel<User> userModel) {
		if (userModel.getObject() != null) {
			return responseService.getResponseCollectionNames(userModel);
		}
		return new ArrayList<String>();
	}

	protected Component makeResponseListView(final ISIPrompt prompt, List<ISIResponse> responses) {
		return new ListView<ISIResponse>("responseList", responses) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ISIResponse> item) {
				item.add(factory.makeResponseViewComponent("response", item.getModel()));
				// Link back to content
				BookmarkablePageLink<ISIStandardPage> editLink = new SectionLinkFactory().linkTo(
						"editLink",
						prompt.getContentElement().getContentLocObject().getSection(),
						prompt.getContentElement().getXmlId());
				editLink.add(new ClassAttributeModifier("sectionLink"));
				item.add(editLink);
			}

		};
	}

	protected List<ISIResponse> getResponsesFor(ISIPrompt prompt) {
		return responseService.getAllResponsesForPromptByStudent(new PromptModel(prompt), mUser);
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
		return paramCollectionName;
	}
}