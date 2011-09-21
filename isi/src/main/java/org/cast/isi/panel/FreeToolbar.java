/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.panel;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import net.jeremybrooks.knicker.KnickerException;
import net.jeremybrooks.knicker.WordApi;
import net.jeremybrooks.knicker.Knicker.SourceDictionary;
import net.jeremybrooks.knicker.dto.Definition;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: document
 * TODO: clean up how decoration is added.
 * 
 * @author borisgoldowsky
 *
 */
public class FreeToolbar extends Panel {
	
	protected HiddenField<String> selectedText;
	protected SidebarDialog dictionaryModal;
	protected Label definition;
	protected IModel<String> mDefinition = new Model<String>("");
	
	protected int MAX_DICT_DEFS = 3; // how many definitions are shown in the dictionary sidebar.
	
	/**
	 * Which dictionaries to look in for definitions.
	 */
	LinkedHashSet<SourceDictionary> wordnikDictionaries = new LinkedHashSet<SourceDictionary>(Arrays.asList(SourceDictionary.ahd, SourceDictionary.webster, SourceDictionary.wiktionary, SourceDictionary.wordnet));

	private static final long serialVersionUID = 1L;

	//@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(FreeToolbar.class);

	public FreeToolbar(String id) {
		super(id);
		
		dictionaryModal = new SidebarDialog("dictionaryModal", "Dictionary", "thtDictionary");
		add (dictionaryModal);
		
		// I'm a little puzzled as to why this extra WMC is necessary, but without it the definition can 
		// only be ajax-updated once, after which it loses its markupId.
		WebMarkupContainer body = new WebMarkupContainer("body");
		dictionaryModal.getBodyContainer().add(body);
		definition = new Label("definition", mDefinition);
		definition.setOutputMarkupId(true);
		definition.setEscapeModelStrings(false);
		body.add(definition);
		
		Form<Void> toolbarForm = new Form<Void>("toolbarForm");
		add(toolbarForm);
		
		toolbarForm.add(selectedText = new HiddenField<String>("selectedText", new Model<String>("")));

		AjaxSubmitLink dictionary = new AjaxSubmitLink("dictionary") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String word = selectedText.getModelObject();
				mDefinition.setObject(getDefinition(word));
				target.addComponent(definition);
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator() {
				return new AjaxCallDecorator() {
					private static final long serialVersionUID = 1L;

					// Several jobs for the Javascript:
					// 1. Get the text selection and put it into a form field for transmission to server
					// 2. Remove old definition that may be in modal window, replace with "Please wait"
					// 3. Open modal
					// 4. Do normal script, which means AJAX-submitting form to server.
					// FIXME: Please wait should be a string property
					// FIXME: does not properly get text selection from inside IFrame (e.g. TinyMCE)
					@Override
					public CharSequence decorateScript(final CharSequence script) { 
						return "$('#selectedText').val(getSelectedText());" 
							+ "$('#" + dictionaryModal.getMarkupId() + " .definitionContainer').text('Please wait...');"
							+ dictionaryModal.getOpenString()
							+ script
							;
					}
				};
			}
			
		};
		dictionary.setMarkupId("thtDictionary"); // id expected by CSS
		toolbarForm.add (dictionary);
	}
	
	protected String getDefinition (String word) {
		if (Strings.isEmpty(word))
			return "No word selected";
		try {
			List<Definition> definitions = WordApi.definitions(word.toLowerCase(), MAX_DICT_DEFS, null, false, wordnikDictionaries, true, false);
			if (definitions.isEmpty())
				return "<strong>" + word + ":</strong> not found";
			StringBuilder deftext = new StringBuilder();
			for (Definition def : definitions) {
				deftext.append("<p>");
				deftext.append("<strong>" + def.getWord() + "</strong> ");
				deftext.append("<em>" + def.getPartOfSpeech() + ":</em> ");
				deftext.append(def.getText());
				deftext.append(" [" + def.getSourceDictionary() + "]");
				deftext.append("</p>\n");
			}
			// FIXME Somehow clean or convert to UTF-8 ?  Defs sometimes have odd chars: try looking up "wonderful".
			log.debug("Received definition from Wordnik: {}", deftext);
			return  deftext.toString();
		} catch (KnickerException e) {
			e.printStackTrace();
			return "<p>Error while looking up word</p>";
		}
	}

}
