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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.IRelativeLinkSource;
import org.cast.cwm.components.DeployJava;
import org.cast.cwm.components.ShyContainer;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.ResponseMetadata.TypeMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.mediaplayer.AudioPlayerPanel;
import org.cast.cwm.mediaplayer.FlashAppletPanel;
import org.cast.cwm.mediaplayer.MediaPlayerPanel;
import org.cast.cwm.service.ICwmSessionService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.TransformResult;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.component.XmlComponent;
import org.cast.cwm.xml.service.XmlService;
import org.cast.isi.component.AnnotatedImageComponent;
import org.cast.isi.component.AuthoredPopupLink;
import org.cast.isi.component.CollapseBoxHeader;
import org.cast.isi.component.DelayedFeedbackSingleSelectView;
import org.cast.isi.component.FeedbackStatusIndicator;
import org.cast.isi.component.HotSpotComponent;
import org.cast.isi.component.ImmediateFeedbackSingleSelectForm;
import org.cast.isi.component.ImmediateFeedbackSingleSelectView;
import org.cast.isi.component.ScoredDelayedFeedbackSingleSelectForm;
import org.cast.isi.component.ScoredImmediateFeedbackSingleSelectForm;
import org.cast.isi.component.SectionCompleteImageContainer;
import org.cast.isi.component.SingleSelectDelayMessage;
import org.cast.isi.component.SingleSelectItem;
import org.cast.isi.component.SingleSelectMessage;
import org.cast.isi.component.SlideShowComponent;
import org.cast.isi.component.VideoLink;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.PromptType;
import org.cast.isi.page.ISIBasePage;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.SectionLinkFactory;
import org.cast.isi.panel.AgentLink;
import org.cast.isi.panel.GlossaryLink;
import org.cast.isi.panel.ImageDetailButtonPanel;
import org.cast.isi.panel.LockingResponseButtons;
import org.cast.isi.panel.LockingResponseList;
import org.cast.isi.panel.MiniGlossaryLink;
import org.cast.isi.panel.MiniGlossaryModal;
import org.cast.isi.panel.PageLinkPanel;
import org.cast.isi.panel.PeriodResponseList;
import org.cast.isi.panel.RatePanel;
import org.cast.isi.panel.ResponseButtons;
import org.cast.isi.panel.ResponseFeedbackButtonPanel;
import org.cast.isi.panel.ResponseFeedbackPanel;
import org.cast.isi.panel.ResponseList;
import org.cast.isi.panel.ResponseViewActionsPanel;
import org.cast.isi.panel.ScorePanel;
import org.cast.isi.panel.StudentScorePanel;
import org.cast.isi.panel.StudentSectionCompleteToggleImageLink;
import org.cast.isi.panel.TeacherScoreResponseButtonPanel;
import org.cast.isi.panel.ThumbPanel;
import org.cast.isi.service.IISIResponseService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

/**
 * A component to display XML content in ISI.
 *
 */
@Slf4j
public class ISIXmlComponent extends XmlComponent {

	private static final long serialVersionUID = 1L;

	@Inject 
	private IISIResponseService responseService;
	
	@Inject 
	private ICwmSessionService cwmSessionService;

	@Inject
	private IEventService eventService;
	
	@Getter @Setter private String contentPage;
	@Getter @Setter private MiniGlossaryModal miniGlossaryModal;
	@Getter @Setter private ResponseFeedbackPanel responseFeedbackPanel;
	@Getter @Setter protected boolean inGlossary = false;
	
	protected boolean isTeacher = false;

	public ISIXmlComponent(String id, ICacheableModel<? extends IXmlPointer> rootEntry, String transformName) {
		super(id, rootEntry, transformName);
		User user = cwmSessionService.getUser();
		isTeacher = user!=null ? user.getRole().subsumes(Role.TEACHER) : false;
	}

	// Enforce more specific type than super does
	public XmlSectionModel getModel() {
		return (XmlSectionModel) super.getModel();
	}
	
	public XmlSection getXmlSection() {
		XmlSectionModel model = getModel();
		if (model == null)
			return null;
		return model.getObject();
	}
	
	protected ISIXmlSection getISIXmlSection() {
		return (ISIXmlSection) getXmlSection();
	}

	// FIXME - HACK:  For some reason, Unicode D7 (and that alone, as far as I can see) is getting swallowed up and not showing up
	// when called for in XML content.  This fixes it.

    // heikki TODO: test if this strange bug still happens without this hack.
    @Override
    public IMarkupFragment getMarkup() {
        try {
            IMarkupFragment fragment = super.getMarkup();
            return fragment;
            // TODO heikki: disabled hack for now because it causes InputStream Closed exceptions
            // see http://apache-wicket.1842946.n4.nabble.com/Is-it-possible-to-change-MarkupStream-td4659757.html#a4659832
            //return hackUnicodeD7(fragment);
        }
        catch(Exception ex) {
            throw new RuntimeException("ERROR fixing markupstream with unicode D7: " + ex.getMessage());
        }
	}

    /**
     *  If an IMarkupFragment contains unicode d7, converts it into one that uses the corresponding character entity.
     *
     * @param fragment
     * @return
     * @throws ResourceStreamNotFoundException
     */
    protected static IMarkupFragment hackUnicodeD7(IMarkupFragment fragment) throws IOException, ResourceStreamNotFoundException{
        MarkupResourceStream markupStream = fragment.getMarkupResourceStream();
        InputStream is = markupStream.getInputStream();
        String x = getStringFromInputStream(is);
        if (x.contains("\u00d7")) {
            log.debug ("Fixing instances of unicode D7");
            x = x.replaceAll("\u00d7", "&#xd7;");
            return createMarkupFragmentFromString(x);
        }
        else {
            return fragment;
        }
    }

    /**
     * TODO heikki move to utility class
     *
     * Creates an IMarkupFragment from a String.
     *
     * @param s string to create IMarkupFragment from
     * @return
     */
    protected static IMarkupFragment createMarkupFragmentFromString(String s) {
        IResourceStream resourceStream = new StringResourceStream(s);
        MarkupResourceStream markupStream = new MarkupResourceStream(resourceStream);
        IMarkupFragment fragment = new Markup(markupStream);
        return fragment;
    }

    /**
     * Creates a String from an InputStream.
     *
     * Thanks mkyong http://www.mkyong.com/java/how-to-convert-inputstream-to-string-in-java/.
     * TODO heikki move to utility class
     *
     * @param is
     * @return
     */
    protected static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
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
				
				String jarUrl = RequestCycle.get().urlFor(getRelativeRef(archive+".jar"), new PageParameters()).toString();
				String dataUrl = RequestCycle.get().urlFor(getRelativeRef(dataFile), new PageParameters()).toString();

				DeployJava dj = new DeployJava(wicketId);
				dj.setArchive(jarUrl);
				dj.setCode(elt.getAttribute("src"));
				dj.addParameter("dataFile", dataUrl);
				return dj;
			}
			return super.getDynamicComponent(wicketId, elt);
			
			
		// link to a short glossary definition modal or main glossary window
		} else if (wicketId.startsWith("glossaryLink_")) {
			IModel<String> wordModel = new Model<String>(elt.getAttribute("word"));
			String glossaryLinkType = ISIApplication.get().getGlossaryLinkType();
			if (glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_MODAL) && pageHasMiniGlossary() && miniGlossaryModal != null) {
				return new MiniGlossaryLink(wicketId, wordModel, miniGlossaryModal);
				
			} else if (glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE)) {
				return new EmptyPanel(wicketId);
				
			} else {
				// Default case: glossary type is MAIN, or we wanted a modal but have no place to put it
				// (eg, we're in a popup window, or modal window)
				GlossaryLink glossaryLink = new GlossaryLink(wicketId, wordModel);
				ISIApplication.get().setLinkProperties(glossaryLink);
				return glossaryLink;
			}
							
		// inline glossary: linked word
		} else if (wicketId.startsWith("glossword")) {
			WebMarkupContainer glossaryWord = new WebMarkupContainer(wicketId);
			glossaryWord.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE));
			return glossaryWord;
			
		// inline glossary: definition
		} else if (wicketId.startsWith("glossdef")) {
			// Span element, to be filled in with the glossary short def.
			String word = elt.getAttribute("word");
			final String def = ISIApplication.get().getGlossary().getShortDefById(word);
			WebMarkupContainer container;
			container = new WebMarkupContainer(wicketId) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
					replaceComponentTagBody(markupStream, openTag, def);
				}
			};
			container.add(new AttributeRemover("word"));
			container.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE));
			return container;

		// inline glossary: "more" link to main glossary window			
		} else if (wicketId.startsWith("glosslink")) {
			IModel<String> wordModel = new Model<String>(elt.getAttribute("word"));
			GlossaryLink glossaryLink = new GlossaryLink(wicketId, wordModel);
			ISIApplication.get().setLinkProperties(glossaryLink);
			glossaryLink.setVisible(ISIApplication.get().glossaryLinkType.equals(ISIApplication.GLOSSARY_TYPE_INLINE));
			return glossaryLink;
		
		} else if (wicketId.startsWith("link_")) {
			String href = elt.getAttribute("href");
			// According to NIMAS, href should be in the form "filename.xml#ID"  or just "#ID" for within-file link
			// For authors' convenience, we accept simple "ID" as well.
			int hashLocation = href.indexOf('#');
			if (hashLocation > 0) {
				// filename#ID case
				return new SectionLinkFactory().linkTo(wicketId, href.substring(0, hashLocation), href.substring(hashLocation+1));
			}
			// "#ID" or "ID" case:
			String file = getModel().getObject().getXmlDocument().getName(); // same file as we're currently viewing
			String id = href.substring(hashLocation+1);  // start at index 0 or 1
			return new SectionLinkFactory().linkTo(wicketId, file, id);

		} else if (wicketId.startsWith("popupLink_")) {
			String href = elt.getAttribute("href");
			// According to NIMAS, href should be in the form "filename.xml#ID"  or just "#ID" for within-file link
			// For authors' convenience, we accept simple "ID" as well.
			int hashLocation = href.indexOf('#');
			String file;
			if (hashLocation > 0) {
				file = href.substring(0, hashLocation);
			} else { // "#ID" or "ID" case:
				file = getModel().getObject().getXmlDocument().getName(); // same file as we're currently viewing
			}
			XmlDocument document = xmlService.getDocument(file);
			String xmlId = href.substring(hashLocation+1);
			XmlSection section = document.getById(xmlId);
			XmlSectionModel mSection = new XmlSectionModel(section);
			return new AuthoredPopupLink(wicketId, xmlId, mSection);
			
		} else if (wicketId.startsWith("noteBackLink_")) {
			// Link back from a note to its (first) noteref.
			String idref = elt.getAttribute("idref");
			// Find candidate noterefs in this chapter
			XmlSection sec = getModel().getObject();
			XPath xPath = XPathFactory.newInstance().newXPath();
			xPath.setNamespaceContext(XmlService.get().getNamespaceContext());
			XmlSection linkSection = null;
			String linkText = "?";
			try {
				String path = String.format("//dtb:noteref[@idref='#%s']", idref);
				NodeList nl = (NodeList) xPath.evaluate(path, sec.getXmlDocument().getDocument().getDocumentElement(), XPathConstants.NODESET);
				if (nl.getLength() > 0) {
					Element node = null;
					node = (Element) nl.item(0);
					linkText = node.getTextContent();
					// Scan parents until you find the smallest enclosing XML Section.
					while (linkSection == null && node.getParentNode() != null) {
						String id = node.getAttributeNS(null, "id");
						if (id != null) {
							linkSection = sec.getXmlDocument().getById(id);
						}
						node = (Element) node.getParentNode();
					}
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();  // malformed expression - shouldn't happen
			}
			if (linkSection != null) {
				BookmarkablePageLink<ISIStandardPage> link = new SectionLinkFactory().linkTo(wicketId, linkSection, "note_"+idref);
				link.add(new AttributeRemover("idref"));
				link.add(new Label("text", linkText));
				return link;
			} else {
				log.debug("Could not find noteref for note: idref={}", idref);
				return new SectionLinkFactory().linkToPage(wicketId, null);
			}
			
		} else if (wicketId.startsWith("fileLink_")) {
			// link to file in content directory
			return new ResourceLink<Object> (wicketId, getRelativeRef(elt.getAttribute("href")));
			
		} else if (wicketId.startsWith("sectionIcon_")) {		
			WebComponent icon = ISIApplication.get().makeIcon(wicketId, elt.getAttribute("class"));
			icon.add(AttributeModifier.replace("class", new Model<String>("sectionIcon")));
			return icon;
	
		} else if (wicketId.startsWith("thumbRating_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String thumbId = elt.getAttribute("id");
			ThumbPanel thumbPanel = new ThumbPanel(wicketId, loc, thumbId);
			thumbPanel.add(new AttributeRemover("id"));
			return thumbPanel;

		} else if (wicketId.startsWith("thumbRatingDescription_")) {
			Label thumbRatingDescription = new Label(wicketId, new ResourceModel("thumbRatingPanel.ratingDescription", "Rate It:"));
			return thumbRatingDescription;

		} else if (wicketId.startsWith("mediaThumbImage_")) {
			String src = elt.getAttribute("src");
			ResourceReference imgRef = getRelativeRef(src);
			Image image = new Image(wicketId, imgRef) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onComponentTag(final ComponentTag tag)
				{
					super.onComponentTag(tag);
						tag.put("width", elt.getAttribute("width"));
						tag.put("height", elt.getAttribute("height"));
				}
			};			
			return image;
			
		} else if (wicketId.startsWith("mediaThumbLink_")) {			
			String videoId = elt.getAttributeNS(null, "videoId");
			XmlSectionModel currentSectionModel = getModel();			
			VideoLink videoLink = new VideoLink(wicketId, videoId, currentSectionModel);
			videoLink.add(new AttributeRemover("videoId"));
			return videoLink;

		} else if (wicketId.startsWith("videoplayer_")) {
			String videoSrc = elt.getAttribute("src");
			ResourceReference videoRef = getRelativeRef(videoSrc);			
			String videoUrl = getRequestCycle().mapUrlFor(videoRef, null).toString();

            Integer width = Integer.valueOf(elt.getAttribute("width"));
			Integer height = Integer.valueOf(elt.getAttribute("height"));

			String preview = elt.getAttribute("poster");
			String captions = elt.getAttribute("captions");
			String audioDescription = elt.getAttribute("audiodescription");
			
			MediaPlayerPanel comp = new MediaPlayerPanel(wicketId, videoUrl, width, height) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onPlay (String status) {
					if (cwmSessionService.isSignedIn())
						eventService.saveEvent("video:view", "id=" + elt.getAttribute("videoId") + ",state=" + status, contentPage);
				}
			};

			comp.setFullScreen(true);
			comp.setUseOnPlay(true);

			if (!Strings.isEmpty(preview))
				comp.setPreview(getRelativeRef(preview));
			
			if (!Strings.isEmpty(captions))
				comp.setCaptionFile(getRelativeRef(captions));
			
			if (!Strings.isEmpty(audioDescription))
				comp.setAudioDescriptionFile(getRelativeRef(audioDescription));
			
			comp.add(new AttributeRemover("src", "width", "height", "poster", "captions", "audiodescription", "videoId"));
			
			return comp;

		} else if (wicketId.startsWith("audioplayer_")) {
			String audioSrc = elt.getAttribute("src");
			ResourceReference audioRef = getRelativeRef(audioSrc);
			String audioUrl = getRequestCycle().mapUrlFor(audioRef, null).toString();

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

			String preview = elt.getAttribute("poster");
			if (!Strings.isEmpty(preview))
				player.setPreview(getRelativeRef(preview));
			
			return player;

		} else if (wicketId.startsWith("swf_")) {
			
			ResourceReference swfRef = getRelativeRef(elt.getAttribute("src"));
			return new FlashAppletPanel(wicketId, swfRef,
					Integer.valueOf(elt.getAttribute("width")),
					Integer.valueOf(elt.getAttribute("height")),
					"");

		} else if (wicketId.startsWith("feedbackButton_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			IModel<Prompt> pm = responseService.getOrCreatePrompt(PromptType.FEEDBACK, loc, responseGroupId);
			ResponseFeedbackButtonPanel component = new ResponseFeedbackButtonPanel(wicketId, pm, responseFeedbackPanel);
			String forRole = elt.getAttribute("for");
			boolean usesTeacherButton = cwmSessionService.getUser().getRole().subsumes(Role.TEACHER);
			component.setVisibilityAllowed(usesTeacherButton ? forRole.equals("teacher") : forRole.equals("student"));
			component.add(new AttributeRemover("rgid", "for"));
			return component;
		} else if (wicketId.startsWith("scoreButtons_")) {
			IModel<Prompt> promptModel = getPrompt(elt);
			IModel<User> studentModel = ISISession.get().getTargetUserModel();
			ISortableDataProvider<Response> responseProvider = responseService.getResponseProviderForPrompt(promptModel, studentModel);
			TeacherScoreResponseButtonPanel component = new TeacherScoreResponseButtonPanel(wicketId, responseProvider);
			return component;
		} else if (wicketId.startsWith("showScore_")) {
			IModel<Prompt> promptModel = getPrompt(elt);
			IModel<User> studentModel = cwmSessionService.getUserModel();
			ISortableDataProvider<Response> responseProvider = responseService.getResponseProviderForPrompt(promptModel, studentModel);
			ScorePanel component = new StudentScorePanel(wicketId, responseProvider);
			return component;
		// A container for a single-select form and whiteboard, notebook links
		} else if (wicketId.startsWith("responseContainer")) {
			return new WebMarkupContainer(wicketId);
		// A single-select, multiple choice form.  MultipleChoiceItems will be added to a RadioGroup
		// child of this form.  
		} else if (wicketId.startsWith("select1_immediate_")) {
			return makeImmediateResponseForm(wicketId, elt);
		// A single-select, multiple choice form.  MultipleChoiceItems will be added to a RadioGroup
		// child of this form.  
		} else if (wicketId.startsWith("select1_delay_")) {
			return makeDelayedResponseForm(wicketId, elt);
		// buttons for viewing in whiteboard and notebook
		} else if (wicketId.startsWith("viewActions")) {
			IModel<Prompt> mPrompt = getPrompt(elt, PromptType.SINGLE_SELECT);
			Long promptId = mPrompt.getObject().getId();
			ResponseViewActionsPanel component = new ResponseViewActionsPanel(wicketId, promptId);
			component.add(new AttributeRemover("rgid", "title", "group", "type"));
			return component;
			// A single-select, multiple choice disabled form.  MultipleChoiceItems will be added to a RadioGroup
			// child of this form.  
		} else if (wicketId.startsWith("select1_view_immediate")) {
			return makeImmediateResponseView(wicketId, elt);
		// A single-select, multiple choice disabled form.  MultipleChoiceItems will be added to a RadioGroup
		// child of this form.  
		} else if (wicketId.startsWith("select1_view_delay")) {
			return makeDelayedResponseView(wicketId, elt);
		// A multiple choice radio button. Stores a "correct" value. This is
		// added to a generic RadioGroup in a SingleSelectForm.
		} else if (wicketId.startsWith("selectItem_")) {
			Component mcItem = new SingleSelectItem(wicketId,
					new Model<String>(wicketId.substring("selectItem_".length())),
					Boolean.valueOf(elt.getAttribute("correct")));
			mcItem.add(new AttributeRemover("correct"));
			return mcItem;

		// A message associated with a wicketId.startsWith("selectItem_").
		// The wicketId of the associated SingleSelectItem should be provided as a "for" attribute.
		// Visibility based on whether the corresponding radio button is selected in the enclosing form.
		} else if (wicketId.startsWith("selectMessage_")) {
			return new SingleSelectMessage(wicketId, elt.getAttribute("for")).add(new AttributeRemover("for"));

		// A delayed feedback message associated with a wicketId.startsWith("selectItem_").
		// The wicketId of the associated SingleSelectItem should be provided as a "for" attribute.
		// Visibility based on whether the response has been reviewed.
		} else if (wicketId.startsWith("selectDelayMessage_")) {
			ISIXmlSection section = getISIXmlSection();
			IModel<XmlSection> currentSectionModel = new XmlSectionModel(section);
			SingleSelectDelayMessage component = new SingleSelectDelayMessage(wicketId, currentSectionModel);
			return component.add(new AttributeRemover("for"));

		} else if (wicketId.startsWith("responseList_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			ResponseMetadata metadata = getResponseMetadata(responseGroupId);
			IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.RESPONSEAREA, loc, responseGroupId, metadata.getCollection());
			ResponseList dataView = new ResponseList (wicketId, mPrompt, metadata, loc, ISISession.get().getTargetUserModel());
			dataView.setContext(getResponseListContext(false));
			dataView.setAllowEdit(!isTeacher);
			dataView.setAllowNotebook(!inGlossary && !isTeacher && ISIApplication.get().isNotebookOn());
			dataView.setAllowWhiteboard(!inGlossary && ISIApplication.get().isWhiteboardOn());
			dataView.add(new AttributeRemover("rgid", "group"));
			return dataView;

		} else if (wicketId.startsWith("locking_responseList_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			ResponseMetadata metadata = getResponseMetadata(responseGroupId);
			IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.RESPONSEAREA, loc, responseGroupId, metadata.getCollection());
			ResponseList dataView = new LockingResponseList (wicketId, mPrompt, metadata, loc, ISISession.get().getTargetUserModel());
			dataView.setContext(getResponseListContext(false));
			dataView.setAllowEdit(!isTeacher);
			dataView.setAllowNotebook(!inGlossary && !isTeacher && ISIApplication.get().isNotebookOn());
			dataView.setAllowWhiteboard(!inGlossary && ISIApplication.get().isWhiteboardOn());
			dataView.add(new AttributeRemover("rgid", "group"));
			return dataView;

		} else if (wicketId.startsWith("period_responseList_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			ResponseMetadata metadata = getResponseMetadata(responseGroupId);
			IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.RESPONSEAREA, loc, responseGroupId, metadata.getCollection());
			PeriodResponseList dataView = new PeriodResponseList(wicketId, mPrompt, metadata, loc, ISISession.get().getCurrentPeriodModel());
			dataView.setContext(getResponseListContext(true));
			dataView.setAllowEdit(!isTeacher);
			dataView.setAllowNotebook(!inGlossary && !isTeacher && ISIApplication.get().isNotebookOn());
			dataView.setAllowWhiteboard(!inGlossary && ISIApplication.get().isWhiteboardOn());
			dataView.add(new AttributeRemover("rgid", "group"));
			return dataView;			
			
		} else if (wicketId.startsWith("responseButtons_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			Element xmlElement = getModel().getObject().getElement().getOwnerDocument().getElementById(responseGroupId);
			ResponseMetadata metadata = new ResponseMetadata(xmlElement);
			if (!ISIApplication.get().isUseAuthoredResponseType()) {
				// set all the response types to the default per application configuration here
				metadata = adjustResponseTypes(metadata);
			}
			IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.RESPONSEAREA, loc, metadata.getId(), metadata.getCollection());
			ResponseButtons buttons = new ResponseButtons(wicketId, mPrompt, metadata, loc);
			buttons.setVisible(!isTeacher);
			return buttons;

		} else if (wicketId.startsWith("locking_responseButtons_")) {
			ContentLoc loc = new ContentLoc(getModel().getObject());
			String responseGroupId = elt.getAttribute("rgid");
			Element xmlElement = getModel().getObject().getElement().getOwnerDocument().getElementById(responseGroupId);
			ResponseMetadata metadata = new ResponseMetadata(xmlElement);
			if (!ISIApplication.get().isUseAuthoredResponseType()) {
				// set all the response types to the default per application configuration here
				metadata = adjustResponseTypes(metadata);
			}
			IModel<Prompt> mPrompt = responseService.getOrCreatePrompt(PromptType.RESPONSEAREA, loc, metadata.getId(), metadata.getCollection());
			return new LockingResponseButtons(wicketId, mPrompt, metadata, loc, cwmSessionService.getUserModel());

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
			teacherBar.setVisible(!cwmSessionService.getUser().getRole().equals(Role.STUDENT) && !inGlossary);
			return teacherBar;

		} else if (wicketId.startsWith("compareResponses_")) {
			IModel<Prompt> mPrompt = getPrompt(elt);
			BookmarkablePageLink<Page> bpl = new BookmarkablePageLink<Page>(wicketId, ISIApplication.get().getPeriodResponsePageClass());
			bpl.getPageParameters().add("promptId", mPrompt.getObject().getId());
			ISIApplication.get().setLinkProperties(bpl);
			bpl.setVisible(isTeacher);
			bpl.add(new AttributeRemover("rgid", "for", "type"));
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
			return new ImageDetailButtonPanel(wicketId, wicketId.substring("imageDetailButton_".length()));
//  		We may want to put some of this back, but for now assuming that any time XSLT requests an image detail button we'll put one in.
//			if (contentPage == null && !inGlossary) // Don't do imageDetails on non-content pages (e.g. the Table of Contents)
//				return new WebMarkupContainer(wicketId).setVisible(false);

		} else if (wicketId.startsWith("imgToggleHeader")
				|| wicketId.startsWith("imgDetailToggleHeader")
				|| wicketId.startsWith("objectToggleHeader")) {
			// long description header for toggle area

			String src = elt.getAttribute("src");

			// remove everything but the name of the media
			int lastIndex = src.lastIndexOf("/") + 1;
			src = src.substring(lastIndex, src.length());

			Label label;
			String eventType = "ld";
			String detail = null;
			if (wicketId.startsWith("img")) {
				label = new Label(wicketId, new ResourceModel("imageLongDescription.toggleHeading",	"image information"));
				String imageId = elt.getAttribute("imageId");
				detail = "imageId=" + imageId;
				label.add(new AttributeRemover("imageId"));
				if (wicketId.startsWith("imgDetailToggleHeader")) {
					label = new Label(wicketId, new ResourceModel("imageLongDescription.toggleHeading",	"image information"));
					detail = detail + ",context=detail";
				}
			} else { // video or mp3 files
				if (src.contains(".mp3")) {
					label = new Label(wicketId, new ResourceModel("audioLongDescription.toggleHeading",	"more audio information"));
				} else {
					label = new Label(wicketId, new ResourceModel("videoLongDescription.toggleHeading",	"more video information"));
				}
				detail = "src=" + src;
			}

			label.add(new CollapseBoxBehavior("onclick", eventType, ((ISIBasePage) getPage()).getPageName(), detail));
			label.add(new AttributeRemover("src"));
			return label;

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
						
		} else if (wicketId.startsWith("collapseBox_")) {
			WebMarkupContainer collapseBox = new WebMarkupContainer(wicketId);
			return collapseBox;

		} else if (wicketId.startsWith("feedbackStatusIndicator_")) {
			FeedbackStatusIndicator feedbackStatusIndicator = new FeedbackStatusIndicator(wicketId);
			return feedbackStatusIndicator;

			
		} else if (wicketId.startsWith("collapseBoxControl-")) {
			String boxSequence = (wicketId.substring("collapseBoxControl-".length()).equals("") ? "0" : wicketId.substring("collapseBoxControl-".length()));
			CollapseBoxHeader collapseBoxHeader = new CollapseBoxHeader(wicketId, boxSequence);
			return collapseBoxHeader;

		} else if (wicketId.startsWith("iScienceLink-")) {			
			return new AjaxFallbackLink<Object>(wicketId) {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					target.prependJavaScript("$('#iScienceVideo-" + wicketId.substring("iScienceLink-".length()) + "').jqmShow();");
					eventService.saveEvent("iscience:view", "Video #" + wicketId.substring("iScienceLink-".length()), ((ISIBasePage) getPage()).getPageName());
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
			return new StudentSectionCompleteToggleImageLink(wicketId, currentSectionModel);
			
		} else if (wicketId.startsWith("inactiveSectionStatusIcon_")) {
			String id = elt.getAttribute("id");
			IModel<XmlSection> currentSectionModel = new XmlSectionModel(getModel().getObject().getXmlDocument().getById(id));
			return new SectionCompleteImageContainer(wicketId, currentSectionModel);
		} else if (wicketId.startsWith("itemSummary_")) {
			boolean noAnswer = Boolean.parseBoolean(elt.getAttributeNS(null, "noAnswer"));
			Component singleSelectComponent = new SingleSelectSummaryXmlComponentHandler().makeComponent(wicketId, elt, getModel(), noAnswer);
			singleSelectComponent.add(new AttributeRemover("noAnswer"));
			return singleSelectComponent;
			
		} else if (wicketId.startsWith("shy")) {
			return new ShyContainer(wicketId);
		} else {
			return super.getDynamicComponent(wicketId, elt);
		}
	}
	
	private boolean pageHasMiniGlossary() {
		ISIBasePage page = (ISIBasePage) getPage();
		return page.hasMiniGlossary();
	}
	
	private String getResponseListContext(boolean isDiscussion) {
		StringBuilder builder = new StringBuilder();
		if (inGlossary)
			builder.append("glossary");
		else
			builder.append("response");
		if (isDiscussion)
			builder.append(".discuss");
		if (isTeacher)
			builder.append(".teacher");
		return builder.toString();
	}
	
	private ScoredDelayedFeedbackSingleSelectForm makeDelayedResponseForm(String wicketId, Element elt) {
		ISIXmlSection section = getISIXmlSection();
		IModel<XmlSection> currentSectionModel = new XmlSectionModel(section);
		boolean compact = Boolean.parseBoolean(elt.getAttributeNS(null, "compact"));
		ScoredDelayedFeedbackSingleSelectForm selectForm = new ScoredDelayedFeedbackSingleSelectForm(wicketId, getPrompt(elt, PromptType.SINGLE_SELECT), currentSectionModel);
		selectForm.add(new AttributeRemover("rgid", "title", "group", "type", "noAnswer", "compact"));
		selectForm.setShowDateTime(!compact);
		return selectForm;
	}

	private Component makeImmediateResponseForm(String wicketId, Element elt) {
		Component selectForm = new ScoredImmediateFeedbackSingleSelectForm(wicketId, getPrompt(elt, PromptType.SINGLE_SELECT));
		selectForm.add(new AttributeRemover("rgid", "title", "group", "type"));
		return selectForm;
	}
		
	private Component makeDelayedResponseView(String wicketId, Element elt) {
		ISIXmlSection section = getISIXmlSection();
		IModel<XmlSection> currentSectionModel = new XmlSectionModel(section);
		DelayedFeedbackSingleSelectView component = new DelayedFeedbackSingleSelectView(wicketId, getPrompt(elt, PromptType.SINGLE_SELECT), currentSectionModel);
		component.add(new AttributeRemover("rgid", "title", "group", "type"));
		component.setShowDateTime(false);
		return component;
	}

	private Component makeImmediateResponseView(String wicketId, Element elt) {
		ImmediateFeedbackSingleSelectForm selectForm = new ImmediateFeedbackSingleSelectView(wicketId, getPrompt(elt, PromptType.SINGLE_SELECT));
		selectForm.add(new AttributeRemover("rgid", "title", "group", "type"));
		return selectForm;
	}
	
	protected IModel<Prompt> getPrompt(Element elt) {
		String type = elt.getAttribute("type");
		if (type.equals("select1"))
			return getPrompt(elt, PromptType.SINGLE_SELECT);
		if (type.equals("responsearea"))
			return getPrompt(elt, PromptType.RESPONSEAREA);
		throw new IllegalArgumentException("Unknown prompt type " + type + " requested");
	}
	
	protected IModel<Prompt> getPrompt(Element elt, PromptType type) {
		ContentLoc loc = new ContentLoc(getModel().getObject());
		String responseGroupId = elt.getAttribute("rgid");
		String collectionName = elt.getAttribute("group").trim();
		return responseService.getOrCreatePrompt(type, loc, responseGroupId, collectionName);
	}
	
	protected ResponseMetadata getResponseMetadata (String responseGroupId) {
		Element xmlElement = getModel().getObject().getElement().getOwnerDocument().getElementById(responseGroupId);
		ResponseMetadata metadata = new ResponseMetadata(xmlElement);
		return metadata;
	}

	/** Rewrite the set of response types in the given metadata to match the set configured for the application.
	 * keeping any actual configuration (sentence starters, etc).
	 * @param metadata the metadata object parsed from the XML
	 * @return metadata object after modification
	 */	
	protected ResponseMetadata adjustResponseTypes (ResponseMetadata metadata) {
		Map<String, TypeMetadata> typesFromXml = metadata.getTypeMap();
		HashMap<String, TypeMetadata> newMap = new HashMap<String,TypeMetadata>(6);
		// Build the new map with all configured types
		for (IResponseType responseType : ISIApplication.get().defaultResponseTypes) {
			String typeName = responseType.getName();
			if (typesFromXml.containsKey(typeName))
				// keep existing type def. when we have one
				newMap.put(typeName, typesFromXml.get(typeName)); 
			else
				// Otherwise add with default configuration
				newMap.put(typeName, new TypeMetadata());
		}
		// Any additional types that may have been configured in the XML are simply ignored.
		metadata.setTypeMap(newMap);
		return metadata;
	}
	
	public ResourceReference getRelativeRef (String src) {
        IInputStreamProvider xmlFile = getModel().getObject().getXmlDocument().getXmlFile();
        if (xmlFile instanceof IRelativeLinkSource) {
            return ((IRelativeLinkSource)xmlFile).getRelativeReference(src);
        }
        throw new IllegalStateException("Can't find reference relative to file " + xmlFile);
	}
	
	public static class AttributeRemover extends Behavior {
		
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