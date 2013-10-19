package org.cast.isi.panel;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.PromptModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.ResponseViewerFactory;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.SectionLinkFactory;
import org.cast.isi.service.IISIResponseService;

import com.google.inject.Inject;

public class ResponseCollectionListing extends GenericPanel<User> {

	@Inject
	protected IISIResponseService responseService;

	private static final ResponseViewerFactory factory = new ResponseViewerFactory();

	private String collectionName;
	
	@Getter
	private boolean showingContent = false;
	
	private static final long serialVersionUID = 1L;

	public ResponseCollectionListing (String wicketId, IModel<User> mUser, String collectionName) {
		super(wicketId, mUser);
		this.collectionName = collectionName;
		add(makePromptResponseRepeater("promptResponseRepeater"));
		setOutputMarkupPlaceholderTag(true);
	}
	
	protected RepeatingView makePromptResponseRepeater(String id) {
		RepeatingView rvPromptResponseList = new RepeatingView(id);
		for (ISIPrompt prompt : responseService.getResponseCollectionPrompts(getModel(), collectionName)) {
			rvPromptResponseList.add(makePromptContainer(rvPromptResponseList.newChildId(), prompt));
			showingContent = true;
		}
		return rvPromptResponseList;
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
		return responseService.getAllResponsesForPromptByStudent(new PromptModel(prompt), getModel());
	}

}
