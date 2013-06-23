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

import com.google.inject.Inject;
import lombok.Getter;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.ResponseViewerFactory;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIPrompt;
import org.cast.isi.data.ISIResponse;
import org.cast.isi.data.PromptType;
import org.cast.isi.panel.RemoveDialog;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.panel.StudentScorePanel;
import org.cast.isi.service.IISIResponseService;
import org.hibernate.LockOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A base Notebook view.  Notebook entries viewable by Chapter.  Entries can be directly inside
 * the notebook or copied from the reading pages.
 * 
 * @author jbrookover
 *
 */
@AuthorizeInstantiation("STUDENT")
public class Notebook extends ISIBasePage implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Notebook.class);

	boolean isTeacher = false;
	
	// A map of responses in the notebook grouped by prompt
	protected Map<ISIPrompt, List<ISIResponse>> responseMap;

	protected ContentLoc currentLoc, currentChapterLoc;
	protected IModel<List<XmlSection>> mChapterList;

	@Getter protected DropDownChoice<XmlSection> chapterChoice;

	protected ResponseMetadata notebookMetadata;

	@Inject
	IISIResponseService responseService;
	
	private static final ResponseViewerFactory factory = new ResponseViewerFactory();

	public Notebook(PageParameters parameters) {
		super(parameters);

		// set the heading for this page - modify the properties file to change this
		String pageTitleEnd = (new StringResourceModel("Notebook.pageTitle", this, null, "Notebook").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));

		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		add(new Label("heading", isTeacher ?  (pageTitleEnd + " - " + ISISession.get().getCurrentPeriodModel().getObject().getName() + " > "
				+ ISISession.get().getTargetUserModel().getObject().getFullName()) : pageTitleEnd));
		add(ISIApplication.get().getToolbar("tht", this));

		setNotebookMetadata(notebookMetadata);

		getChapterList();
		getInitChapter(parameters);
		addChapterChoice();
		addNotebookResponses();

		populateResponseMap();
		addResponses();
	}

	
	/**
	 * Create a loadable detachable model of the Chapters (level 1).
	 */
	protected void getChapterList() {
		mChapterList = new LoadableDetachableModel<List<XmlSection>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<XmlSection> load() {
				List<XmlSection> chapterList = new ArrayList<XmlSection>(); 
				for (XmlDocument doc : ISIApplication.get().getStudentContent()) { // For each XML document
					chapterList.addAll(doc.getTocSection().getChildren()); // get all chapters (level1)
				}
				return chapterList;
			}			
		};
	}

	/**
	 * Determine which chapter you are currently in based on the location parameter
	 * or the bookmark set for this user.
	 */
	protected void getInitChapter(PageParameters parameters) {
		// check if there is a location passed in otherwise default to the current bookmarked location
		if (parameters.getNamedKeys().contains("loc")) {
			currentLoc = new ContentLoc(parameters.get("loc").toString());
		} else {
			currentLoc = ISIApplication.get().getBookmarkLoc();
		}

		// Determine which chapter is the current chapter 
		for (XmlSection chapter : mChapterList.getObject()) {
			if (chapter.isAncestorOf(currentLoc.getSection())) {
				currentChapterLoc = new ContentLoc(chapter);
			} else if (chapter.equals(currentLoc.getSection())) {
				currentChapterLoc = currentLoc;
			}   				
		}

		// if for some reason you can't find the chapter, then load the first chapter
		if (currentChapterLoc == null) {
			currentChapterLoc = new ContentLoc(mChapterList.getObject().get(0));
		}
	}

	/**
	 * Add a drop down choice of chapters.
	 */
	protected void addChapterChoice() {
		
		// display only the titles in the dropdown
		ChoiceRenderer<XmlSection> renderer = new ChoiceRenderer<XmlSection>("title");
		chapterChoice = new DropDownChoice<XmlSection>("chapterChoice", new XmlSectionModel(currentChapterLoc.getSection()), mChapterList, renderer);
		chapterChoice.add(new SimpleAttributeModifier("autocomplete", "off"));
		chapterChoice.add(new SimpleAttributeModifier("ignore", "true"));

		Form<XmlSection> chapterSelectForm = new Form<XmlSection>("chapterSelectForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				super.onSubmit();
				// only reload the page if the user has selected a new chapter
				ContentLoc newLoc = new ContentLoc((ISIXmlSection)chapterChoice.getModelObject());
				if (!newLoc.equals(currentChapterLoc)) {
					currentLoc = newLoc;
					PageParameters param = new PageParameters();
					param.add("loc", currentLoc.getLocation());
					setResponsePage(ISIApplication.get().getNotebookPageClass(), param);
				}
			}			
		};
		add(chapterSelectForm);		
		chapterSelectForm.add(chapterChoice);							
	}


	/**
	 * Add a response list to the notebook.  These are notes NOT associated with reading pages.
	 * These responses are associated with the Chapter (level1).
	 */
	protected void addNotebookResponses () {
		IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.NOTEBOOK_NOTES, currentChapterLoc);
		ResponseList responseList = new ResponseList("nbResponseList", mPrompt, notebookMetadata, currentChapterLoc, ISISession.get().getTargetUserModel());
		String context = "notebook" + (isTeacher ? ".teacher" : ""  );
		responseList.setContext(context);
		responseList.setAllowEdit(!isTeacher);
		responseList.setAllowNotebook(false);
		responseList.setAllowWhiteboard(false);
		add(responseList);
		ResponseButtons responseButtons = new ResponseButtons("responseButtons", mPrompt, notebookMetadata, currentLoc);
		responseButtons.setContext("notebook");
		responseButtons.setVisible(!isTeacher);
		add(responseButtons);
	}


	/**
	 * Fill the response map with responses from the database.  This creates a TreeHashMap (sorted
	 * by the Prompt's curriculum order).
	 */
	protected void populateResponseMap() {

		// get all the notebook responses for this target user, sorted by notebook insert time
		IModel<List<ISIResponse>> responseList = responseService.getAllNotebookResponsesByStudent(ISISession.get().getTargetUserModel());	
		responseMap = new TreeMap<ISIPrompt, List<ISIResponse>>();

		// Loop through the responses.  Group them by prompt, starting with the response added
		// to the notebook most recently.
		ISIXmlSection currentChapterSection = currentChapterLoc.getSection();
		for (ISIResponse r : responseList.getObject()) {

			// check if the response is a child of the current chapter, if so add it to the response map
			ISIPrompt prompt = (ISIPrompt) r.getPrompt();
			ISIXmlSection promptSection = new ContentLoc(prompt.getContentElement().getContentLocation()).getSection();
			
			if (promptSection != null) {
				if (currentChapterSection.isAncestorOf(promptSection)) {
					if (responseMap.containsKey(r.getPrompt()))
						responseMap.get(r.getPrompt()).add(r);
					else
						responseMap.put((ISIPrompt) r.getPrompt(), new ArrayList<ISIResponse>(Arrays.asList(r)));
				}
			} else {
				log.error("Notebook contained content item that no longer exists: {}", prompt);
			}
		}		
	}

	/**
	 * Add a repeater with the set of Prompts/Responses
	 */
	protected void addResponses() {

		// "No Responses" message
		WebMarkupContainer noNotes = new WebMarkupContainer("noNotes");
		add(noNotes.setVisible(responseMap.isEmpty()));

		RepeatingView noteRepeater = new RepeatingView("noteRepeater");
		add(noteRepeater.setVisible(!responseMap.isEmpty()));

		for (Entry<ISIPrompt, List<ISIResponse>> entry : responseMap.entrySet()) {
			ISIPrompt currentPrompt = entry.getKey();

			WebMarkupContainer promptGroup = new WebMarkupContainer(noteRepeater.newChildId());
			noteRepeater.add(promptGroup);

			String crumbTrail = currentPrompt.getContentElement().getContentLocObject().getSection().getCrumbTrailAsString(1, 1);						
			promptGroup.add(new Label("responseHeader", crumbTrail));

			// Prompt Icon
			promptGroup.add(ISIApplication.get().iconFor(
					currentPrompt.getContentElement().getContentLocObject().getSection().getSectionAncestor()));

			// Add the title and link to the page where this note is located
			BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkToPage("titleLink", currentPrompt.getContentElement().getContentLocObject().getSection());
			link.add(new Label("sectionTitle", currentPrompt.getContentElement().getContentLocObject().getSection().getTitle()));
			link.add(new ClassAttributeModifier("sectionLink"));
			promptGroup.add(link);

			// Show the score
			promptGroup.add(new StudentScorePanel("responseScore", getModels(entry.getValue())));

			// Text associated with Prompt
			promptGroup.add(factory.makeQuestionTextComponent("question", currentPrompt));

			// The list of responses under this prompt
			promptGroup.add(makeResponseListView(entry));
		}
	}

	private List<IModel<Response>> getModels(List<ISIResponse> responses) {
		List<IModel<Response>> result = new ArrayList<IModel<Response>>();
		for (ISIResponse response: responses) {
			result.add(new HibernateObjectModel<Response>(response));
		}
		return result;
	}


	private ListView<ISIResponse> makeResponseListView(
			Entry<ISIPrompt, List<ISIResponse>> entry) {
		final ISIPrompt currentPrompt = entry.getKey();
		return new ListView<ISIResponse>("responseList", entry.getValue()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ISIResponse> item) {

				// Anchor so links can jump to this id
				item.add(new WebMarkupContainer("responseAnchor")
				.add(new SimpleAttributeModifier("name", String.valueOf(item.getModelObject().getId()))));

				// Actual response
				item.add(factory.makeResponseViewComponent("response", item.getModel()));

				// Remove From Notebook button
				NotebookRemoveDialog removeDialog = new NotebookRemoveDialog("removeModal", item.getModel());
				item.add(removeDialog);
				Component removeLink = new WebMarkupContainer("removeLink").add(removeDialog.getClickToOpenBehavior());
				removeLink.setVisible(!isTeacher);
				item.add(removeLink);

				// Link back to content
				BookmarkablePageLink<ISIStandardPage> editLink = new SectionLinkFactory().linkTo(
						"editLink",
						currentPrompt.getContentElement().getContentLocObject().getSection(),
						currentPrompt.getContentElement().getXmlId());
				editLink.add(new ClassAttributeModifier("sectionLink"));
				item.add(editLink);
			}

		};
	}

	public void setNotebookMetadata(ResponseMetadata notebookMetadata) {
		this.notebookMetadata = ISIApplication.get().getResponseMetadata();
	}

	@Override
	public String getPageName() {
		return "notebook";
	}

	@Override
	public String getPageType() {
		return "notebook";
	}

	@Override
	public String getPageViewDetail() {
		return null;
	}

	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		renderThemeCSS(response, "css/window.css");
		renderThemeCSS(response, "css/window_print.css", "print");
		response.renderOnLoadJavaScript("bindSectionOpenerLinks()");
	}


	@Override
	protected void onDetach() {
		if (mChapterList != null)
			mChapterList.detach();
		super.onDetach();
	}

	protected class NotebookRemoveDialog extends RemoveDialog {
		private static final long serialVersionUID = 1L;

		public NotebookRemoveDialog(String id, IModel<?> model) {
			super(id, model);
		}

		protected void removeObject() {
			ISIResponse resp = (ISIResponse) getDefaultModelObject();
			// Lock to current session - necessary since objects are stored detached in responseMap
			Databinder.getHibernateSession().buildLockRequest(LockOptions.UPGRADE).lock(resp);
			responseService.removeFromNotebook(resp, getPage());
			setResponsePage(ISIApplication.get().getNotebookPageClass(), getPageParameters());
		}

		@Override
		protected String getDialogTitle() {
			return new StringResourceModel("Notebook.removeDialogTitle", this, null, "Remove from Notebook?").getString();
		}

		@Override
		protected String getDialogText() {
			return new StringResourceModel("Notebook.removeDialogText", this, null, "Are you sure you want to remove this response from the Notebook?").getString();
		}
	}	
}