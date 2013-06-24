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
package org.cast.isi.panel;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.cast.audioapplet.component.AbstractAudioRecorder;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.ResponseMetadata.TypeMetadata;
import org.cast.cwm.service.IEventService;
import org.cast.isi.ISIApplication;
import org.cast.isi.data.ContentLoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wicket.contrib.tinymce.settings.TinyMCESettings;

import java.util.ArrayList;
import java.util.List;

public class ResponseEditor extends org.cast.cwm.data.component.ResponseEditor {

	@Getter
	protected ResponseMetadata metadata;
	
	@Getter @Setter
	protected String context = "default";

	protected ContentLoc loc;
	
	@Getter @Setter
	private boolean titleVisible = false;

	protected static TinyMCESettings tinyMCESettings = null;
	
	protected FeedbackPanel feedback;

	@Inject
	private IEventService eventService;

	private final static Logger log = LoggerFactory.getLogger(ResponseEditor.class);	
	private static final long serialVersionUID = 1L;

	public ResponseEditor (String wicketId, IModel<Response> model, ResponseMetadata metadata, ContentLoc loc) {
		super(wicketId, model);
		this.metadata = metadata;
		this.loc = loc;
	}

	public ResponseEditor (String wicketId, IModel<Prompt> prompt, IResponseType type, ResponseMetadata metadata, ContentLoc loc) {
		super (wicketId, prompt, type);
		this.metadata = metadata;
		this.loc = loc;
	}
	
	/**
	 * Since our superclass doesn't (yet) use ResponseMetadata,
	 * when initializing this component, extract all the relevant metadata from the ResponseMetadata object
	 * and push it into the class's specific fields.
	 */
	@Override
	protected void onInitialize() {

        IInputStreamProvider xmlFile = null;
		if (loc != null) {
			this.setPageName(loc.getLocation());
			xmlFile = loc.getSection().getXmlDocument().getXmlFile();  
		}
		IResponseType thisType = getModelObject().getType();
		String typeName = type.getName();
		if (typeName.equals("AUDIO") || typeName.equals("SVG") || typeName.equals("UPLOAD"))
			titleVisible = true;
		if (metadata != null) {
			TypeMetadata typeMD = metadata.getType(thisType);
			if (typeMD != null) {
				if (thisType.getName().equals("SVG") && typeMD.getFragments() != null) {
					// Drawing starters - need to convert to URLs.
				    List<String> urls = new ArrayList<String>(typeMD.getFragments().size());
					for (String frag : typeMD.getFragments()) {
						ResourceReference fragResourceRef = ((IRelativeLinkSource)xmlFile).getRelativeReference(frag);
						String url = RequestCycle.get().urlFor(fragResourceRef, new PageParameters()).toString();
						if (url != null) {
							urls.add(url);
                        }
						else {
							log.warn("Drawing stamp image does not exist: {}", frag);
                        }
					}
					setStarters(urls);
				} else {
					// Sentence starters
					this.setStarters(typeMD.getFragments());
				}
				// Template
				if (typeMD.getTemplates() != null && !typeMD.getTemplates().isEmpty()) {
					String templateRelativePath = typeMD.getTemplates().get(0);  // path from xml file
					ResourceReference templateResourceRef = ((IRelativeLinkSource)xmlFile).getRelativeReference(templateRelativePath);
					this.setTemplateURL(RequestCycle.get().urlFor(templateResourceRef, new PageParameters()).toString());
				}
			}
		}
		super.onInitialize();
	}

	@Override
	protected WebMarkupContainer getEditorFragment(String id, final IModel<Response> model, IResponseType type) {
		final WebMarkupContainer editor = super.getEditorFragment(id, model, type);
		
		if (type.getName().equals("AUDIO")) {
			// Audio fragment does not by default include a form so title can be saved.
			Form<Response> form = new Form<Response>("form", model);
			editor.add(form);
			// Replace standard audio "save" link with one that also saves the form.
			AjaxSubmitLink saveLink = new AjaxSubmitLink("save", form) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					// This gets invoked when user clicks "save" button.
					// First, save the title to datastore (creating Response if necessary).
					responseService.saveResponseWithoutData(model);
					// Then get the audio data streamed back to the server
					target.appendJavaScript(((AbstractAudioRecorder)editor.get("applet")).generateJavascriptMessage("SAVE"));
				}
				
			};
			saveLink.setOutputMarkupId(true);
			editor.replace(saveLink);			
		}
		
		((Form<?>)editor.get("form")).add(new TitleFragment("titleFragment", model));
		
		if (type.getName().equals("AUDIO")) {
			String propertyString;
			TypeMetadata typeMD = metadata.getType("AUDIO");
			// Special case: use text sentence starters for audio if there are no audio-specific ones.
			if ((typeMD==null || typeMD.getFragments()==null || typeMD.getFragments().isEmpty())) {
				 propertyString = new String("typeMap[HTML].fragments");
			} else {
				 propertyString = new String("typeMap[AUDIO].fragments");
			}
			editor.add(new ListView<String>("starters", new PropertyModel<List<String>>(metadata, propertyString)) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<String> item) {
					item.add(new Label("text", item.getModelObject()));
				}

				@Override
				public boolean isVisible() {
					return ! (getModelObject() == null || getModelObject().isEmpty());
				}
				
			});
		}
		
		return editor;
	}

	protected class TitleFragment extends Fragment {

		private static final long serialVersionUID = 1L;

		public TitleFragment(String id, IModel<Response> model) {
			super(id, "titleFragment", ResponseEditor.this, model);
			setRenderBodyOnly(true);
			// resource keys are, for example, "response.title.prompt.audio"
			add(new Label("titleInstructions", new ResourceModel("response.title.prompt."+model.getObject().getType().getName().toLowerCase(), "Add a title")));
			add(new TextField<String>("title", new PropertyModel<String>(model, "title")).add(new SimpleAttributeModifier("maxlength", "250")));
		}
		
		@Override
		public boolean isVisible() {
			return titleVisible;
		}
	}

	@Override
	protected void onSave(final AjaxRequestTarget target) {
	}

	protected void onCancel(final AjaxRequestTarget target) {
	}

	@Override
	protected void onDelete(final AjaxRequestTarget target) {
	}

	@Override
	protected void onStarterAdded(AjaxRequestTarget target) {
		eventService.saveEvent("starterAdded", "promptId=" + prompt.getObject().getId().toString(), getPageName());
		super.onStarterAdded(target);
	}

	@Override
	public TinyMCESettings getTinyMCESettings() {
		tinyMCESettings = ISIApplication.get().getTinyMCESettings(context);
		return tinyMCESettings;
	}
	
}
