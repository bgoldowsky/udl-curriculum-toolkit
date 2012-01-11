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
package org.cast.isi;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.components.DeployJava;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.ResponseType;
import org.cast.cwm.data.Role;
import org.cast.cwm.indira.FileResource;
import org.cast.cwm.indira.FileResourceManager;
import org.cast.cwm.mediaplayer.AudioPlayerPanel;
import org.cast.cwm.mediaplayer.FlashAppletPanel;
import org.cast.cwm.mediaplayer.MediaPlayerPanel;
import org.cast.cwm.service.EventService;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.TransformResult;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.component.XmlComponent;
import org.cast.isi.component.AnnotatedImageComponent;
import org.cast.isi.component.HotSpotComponent;
import org.cast.isi.component.SlideShowComponent;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.PromptType;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.panel.AgentLink;
import org.cast.isi.panel.GlossaryLink;
import org.cast.isi.panel.ImageDetailButtonPanel;
import org.cast.isi.panel.MiniGlossaryLink;
import org.cast.isi.panel.MiniGlossaryModal;
import org.cast.isi.panel.PageLinkPanel;
import org.cast.isi.panel.RatePanel;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseFeedbackButtonPanel;
import org.cast.isi.panel.ResponseFeedbackPanel;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.panel.SectionCompleteToggleComponent;
import org.cast.isi.panel.ThumbPanel;
import org.cast.isi.service.ISIResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A component to display XML content in ISI.
 *
 */
public class ISIXmlComponent extends XmlComponent {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ISIXmlComponent.class);
	
	@Getter @Setter private String contentPage;
	@Getter @Setter private MiniGlossaryModal miniGlossaryModal;
	@Getter @Setter private ResponseFeedbackPanel responseFeedbackPanel;
	@Getter @Setter protected boolean inGlossary = false;
	protected boolean isTeacher = false;

	public ISIXmlComponent(String id, ICacheableModel<? extends IXmlPointer> rootEntry, String transformName) {
		super(id, rootEntry, transformName);
		isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
	}

	// Enforce more specific type than super does
	public XmlSectionModel getModel() {
		return (XmlSectionModel) super.getModel();
	}
	
	// FIXME - HACK:  For some reason, Unicode D7 (and that alone, as far as I can see) is getting swallowed up and not showing up
	// when called for in XML content.  This fixes it.
	protected String getMarkup() {
		String x =  super.getMarkup();
		if (x.contains("\u00d7")) {
			log.debug ("Fixing instances of unicode D7");
			x = x.replaceAll("\u00d7", "&#xd7;");
		}
		return x;
	}

	@Override
	public Component getDynamicComponent(final String wicketId, final Element elt) {
		
		if (wicketId.startsWith("object_")) {
			NodeList nodes = elt.getChildNodes();
			String archive = null;
			String dataFile = null;
			for(int i=0; i<nodes.getLength(); i++) {
				Node nextNode = nodes.item(i);
				if(nextNode instanceof Element) {
					Element next = (Element)nodes.item(i);
					if(next.getAttribute("name").equals("archive")) {
						archive = next.getAttribute("value");
					} else if(next.getAttribute("name").equals("dataFile")) {
						dataFile = next.getAttribute("value");
					} 
				}
			}
			if(archive != null) {
				DeployJava dj = new DeployJava(wicketId);
				dj.setArchive(FileResourceManager.get().getUrl(archive+".jar"));
				dj.setCode(elt.getAttribute("src"));
				dj.addParameter("dataFile", FileResourceManager.get().getUrl(dataFile));
				return dj;
			}
			return super.getDynamicComponent(wicketId, elt);
			
			
		// glossaryLink is associated with a short glossary definition modal
		} else if (wicketId.startsWith("glossaryLink_")) {
			MiniGlossaryLink miniGlossaryLink = new MiniGlossaryLink(wicketId, new Model<String>(elt.getAttribute("word")), miniGlossaryModal);
			miniGlossaryLink.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_MODAL));
			return miniGlossaryLink;

		// glossdef and the the glosslink that follows are associated with inline glossary definitions
		} else if (wicketId.startsWith("glossword")) {
			WebMarkupContainer glossaryWord = new WebMarkupContainer(wicketId);
			glossaryWord.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE));
			return glossaryWord;
			
		} else if (wicketId.startsWith("glossdef")) {
			// Span element, to be filled in with the glossary short def.
			String word = elt.getAttribute("word");
			final String def = ISIApplication.get().getGlossary().getShortDefById(word);
			WebMarkupContainer container;
			container = new WebMarkupContainer(wicketId) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
					replaceComponentTagBody(markupStream, openTag, def);
				}
			};
			container.add(new AttributeRemover("word"));
			container.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE));
			return container;
					
		} else if (wicketId.startsWith("glosslink")) {
			IModel<String> wordModel = new Model<String>(elt.getAttribute("word"));
			GlossaryLink glossaryLink = new GlossaryLink(wicketId, wordModel);
			ISIApplication.get().setLinkProperties(glossaryLink);
			glossaryLink.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE));
			return glossaryLink;
		
		// glossaryMainLinks are linked directly to the glossary popup page
		} else if (wicketId.startsWith("glossaryMainLink_")) {
			IModel<String> wordModel = new Model<String>(elt.getAttribute("word"));
			GlossaryLink glossaryLink = new GlossaryLink(wicketId, wordModel);
			ISIApplication.get().setLinkProperties(glossaryLink);
			glossaryLink.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_MAIN));
			return glossaryLink;
						
		} else if (wicketId.startsWith("link_")) {
			String href = elt.getAttribute("href");
			// According to NIMAS, href should be in the form "filename.xml#ID"  or just "#ID" for within-file link
			// For authors' convenience, we accept simple "ID" as well.
			int hashLocation = href.indexOf('#');
			if (hashLocation > 0) {
				// filename#ID case
				return ISIStandardPage.linkTo(wicketId, href.substring(0, hashLocation), href.substring(hashLocation+1));
			}
			// "#ID" or "ID" case:
			String file = getModel().getObject().getXmlDocument().getName(); // same file as we're currently viewing
			String id = href.substring(hashLocation+1);  // start at index 0 or 1
			log.debug("Link to {} # {}", file, id);
			return ISIStandardPage.linkTo(wicketId, file, id);
			
		} else if (wicketId.startsWith("fileLink_")) {
			// link to file in content directory
			return new ResourceLink<Object> (wicketId, getRelativeRef(elt.getAttribute("href")));
			
		} else if (wicketId.startsWith("sectionIcon_")) {		
			WebComponent icon = ISIApplication.get().makeIcon(wicketId, elt.getAttribute("class"));
			icon.add(new AttributeRemover("class"));
			return icon;
	
		} else if (wicketId.startsWith("thumbRating_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String thumbId = elt.getAttribute("id");
			ThumbPanel thumbPanel = new ThumbPanel(wicketId, loc, thumbId);
			thumbPanel.add(new AttributeRemover("id"));
			return thumbPanel;

		} else if (wicketId.startsWith("videoplayer_")) {
			String videoSrc = elt.getAttribute("src");
			ResourceReference videoRef = getRelativeRef(videoSrc);
			String videoUrl = RequestCycle.get().urlFor(videoRef).toString();

			String previewName = videoSrc.replace(".flv", "_p.png");
			ResourceReference previewRef = getRelativeRef(previewName);
			String preview = RequestCycle.get().urlFor(previewRef).toString();

			String captionsName = videoSrc.replace(".flv", "_cap.xml");
			ResourceReference captionsRef = getRelativeRef(captionsName);
			String captions = RequestCycle.get().urlFor(captionsRef).toString();
			MediaPlayerPanel comp = new MediaPlayerPanel(wicketId, videoUrl, 
					Integer.valueOf(elt.getAttribute("width")), 
					Integer.valueOf(elt.getAttribute("height"))) {

						private static final long serialVersionUID = 1L;
						
						@Override
						public void onPlay (String status) {
							EventService.get().saveEvent("video:view", wicketId.substring("videoplayer_".length()) + " - " + status, contentPage);
						}
			};
			
			comp.setFullScreen(true);
			comp.setUseOnPlay(true);
			if (preview != null)
				comp.setPreview(previewRef);
			if (captions != null)
				comp.setCaptionFile(captionsRef);
			return comp;

		} else if (wicketId.startsWith("audioplayer_")) {
			String audioSrc = elt.getAttribute("src");
			ResourceReference audioRef = getRelativeRef(audioSrc);
			String audioUrl = RequestCycle.get().urlFor(audioRef).toString();
			int width = 400;
			if (!elt.getAttribute("width").equals("")) {
				try {
					width = Integer.parseInt(elt.getAttribute("width").trim());
				} catch (Exception e) {
					log.debug("Can't get width for {}: {}", audioUrl, e);
					width = 400;
				}
			}
			AudioPlayerPanel player = new AudioPlayerPanel(wicketId, audioUrl, width, 20);
			player.setShowDownloadLink(false);
			player.setRenderBodyOnly(true);
			return player;

		} else if (wicketId.startsWith("swf_")) {
			return new FlashAppletPanel(wicketId,
					new ResourceReference(FileResource.class, elt.getAttribute("src"), null, null),
					Integer.valueOf(elt.getAttribute("width")),
					Integer.valueOf(elt.getAttribute("height")),
					"");

		} else if (wicketId.startsWith("feedbackButton_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			IModel<Prompt> pm = ISIResponseService.get().getOrCreatePrompt(PromptType.FEEDBACK, loc, responseGroupId);
			ResponseFeedbackButtonPanel component = new ResponseFeedbackButtonPanel(wicketId, pm, responseFeedbackPanel);
			String forRole = elt.getAttribute("for");
			boolean usesTeacherButton = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
			component.setVisibilityAllowed(usesTeacherButton ? forRole.equals("teacher") : forRole.equals("student"));
			component.add(new AttributeRemover("rgid", "for"));
			return component;
			
		} else if (wicketId.startsWith("responseList_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			ResponseMetadata metadata = getResponseMetadata(responseGroupId);
			IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.RESPONSEAREA, loc, responseGroupId, metadata.getCollection());
			ResponseList dataView = new ResponseList (wicketId, mPrompt, metadata, loc, null);
			dataView.setContext("response");
			dataView.setAllowEdit(!isTeacher);
			dataView.setAllowNotebook(!inGlossary && !isTeacher && ISIApplication.get().isNotebookOn());
			dataView.setAllowWhiteboard(!inGlossary && ISIApplication.get().isWhiteboardOn());
			dataView.add(new AttributeRemover("rgid"));
			return dataView;

		} else if (wicketId.startsWith("responseButtons_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			Element xmlElement = getModel().getObject().getElement().getOwnerDocument().getElementById(responseGroupId);
			ResponseMetadata metadata = new ResponseMetadata(xmlElement);
			if (!ISIApplication.get().isUseAuthoredResponseType()) {
				// set all the response types to the default per application configuration here
				metadata = addMetadata(metadata);
			}
			IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.RESPONSEAREA, loc, metadata.getId(), metadata.getCollection());
			ResponseButtons buttons = new ResponseButtons(wicketId, mPrompt, metadata, loc);
			buttons.setVisible(!isTeacher);
			return buttons;

		} else if (wicketId.startsWith("ratePanel_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String promptText = null;
			String ratingId = elt.getAttribute("id");
			NodeList nodes = elt.getChildNodes();
			// extract the prompt text authored - we might want to consider re-writing this
			// to use xsl instead of this - but this works for now - ldm
			for (int i=0; i<nodes.getLength(); i++) {
				Node nextNode = nodes.item(i);
				if (nextNode instanceof Element) {
					Element next = (Element)nodes.item(i);
					if (next.getAttribute("class").equals("prompt")) {
						//get all the html under the prompt element
						promptText = new TransformResult(next).getString();
					} 
				}
			}
			RatePanel ratePanel = new RatePanel(wicketId, loc, ratingId, promptText);
			ratePanel.add(new AttributeRemover("id"));
			ratePanel.add(new AttributeRemover("type"));
			return ratePanel;

		} else if (wicketId.startsWith("teacherBar_")) {
			WebMarkupContainer teacherBar = new WebMarkupContainer(wicketId);
			teacherBar.setVisible(!ISISession.get().getUser().getRole().equals(Role.STUDENT) && !inGlossary);
			return teacherBar;

		} else if (wicketId.startsWith("compareResponses_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			ResponseMetadata metadata = getResponseMetadata(responseGroupId);
			IModel<Prompt> mPrompt = ISIResponseService.get().getOrCreatePrompt(PromptType.RESPONSEAREA, loc, metadata.getId(), metadata.getCollection());
			// create a link to the periodResponse page passing in the prompt id
			BookmarkablePageLink<Page> bpl = new BookmarkablePageLink<Page>(wicketId, ISIApplication.get().getPeriodResponsePageClass());
			bpl.setParameter("promptId", mPrompt.getObject().getId());
			ISIApplication.get().setLinkProperties(bpl);
			bpl.setVisible(isTeacher);
			bpl.add(new AttributeRemover("rgid", "for"));
			return bpl;

		} else if(wicketId.startsWith("agent_")) {
			String title = elt.getAttribute("title");
			if (Strings.isEmpty(title))
				title = new StringResourceModel("isi.defaultAgentButtonText", this, null, "Help").getObject();
			AgentLink link = new AgentLink(wicketId, title, elt.getAttribute("responseAreaId"));
			link.add(new AttributeRemover("title", "responseAreaId"));
			return link;
			
		} else if (wicketId.startsWith("image_")) {
			String src = elt.getAttribute("src");
			ResourceReference imgRef = getRelativeRef(src);
			return new Image(wicketId, imgRef);

		} else if (wicketId.startsWith("imageThumb_")) {			
			String src = elt.getAttribute("src");
			int ext = src.lastIndexOf('.');
			src = src.substring(0, ext) + "_t" + src.substring(ext);
			ResourceReference imgRef = getRelativeRef(src);
			Image img = new Image(wicketId, imgRef);
			// FIXME these attributes were removed because indira was adding height and width of the detail image
			// not the thumbnail image - remove when indira gets removed
			img.add(new AttributeRemover("width", "height"));
			return img;
			
		} else if (wicketId.startsWith("imageDetailButton_")) {
			// for thumbnail images only - no longer for more info
			return new ImageDetailButtonPanel(wicketId, wicketId.substring("imageDetailButton_".length()), true);
//  		We may want to put some of this back, but for now assuming that any time XSLT requests an image detail button we'll put one in.
//			if (contentPage == null && !inGlossary) // Don't do imageDetails on non-content pages (e.g. the Table of Contents)
//				return new WebMarkupContainer(wicketId).setVisible(false);

		} else if (wicketId.startsWith("imgToggleHeader_")) {
			// long description header for toggle area
			return new Label(wicketId, new ResourceModel("longDescription.toggleHeading", "More Info"));
			
		} else if (wicketId.startsWith("annotatedImage_")) {
			// image with hotspots
			AnnotatedImageComponent annotatedImageComponent = new AnnotatedImageComponent(wicketId, elt, getModel());
			annotatedImageComponent.add(new AttributeRemover("annotatedImageId"));
			return annotatedImageComponent;
			
		} else if (wicketId.startsWith("hotSpot_")) {
			// clickable areas on annotated images
			HotSpotComponent hotSpotComponent = new HotSpotComponent(wicketId, elt);
			hotSpotComponent.add(new AttributeRemover("annotatedImageId"));
			return hotSpotComponent;
			
		} else if (wicketId.startsWith("slideShow_")) {
			SlideShowComponent slideShowComponent = new SlideShowComponent(wicketId, elt);
			return slideShowComponent;
						
		} else if (wicketId.startsWith("collapseBoxControl-")) {
			String boxSequence = (wicketId.substring("collapseBoxControl-".length()).equals("") ? "0" : wicketId.substring("collapseBoxControl-".length()));
			WebMarkupContainer collapseBoxContainer = new WebMarkupContainer(wicketId);
			collapseBoxContainer.add(new CollapseBoxBehavior("onclick", "support:" + boxSequence, ((ISIStandardPage) getPage()).getPageName()));
			return collapseBoxContainer;

		} else if (wicketId.startsWith("iScienceLink-")) {			
			return new AjaxFallbackLink<Object>(wicketId) {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					target.prependJavascript("$('#iScienceVideo-" + wicketId.substring("iScienceLink-".length()) + "').jqmShow();");
					EventService.get().saveEvent("iscience:view", "Video #" + wicketId.substring("iScienceLink-".length()), ((ISIStandardPage) getPage()).getPageName());
				}
			};

		} else if (wicketId.startsWith("youtube_")) {
			int width = getWidth(elt, 640);
			int height = getHeight(elt, 385);
			String src = elt.getAttribute("src");
			src = src.replace("youtube.com/watch?v=", "youtube.com/v/");

			FlashAppletPanel panel = new FlashAppletPanel(wicketId, width, height);
			panel.add(new AttributeRemover("src", "width", "height"));
			panel.setAppletHRef(src);
			panel.setFullScreen(true);
			return panel;	
			
		} else if (wicketId.startsWith("pageLinkPanel_")) {
			String id = elt.getAttribute("id");
			IModel<XmlSection> currentSectionModel = new XmlSectionModel(getModel().getObject().getXmlDocument().getById(id));
			PageLinkPanel panel = new PageLinkPanel(wicketId, currentSectionModel, null);
			panel.add(new AttributeRemover("id"));
			return panel;

		} else if (wicketId.startsWith("sectionStatusIcon_")) {
			String id = elt.getAttribute("id");
			IModel<XmlSection> currentSectionModel = new XmlSectionModel(getModel().getObject().getXmlDocument().getById(id));
			SectionCompleteToggleComponent sectionStatusIcon = new SectionCompleteToggleComponent(wicketId, currentSectionModel); 
			return sectionStatusIcon;
							
		} else {
			return super.getDynamicComponent(wicketId, elt);
		}
	}
	
	protected ResponseMetadata getResponseMetadata (String responseGroupId) {
		Element xmlElement = getModel().getObject().getElement().getOwnerDocument().getElementById(responseGroupId);
		ResponseMetadata metadata = new ResponseMetadata(xmlElement);
		return metadata;
	}

	
	protected ResponseMetadata addMetadata (ResponseMetadata metadata) {
		// Set the response types to be allowed for this application configuration
		for (ResponseType responseType : ISIApplication.get().defaultResponseTypes) {
			metadata.addType(responseType);
		}
		return metadata;
	}

	
	public ResourceReference getRelativeRef (String src) {
		Resource xmlFile = ((XmlSection)getModel().getObject()).getXmlDocument().getXmlFile();
		if (xmlFile instanceof IRelativeLinkSource)
			return ((IRelativeLinkSource)xmlFile).getRelativeReference(src);
		throw new IllegalStateException("Can't find reference relative to file " + xmlFile);
	}

	
	public static class AttributeRemover extends AbstractBehavior {
		
		private String[] atts;

		private static final long serialVersionUID = 1L;
	
		public AttributeRemover (String... atts) {
			this.atts = atts;
		}
		
		@Override
		public void onComponentTag(Component component,	ComponentTag tag) {
			for (String at : atts)
				tag.getAttributes().remove(at);
		}
	}
}