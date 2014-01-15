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
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Role;
import org.cast.cwm.glossary.IGlossaryEntry;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.WordCard;
import org.cast.isi.panel.GlossaryPanel;
import org.cast.isi.service.IWordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
public class GlossaryPage extends ISIBasePage implements IHeaderContributor{

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GlossaryPage.class);
	protected Map<String, IGlossaryEntry> listUserEntries;
	protected IModel<List<WordCard>> wordCardList;
	
	@Inject
	protected IWordService wordService;

	/**
	 * The glossary page is used to wrap the glossary panel.  It adds the header/footer
	 * and also the form to add new user words (wordcard)
	 * 
	 * @param params
	 */
	public GlossaryPage(PageParameters params) {
		super(params);

		// Look up word
		IModel<? extends IGlossaryEntry> mEntry = null;
		if (params.getNamedKeys().contains("word")) {
			mEntry = ISIApplication.get().getGlossary().getEntryById(params.get("word").toString());
			if (mEntry == null) {
				// Try WordCards
				Long id = params.get("word").toLong();
				mEntry = wordService.getWordCard(id);
			}
		}
		
		// add page title
		String pageTitleEnd = (new StringResourceModel("Glossary.pageTitle", this, null, "Glossary").getString());
		setPageTitle(pageTitleEnd);
		boolean isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		add(new Label("heading", isTeacher ?  (pageTitleEnd + " - " + ISISession.get().getCurrentPeriodModel().getObject().getName() + " > "
				+ ISISession.get().getTargetUserModel().getObject().getFullName()) : pageTitleEnd));
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		
		add(ISIApplication.get().getToolbar("tht", this));
		add(new GlossaryPanel("glossaryPanel", mEntry));
		add(new NewWordForm("newWordForm"));		
	}
	
	
	/**
	 * Add the form to create user defined words in this glossary
	 */
	protected class NewWordForm extends Form<Object> {
		private static final long serialVersionUID = 1L;
		IModel<String> mWord = new Model<String>("");
		private FeedbackPanel feedback;

		public NewWordForm(String id) {
			super(id);
			add(new TextArea<String>("wordText", mWord)
					.setRequired(true)
					.add(new AttributeModifier("maxlength", "50")));
			add(new AjaxButton("submit") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					String newWord = WordCard.normalizeWord(mWord.getObject());
					PageParameters param = new PageParameters();
					
					if (ISIApplication.get().getGlossary().getEntryByForm(newWord) == null) { // TODO should this get by headword only?
						IModel<WordCard> wc = wordService.getWordCardCreate(newWord, ISISession.get().getUser(), false);
						param.add("wc", wc.getObject().getId().toString());
					} else {
						// word is already in the glossary
						param.add("word", newWord);
					}
					this.setResponsePage(ISIApplication.get().getGlossaryPageClass(), param);
				}
			});                  
			add(feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(NewWordForm.this)));
			feedback.setOutputMarkupPlaceholderTag(true);
		}
	}
	
	public void renderHead(IHeaderResponse response) {
		renderThemeCSS(response, "css/glossary.css");
		renderThemeCSS(response, "css/window_print.css", "print");
		super.renderHead(response);
		response.renderOnLoadJavaScript("bindSectionOpenerLinks()");
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public String getPageType() {
		String linkFrom = getPageParameters().get("link").toString();
		if (linkFrom != null)
			return "glossary:" + linkFrom;
		return "glossary";
	}

	@Override
	public String getPageViewDetail() {
		return getPageParameters().get("word").toString();
	}

}