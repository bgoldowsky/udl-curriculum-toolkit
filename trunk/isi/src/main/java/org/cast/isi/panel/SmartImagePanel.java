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

import java.util.Random;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.components.ClassAttributeModifier;
import org.cast.cwm.indira.IndiraImage;
import org.cast.cwm.indira.IndiraImageComponent;
import org.cast.cwm.indira.IndiraImage.STATE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SmartImagePanel extends ISIPanel implements IHeaderContributor {

	private static final long serialVersionUID = 1L;
	private final static Logger log = LoggerFactory.getLogger(SmartImagePanel.class);
	
	private int numImages; // How many images are there?
	private int currentImage = 0; // Image currently being displayed: 0-based index
	private String[] imageNames; // A list of base image names for prev/next functionality
	private String[] captions; // Caption for each image
	private boolean showLabels = false; // Should we be displaying the "_l" version of this image?
	private final IModel<IndiraImage> indiraImageModel;
	private IndiraImageComponent imageComponent;
	private AjaxFallbackLink<Object> prevLink, nextLink;
	
	public SmartImagePanel(String id, Element elt) {
		super(id);
		setOutputMarkupId(true);
		
		// Set Smart Image Title
		add (new Label("title", elt.getAttribute("title")));
		
		// List of image nodes
		NodeList images = elt.getElementsByTagName("img");
		numImages = images.getLength();
		
		// Image Names
		imageNames = new String[images.getLength()];
		for (int i=0; i<numImages; i++)
			imageNames[i] = ((Element) images.item(i)).getAttribute("src");
		
		// Captions
		captions = new String[numImages];
		NodeList captionNL = elt.getElementsByTagName("caption");
		for (int i=0; i<captionNL.getLength(); i++) {
			Element cap = (Element) captionNL.item(i);
			String imgid = cap.getAttribute("imgref");
			if (!Strings.isEmpty(imgid)) {
				// find the image with matching ID from the list of images we have.
				boolean found = false;
				for (int j=0; j<images.getLength(); j++) {
					if (imgid.equals(((Element) images.item(j)).getAttribute("id"))) {
						captions[j] = cap.getTextContent();
						found = true;
						break;
					}
				}
				if (!found)
					log.debug("Caption in smart image does not refer to any image; imgref={}", imgid);
			} else {
				log.debug("Caption in smart image has no imgref: {}", cap.getTextContent());
			}
		}
		
		// Model for the IndiraImage for the currentImageName
		indiraImageModel = new AbstractReadOnlyModel<IndiraImage>() {

			private static final long serialVersionUID = 1L;

			@Override
			public IndiraImage getObject() {
				return IndiraImage.get(imageNames[currentImage], STATE.LABEL);
			}
			
		};
				
		// Add Dynamic Image Component
		imageComponent = new IndiraImageComponent("imageTarget", indiraImageModel, new AbstractReadOnlyModel<STATE>() {

			private static final long serialVersionUID = 1L;

			@Override
			public STATE getObject() {
				
				if (showLabels && IndiraImage.get(imageNames[currentImage]).hasLabel())
					return STATE.LABEL;
				else 
					return null;
			}
		});
		add(imageComponent);
		imageComponent.setOutputMarkupId(true);

		// Add Direct Title Links
		RepeatingView titleLinks = new RepeatingView("titleNav");
		add(titleLinks);

		for (int i = 0; i < images.getLength(); i++) {
			WebMarkupContainer titleLinkContainer = new WebMarkupContainer(titleLinks.newChildId());
			titleLinks.add(titleLinkContainer);
			titleLinkContainer.setRenderBodyOnly(true);
			
			AjaxFallbackLink<Object> titleLink = new ImageLink("link", i);
			titleLinkContainer.add(titleLink);
			titleLink.add(new Label("title", images.item(i).getAttributes().getNamedItem("title").getTextContent()).setRenderBodyOnly(true));			
		}
		
		// Add Previous Link
		prevLink = new AjaxFallbackLink<Object>("previous") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (isEnabled()) {
					currentImage--;
					if (target != null) {
						target.addComponent(SmartImagePanel.this);
						target.addComponent(nextLink);
					}
				}
			}
			
			@Override
			public boolean isEnabled() {
				return currentImage > 0;
			}

			@Override
			protected void onBeforeRender()
			{
				if (isEnabled()) {
					add(new ClassAttributeModifier("off", true));
				} else {
					add(new ClassAttributeModifier("off"));
				}
				super.onBeforeRender();
			}
		};
		add(prevLink);
		prevLink.setOutputMarkupId(true);
		
		// Add Next Link
		nextLink = new AjaxFallbackLink<Object>("next") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {				
				if (isEnabled()) {
					currentImage++;
					if (target != null) {
						target.addComponent(SmartImagePanel.this);
						target.addComponent(prevLink);
					}
				}	
			}
			
			@Override
			public boolean isEnabled() {
				return (currentImage < numImages-1);
			}

			@Override
			protected void onBeforeRender() {
				if (isEnabled()) {
					add(new ClassAttributeModifier("off", true));
				} else {
					add(new ClassAttributeModifier("off"));
				}
				super.onBeforeRender();
			}
		};
		add(nextLink);
		nextLink.setOutputMarkupId(true);
		
		// Add Caption for this Smart Image
		Label caption = new Label("caption", new AbstractReadOnlyModel<String>() {
				private static final long serialVersionUID = 1L;
	
				@Override
				public String getObject() {
					return captions[currentImage];
				}
			}) 
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isVisible() {
				return (captions[currentImage] != null);
			}
		};
		add(caption);
		
		// Add Label show|hide controls
		add(new OverlayContainer("overlay"));
	}
	

	@Override
	protected void onComponentTag(ComponentTag tag) {
		// Don't include title attribute of div in output
		tag.remove("title");
		super.onComponentTag(tag);
	}
	
	// Preload image sources
	public void renderHead(IHeaderResponse response) {
		String script = "";
		Random r = new Random();
		for (int i = 0; i < numImages; i++) {
			String currentImage = imageNames[i];
			IndiraImage ii = IndiraImage.get(currentImage);
			String uniqueName = SmartImagePanel.this.getId().replace("-", "_") + r.nextInt(Integer.MAX_VALUE) + "_" + i; 
			script += uniqueName + " = new Image();\n";
			script += uniqueName + ".src = '" + ii.getImagePath() + "';\n";
			script += uniqueName + "_l = new Image();\n";
			script += uniqueName + "_l.src = '" + ii.getImagePath(STATE.LABEL) + "';\n";
		}
		response.renderJavascript(script, "smartImagePreload");
	}
	
	
	// A panel that controls the showing and hiding of "_l" versions of the current image
	protected class OverlayContainer extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		public OverlayContainer(String id) {
			super(id);
			AjaxFallbackLink<Object> show = new AjaxFallbackLink<Object>("show") {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					showLabels = true;
					if (target != null) {
						target.addComponent(SmartImagePanel.this);
					}
				}
				
				@Override
				public boolean isEnabled() {
					return !showLabels;
				}
				
				@Override
				public void onBeforeRender() {
					if (!isEnabled()) {
						add(new ClassAttributeModifier("current", false));
					} else {
						add(new ClassAttributeModifier("current", true));
					}
					super.onBeforeRender();
				}
			};
			add(show);
			
			AjaxFallbackLink<Object> hide = new AjaxFallbackLink<Object>("hide") {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					showLabels = false;
					if (target != null) {
						target.addComponent(SmartImagePanel.this);
					}
				}
				
				@Override
				public boolean isEnabled() {
					return showLabels;
				}
				
				@Override
				public void onBeforeRender() {
					if (!isEnabled()) {
						add(new ClassAttributeModifier("current", false));
					} else {
						add(new ClassAttributeModifier("current", true));
					}
					super.onBeforeRender();
				}
			};
			add(hide);
		}
		
		@Override
		public boolean isVisible() {
			return IndiraImage.get(imageNames[currentImage]).hasLabel();
		}
	}
	
	
	protected class ImageLink extends AjaxFallbackLink<Object> {

			private static final long serialVersionUID = 1L;
			private int linkTo;
			
			public ImageLink (String wicketId, int linkTo) {
				super(wicketId);
				this.linkTo = linkTo;
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				currentImage = linkTo;
				if (target != null)
					target.addComponent(SmartImagePanel.this);
			}
			
			@Override
			public boolean isEnabled() {
				return currentImage != linkTo;
			}

			@Override
			public void onBeforeRender() {
				add(new ClassAttributeModifier("current", (currentImage!=linkTo)));
				super.onBeforeRender();
			}

			@Override
			public IAjaxCallDecorator getAjaxCallDecorator() {
				return new IAjaxCallDecorator() {

					private static final long serialVersionUID = 1L;

					public CharSequence decorateOnFailureScript(CharSequence script) {
						return script;
					}

					public CharSequence decorateOnSuccessScript(CharSequence script) {
						return script;
					}
					// TODO: Get Fade Working... 
					// Create a Temp Image, fade out, swap, fade in
					public CharSequence decorateScript(CharSequence script) {
						return "fadeOutIn('" + imageComponent.getMarkupId() + "');" + script;
					}
				};
			}
	}		
}