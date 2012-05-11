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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.databinder.auth.hib.AuthDataSession;
import net.databinder.hib.DataRequestCycle;
import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;
import net.jeremybrooks.knicker.AccountApi;
import net.jeremybrooks.knicker.KnickerException;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.CwmSession;
import org.cast.cwm.components.CwmPopupSettings;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.component.DialogBorder;
import org.cast.cwm.data.component.SessionExpireWarningDialog;
import org.cast.cwm.dav.DavClientManager;
import org.cast.cwm.dav.DavResource;
import org.cast.cwm.glossary.Glossary;
import org.cast.cwm.glossary.GlossaryService;
import org.cast.cwm.glossary.GlossaryTransformer;
import org.cast.cwm.indira.FileResourceManager;
import org.cast.cwm.indira.IndiraImage;
import org.cast.cwm.indira.IndiraImageComponent;
import org.cast.cwm.indira.IndiraMarkupParserFactory;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.HighlightService;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.tag.TagService;
import org.cast.cwm.xml.FileResource;
import org.cast.cwm.xml.IDocumentObserver;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlDocumentList;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.parser.DtbookParser;
import org.cast.cwm.xml.service.XmlService;
import org.cast.cwm.xml.transform.EnsureUniqueWicketIds;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.cwm.xml.transform.TransformChain;
import org.cast.cwm.xml.transform.XslTransformer;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIEvent;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.page.ExceptionPage;
import org.cast.isi.page.GlossaryPage;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.Login;
import org.cast.isi.page.Notebook;
import org.cast.isi.page.PeriodResponsePage;
import org.cast.isi.page.TeacherNotesPopup;
import org.cast.isi.page.Whiteboard;
import org.cast.isi.panel.AbstractNavBar;
import org.cast.isi.panel.FooterPanel;
import org.cast.isi.panel.FreeToolbar;
import org.cast.isi.panel.HeaderPanel;
import org.cast.isi.service.ISIEmailService;
import org.cast.isi.service.ISIResponseService;
import org.cast.isi.service.QuestionService;
import org.hibernate.Session;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.contrib.tinymce.settings.TinyMCESettings;
import wicket.contrib.tinymce.settings.TinyMCESettings.Theme;

import com.visural.wicket.util.lesscss.LessCSSResourceStreamLocator;

public abstract class ISIApplication extends CwmApplication {
	
	private static final Logger log = LoggerFactory.getLogger(ISIApplication.class);
	
	// Constants
	public static final String PAGE_NOTES_EVENT_TYPE = "post:pagenotes";
	public static final String NOTEBOOK_NOTES_EVENT_TYPE = "post:notebooknotes";
	protected static final String DEFAULT_STUDENT_CONTENT_FILE_NAMES = "student";
	protected static final String DEFAULT_GLOSSARY_TYPE = "modal";
	protected static final String GLOSSARY_TYPE_INLINE = "inline";
	protected static final String GLOSSARY_TYPE_MODAL = "modal";
	protected static final String GLOSSARY_TYPE_MAIN = "main";
	
	// Popup settings
	protected static final int POPUP_WIDTH = 800;
	protected static final int POPUP_HEIGHT = 600;
	protected static final int POPUP_FLAGS = PopupSettings.RESIZABLE+PopupSettings.SCROLLBARS;
	
	@Getter protected static final PopupSettings glossaryPopupSettings = new CwmPopupSettings ("glossary", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	@Getter protected static final PopupSettings whiteboardPopupSettings = new CwmPopupSettings ("whiteboard", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	@Getter protected static final PopupSettings notebookPopupSettings = new CwmPopupSettings ("notebook", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	@Getter protected static final PopupSettings periodResponsePopupSettings = new CwmPopupSettings ("compare", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	@Getter public static final PopupSettings teacherNotesPopupSettings = new CwmPopupSettings ("tnotebook", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	@Getter public static final PopupSettings questionPopupSettings = new CwmPopupSettings ("questionp", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	
	// These settings are generally overridden by applications in the configuration file
	@Getter @Setter protected String sectionElement = "level3"; // XML element that is one section, usually
	@Getter @Setter protected String pageElement    = "level4";    // XML element that is one page, usually
	@Getter protected String readingAgentName = "the Penguin";
	@Getter protected String responseAgentName = "the Gecko";
	@Getter protected boolean ratePanelIncludesDone = true;
	@Getter protected boolean notebookOn = true;
	@Getter protected boolean whiteboardOn = true;
	@Getter protected boolean glossaryOn = true;
	@Getter protected boolean myQuestionsOn = true;
	@Getter protected boolean responseCollectionsOn = true;
	@Getter protected boolean tagsOn = true;		
	@Getter protected boolean pageNotesOn = true;	// margin notes
	@Getter protected boolean classMessageOn = true;
	@Getter protected boolean pageNumbersOn = true;  // ALL page numbers FIXME need one for TOC only-ldm
	@Getter protected boolean toolBarOn = true; // text help, dictionary
	@Getter protected boolean mathMLOn = false; 
	@Getter protected String glossaryLinkType = DEFAULT_GLOSSARY_TYPE;
	@Getter protected boolean useAuthoredResponseType = false; // false for backwards compatibility
	@Getter protected String responseSortField = "createDate";
	@Getter protected int responseSortState = ISortState.DESCENDING;
	@Getter protected ResponseMetadata responseMetadata = new ResponseMetadata(); // the default type of responses
	@Getter protected ArrayList<IResponseType> defaultResponseTypes = new ArrayList<IResponseType>();
	protected TinyMCESettings tinyMCESettings = null;

	// highlighter settings from the config file
	@Getter protected boolean highlightsPanelOn = true;
	@Getter protected boolean yHighlighterOn = true;
	@Getter protected boolean bHighlighterOn = true;
	@Getter protected boolean gHighlighterOn = true;
	@Getter protected boolean yHighlighterEditable = false;
	@Getter protected boolean bHighlighterEditable = false;
	@Getter protected boolean gHighlighterEditable = true;

	// rss feed info from the config file
	@Getter protected boolean rssFeedOn = false; 
	@Getter protected String rssFeedUrl;
	@Getter protected int maxRssFeedItems;
	
	// Service Classes and Plugins
	@Getter @Setter protected Glossary glossary;
	@Getter @Setter protected ISITagLinkBuilder tagLinkBuilder = new ISITagLinkBuilder();
	@Getter @Setter protected ISITagLinkBuilder tagPopupLinkBuilder = null;
	@Getter @Setter protected QuestionService questionService = new QuestionService();
	
	protected String[] studentContentFiles;
	@Getter protected XmlDocumentList studentContent = new XmlDocumentList();
	
	//email related
	@Getter protected boolean emailOn = false;			// ability to email, includes password reminder
	@Getter protected boolean selfRegisterOn = false; 	// users can create their own logins
	@Getter protected String EMAIL_FILE_NAME = "email.xml";
	@Getter protected String EMAIL_TRANSFORMER = "email";

	// Application URL - needed for email links
	@Getter protected String url;
	@Getter protected XmlDocument emailContent;
	
	protected List<IDocumentObserver> documentObservers = new ArrayList<IDocumentObserver>();
	protected static Time lastFileCheck;
	protected List<String> enabledFeatures = new ArrayList<String>();

	protected void init() {
		
		EventService.get().setEventClass(ISIEvent.class);

		// Set xml content Section and Page based on property file - these have to be set before
		// the super.init is called
		sectionElement = appProperties.getProperty("isi.sectionElement");
		pageElement = appProperties.getProperty("isi.pageElement");

		super.init();
		
		ISIEmailService.useAsServiceInstance();
		ISIResponseService.useAsServiceInstance();
		ISIResponseService.get().setResponseClass(getResponseClass());
		XmlService.get().setXmlSectionClass(getXmlSectionClass());
		
		
		// Gather Extended Browser Information - Uses a wicket-administered redirect.
		// May not be necessary.
		// 
		// If enabled, see Login.java for pre-fetching of data so it does not break
		// the login form.
		//
		// getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
		
		getMarkupSettings().setDefaultBeforeDisabledLink( "" );
		getMarkupSettings().setDefaultAfterDisabledLink( "" ); 
		
		// Strip tags so that jQuery traversal doesn't break
		getMarkupSettings().setStripWicketTags(true);
				
		// Tells ResourceFinder to look in skin directory before looking in context dir.
		if (getCustomSkinDir() != null) 
			getResourceSettings().addResourceFolder(getCustomSkinDir());
		getResourceSettings().addResourceFolder(getSkinDir());
		getResourceSettings().setResourceStreamLocator(new LessCSSResourceStreamLocator());
		
		// Indira rewrites markup for images
		getMarkupSettings().setMarkupParserFactory(new IndiraMarkupParserFactory());

		// FileResourceManager manages external images and files.
		FileResourceManager.addKnownExtensions("jar");  // Java Applets
		if (getCustomSkinDir() != null) 
			FileResourceManager.get().addFolder("theme", new Folder(getCustomSkinDir()));
		FileResourceManager.get().addFolder("themeBase", new Folder(getSkinDir()));
		FileResourceManager.get().addFolder("content", new Folder(getContentDir()));
		FileResourceManager.get().scanFolders(false); // appears to be necessary for file download links to work

		// Content elements are taggable
		TagService.get().configureTaggableClass('P', ContentElement.class);
		String requestedTags = appProperties.getProperty("isi.defaultTags", "");
		if (!Strings.isEmpty(requestedTags))
			TagService.get().setDefaultTags(Arrays.asList(requestedTags.split("\\s*,\\s*")));

		// get the configuration from the properties file
		configureApplicationProperties();
		configureResponseTypes();
		configureResponseSort();
		if (highlightsPanelOn) 
			registerHighlighters();		
		
		// load the xml documents and xsl transformers
		loadXmlFiles();
		loadXslFiles();

		// Tell dialog border to not use it's css
		List<ResourceReference> noStylesheets = Collections.emptyList();
		DialogBorder.setStyleReferences(noStylesheets);

		// Generally helpful log statement.
		if (!DEVELOPMENT.equalsIgnoreCase(getConfigurationType())) {
			log.warn("********************** Wicket is running in Deployment Mode **********************");
		}
	}

	protected void registerHighlighters() {
		// These letters are the same as used in the markup.  Don't change one without changing the other
		// or just change the css and leave these alone.
		yHighlighterOn = setBooleanProperty("isi.highlighter.yellow.isOn", yHighlighterOn );
		yHighlighterEditable = setBooleanProperty("isi.highlighter.yellow.nameEditable", yHighlighterEditable );

		bHighlighterOn = setBooleanProperty("isi.highlighter.blue.isOn", bHighlighterOn );
		bHighlighterEditable = setBooleanProperty("isi.highlighter.blue.nameEditable", bHighlighterEditable );

		gHighlighterOn = setBooleanProperty("isi.highlighter.green.isOn", gHighlighterOn );
		gHighlighterEditable = setBooleanProperty("isi.highlighter.green.nameEditable", gHighlighterEditable );

		HighlightService.get().addHighlighter('Y', null, yHighlighterEditable);
		HighlightService.get().addHighlighter('B', null, bHighlighterEditable);
		HighlightService.get().addHighlighter('G', null, gHighlighterEditable);		
	}

	
	/**
	 * Method to call with any BookmarkablePageLink that will set appropriate PopupSettings
	 * (or it can be extended to do other link configuration if necessary).
	 * 
	 * @param the BookmarkablePageLink
	 */	
	public void setLinkProperties (BookmarkablePageLink<?> link) {
		if (GlossaryPage.class.isAssignableFrom(link.getPageClass())) {
			link.setPopupSettings(glossaryPopupSettings);
		} else if (Notebook.class.isAssignableFrom(link.getPageClass())) {
			link.setPopupSettings(notebookPopupSettings);
		} else if (Whiteboard.class.isAssignableFrom(link.getPageClass())) {
			link.setPopupSettings(whiteboardPopupSettings);
		} else if (TeacherNotesPopup.class.isAssignableFrom(link.getPageClass())) {
			link.setPopupSettings(teacherNotesPopupSettings);
		} else if (PeriodResponsePage.class.isAssignableFrom(link.getPageClass())) {
			link.setPopupSettings(periodResponsePopupSettings);
		}
	}
	
	@Override
	protected void configureHibernate(AnnotationConfiguration ac) {
		super.configureHibernate(ac);
		
		Configuration c = ac;
		TagService.configureTagClasses(c);
		c.addAnnotatedClass(org.cast.isi.data.ISIEvent.class);
		c.addAnnotatedClass(org.cast.isi.data.ISIResponse.class);
		c.addAnnotatedClass(org.cast.isi.data.SectionStatus.class);
		c.addAnnotatedClass(org.cast.isi.data.ClassMessage.class);
		c.addAnnotatedClass(org.cast.isi.data.StudentFlag.class);
		c.addAnnotatedClass(org.cast.isi.data.FeedbackMessage.class);
		c.addAnnotatedClass(org.cast.isi.data.WordCard.class);
		c.addAnnotatedClass(org.cast.isi.data.ISIPrompt.class);
		c.addAnnotatedClass(org.cast.isi.data.ContentElement.class);
		c.addAnnotatedClass(org.cast.isi.data.Question.class);
	}
	
	protected void configureApplicationProperties() {
		// Set Application settings based on property file
		notebookOn = setBooleanProperty("isi.notebook.isOn", notebookOn);
		whiteboardOn = setBooleanProperty("isi.whiteboard.isOn", whiteboardOn);
		glossaryOn = setBooleanProperty("isi.glossary.isOn", glossaryOn);
		myQuestionsOn = setBooleanProperty("isi.myQuestion.isOn", myQuestionsOn);
		responseCollectionsOn = setBooleanProperty("isi.responseCollection.isOn", responseCollectionsOn);
		tagsOn = setBooleanProperty("isi.tag.isOn", tagsOn);
		highlightsPanelOn = setBooleanProperty("isi.highlightsPanel.isOn", highlightsPanelOn);
		pageNotesOn = setBooleanProperty("isi.pageNotes.isOn", pageNotesOn);
		pageNumbersOn = setBooleanProperty("isi.pageNumbers.isOn", pageNumbersOn);
		classMessageOn = setBooleanProperty("isi.classMessage.isOn", classMessageOn);
		toolBarOn = setBooleanProperty("isi.toolBar.isOn", toolBarOn);
		mathMLOn = setBooleanProperty("isi.mathML.isOn", mathMLOn);
		useAuthoredResponseType = setBooleanProperty("isi.useAuthoredResponseType.isOn", useAuthoredResponseType);	
		rssFeedOn = setBooleanProperty("isi.rssFeed.isOn", rssFeedOn);
		emailOn = setBooleanProperty("isi.email.isOn", emailOn);
		selfRegisterOn = setBooleanProperty("isi.selfRegister.isOn", selfRegisterOn);

		String urlValue = appProperties.getProperty("app.url");
		if (urlValue != null) {
			url = urlValue.trim();
			log.info("using this URL starter for email links: {}", url);
		}

		/* if the glossary is on, decide what type of glossary link is used */
		if (glossaryOn == true) {
			String glossaryTypeProperty =  appProperties.getProperty("isi.glossary.type");
			if (glossaryTypeProperty != null && 
					(glossaryTypeProperty.trim().equals(GLOSSARY_TYPE_INLINE) || 
							glossaryTypeProperty.trim().equals(GLOSSARY_TYPE_MAIN) || 
							glossaryTypeProperty.trim().equals(GLOSSARY_TYPE_MODAL))) {
				glossaryLinkType = glossaryTypeProperty.trim();
			}
			log.info("Type of of glossary link is  = {}", glossaryLinkType);
		}

		// Wordnik API key, used if available for free dictionary
		String wordnikKey = appProperties.getProperty("isi.wordnikApiKey");
		if (wordnikKey != null) {
			System.setProperty("WORDNIK_API_KEY", wordnikKey);
			try {
				if (AccountApi.apiTokenStatus().isValid())
					log.info("Valid Wordnik API key confirmed");
				else
					log.warn("Wordnik API key is not valid");
			} catch (KnickerException e) {
				log.error("Wordnik API failure");
				e.printStackTrace();
			}
		} else {
			log.info("Wordnik will not be used");
		}

		if (rssFeedOn == true) {
			rssFeedUrl =  appProperties.getProperty("isi.rssFeed.url").trim();
			log.info("Using this URL for the RSS feed: {}", rssFeedUrl);
			maxRssFeedItems =  Integer.parseInt(appProperties.getProperty("isi.rssFeed.maxItems").trim());
			log.info("The RSS feed will have at most {} items displayed.", maxRssFeedItems);
		}

	
	}

	protected Boolean setBooleanProperty(String property, boolean defaultPropertyValue) {
		String propertyValue =  appProperties.getProperty(property);
		if (propertyValue != null) {
			log.info("Value of {} is  = {}", property, propertyValue);
			return Boolean.valueOf(propertyValue.trim());
		}
		log.info("Value of {} is = {}", property, defaultPropertyValue);
		return defaultPropertyValue; 
	}

	/**
	 * determine what the default response types are used
	 */
	public void configureResponseTypes() {
		// Set default response types based on comma separated values in the property file
		String responseTypeList = appProperties.getProperty("isi.defaultResponse.type");
		String[] responseTypes = null;
		if (responseTypeList != null) {
			responseTypes = responseTypeList.split("\\s*,\\s*");	
		}
		CwmApplication app = CwmApplication.get();
		if (responseTypes == null) {
			log.info("No Response Types Set - using the Defaults: text, image, audio, file and table");
			defaultResponseTypes.add(app.getResponseType("HTML"));
			defaultResponseTypes.add(app.getResponseType("AUDIO"));
			defaultResponseTypes.add(app.getResponseType("SVG"));
			defaultResponseTypes.add(app.getResponseType("UPLOAD"));
			defaultResponseTypes.add(app.getResponseType("TABLE"));
		} else {
			for (String responseType : responseTypes) {
				log.info("Adding the application Response type {}", responseType);
				if (responseType.toLowerCase().trim().equals("text"))
					defaultResponseTypes.add(app.getResponseType("HTML"));				
				else if (responseType.toLowerCase().trim().equals("image"))
					defaultResponseTypes.add(app.getResponseType("SVG"));
				else if (responseType.toLowerCase().trim().equals("audio"))
					defaultResponseTypes.add(app.getResponseType("AUDIO"));
				else if (responseType.toLowerCase().trim().equals("file"))
					defaultResponseTypes.add(app.getResponseType("UPLOAD"));
				else if (responseType.toLowerCase().trim().equals("table"))
					defaultResponseTypes.add(app.getResponseType("TABLE"));
				else log.error("This Response Type {} is NOT valid and will NOT be added", responseType);
			}
		}
		if (defaultResponseTypes.isEmpty())
			log.error("There are no valid response types defined");
		
		for (IResponseType rt : defaultResponseTypes) {
			responseMetadata.addType(rt);
		}
	}

	/**
	 * determine any custom response sorting
	 */
	public void configureResponseSort() {
		String field = appProperties.getProperty("isi.response.sortField");
		String state = appProperties.getProperty("isi.response.sort");

		if (field != null) {
			responseSortField = field.trim();
			log.info("The Response will be sorted by: {}", responseSortField);
		}
		if (state != null) {
			state = state.trim();
			if (state.equalsIgnoreCase("ascending")) 
				responseSortState = ISortState.ASCENDING;
			else if (state.equalsIgnoreCase("descending")) 
				responseSortState = ISortState.DESCENDING;
			else if (state.equalsIgnoreCase("none")) 
				responseSortState = ISortState.NONE;
			log.info("The Response have a sort order: {}", state);
		}
	}
	
	/**
	 * Load XML documents
	 */
	protected void loadXmlFiles() {
		
		String davServer  = getDavServer();
		XmlService xmls = XmlService.get();

		if (davServer != null) {
			final String davUser = appProperties.getProperty("isi.davUser");
			final String davPassword = appProperties.getProperty("isi.davPassword");
			
			DavClientManager manager = DavClientManager.get();
			manager.setDefaultAuthentication (davUser, davPassword);
			manager.createClient (davServer, davServer, getContentDir());
		}
		
		// Load glossary if there is one
		String glossaryFileName = appProperties.getProperty("isi.glossaryFile");
		if (glossaryOn == false) {
			log.debug("Glossary is turned off");
		} else if (Strings.isEmpty(glossaryFileName)) {
			log.debug("No glossary file");
		} else { // when the glossary is on
			Resource glossaryResource;
			if (davServer != null) {
				glossaryResource = new DavResource(davServer, getContentDir() + "/" + glossaryFileName);
			} else {
				glossaryResource = new FileResource(new File(getContentDir(), glossaryFileName));
			}
			final XmlDocument glossaryDoc = xmls.loadXmlDocument("glossary", glossaryResource, new DtbookParser(), null);
			
			// Set up Glossary
			Databinder.ensureSession(new SessionUnit() {
				public Object run(Session sess) {
					glossary = GlossaryService.get().parseXmlGlossaryDocument(glossaryDoc);
					return null;
				}
			});
		}
				
		// Load student content files
		String fileList = appProperties.getProperty("isi.studentContentFiles", DEFAULT_STUDENT_CONTENT_FILE_NAMES).trim();
		studentContentFiles = fileList.split("\\s*,\\s*");		
		documentObservers.add(new XmlDocumentObserver()); // Use set so sub-classed applications can add to it as well
		for (String file : studentContentFiles) {
			Resource resource;
			if (davServer != null) {
				log.debug("attempting to load DavResource file = {}", getContentDir() + "/" + file);
				log.debug("loading the DavResource on the Server = {}", davServer);
				resource = new DavResource(davServer, getContentDir() + "/" + file);
			} else {
				log.debug("attempting to load Resource file = {}", getContentDir() + "/" + file);
				resource = new FileResource(new File(getContentDir(), file));
			}
			XmlDocument doc = xmls.loadXmlDocument(file, resource, new DtbookParser(), documentObservers);
			studentContent.add(doc);
		}
		
		// Load email content files
		if (emailOn) {
			String emailFileName = EMAIL_FILE_NAME;					
			Resource emailResource;
			if (davServer != null) {
				emailResource = new DavResource(davServer, getContentDir() + "/" + emailFileName);
			} else {
				emailResource = new FileResource(new File(getContentDir(), emailFileName));
			}
			emailContent = xmls.loadXmlDocument("email", emailResource, new DtbookParser(), null);					
		}
		
	}

	/**
	 * Load XSL transformer files
	 */
	protected void loadXslFiles() {
		XmlService xmls = XmlService.get();

		// load the transformation directories into the list managed by XmlService
		// load the custom directory first, then the base directory (if they are different)
		List<String> xslTransformerDirectories = new ArrayList<String>();
		xslTransformerDirectories.add(getCustomTransformationDir());
		if (!getCustomTransformationDir().equals(getTransformationDir()))
			xslTransformerDirectories.add(getTransformationDir());
		xmls.setTransformerDirectories(xslTransformerDirectories);

		// Transformers
		xmls.loadXSLTransformer("glossary", getGlossaryTransformationFile(), true);
		xmls.loadXSLTransformer("toc", getTocTransformationFile(), true);
		
		// check for the student transformation file in the custom dir first then base dir
		File studentXslFile = new File(getCustomTransformationDir(), getStudentTransformationFile());
		if (!studentXslFile.exists())
			studentXslFile = new File(getTransformationDir(), getStudentTransformationFile());
		
		// For comparing responses, need to filter down to a single response area and invoke custom XSL
		TransformChain compareChain = new TransformChain(
				new FilterElements(),
				new XslTransformer(xmls.findXslResource("compare-responses.xsl")),
				new EnsureUniqueWicketIds());
		xmls.registerTransformer("compare-responses", compareChain);

		
		// Construct transformation pipeline for student content: glossary -> XSL -> unique wicket:ids
		TransformChain transformchain = new TransformChain(
				new GlossaryTransformer(glossary),
				new FilterElements(),
				new XslTransformer(new FileResource(studentXslFile)),
				new EnsureUniqueWicketIds());
		xmls.registerTransformer("student", transformchain);	
		
		if (emailOn) {
			// For sending emails to users
			xmls.loadXSLTransformer("email", getEmailTransformationFile(), true);
		}

	}

	
	public static ISIApplication get() {
		return (ISIApplication) Application.get();
	}

	
	@Override
	public org.apache.wicket.Session newSession(Request request, Response response) {
		return new ISISession(request);
	}
	
	@Override
	public RequestCycle newRequestCycle (final Request request, final Response response) {
		return new DataRequestCycle (this, (WebRequest) request, (WebResponse) response) {
			
			@Override
			public Page onRuntimeException(final Page cause, final RuntimeException e) {
				super.onRuntimeException(cause, e);  // Executes some methods
				return new ExceptionPage(cause != null ? cause.getPageParameters() : null, e);
			}
		};
	}
	
	@Override
	public AjaxRequestTarget newAjaxRequestTarget (final Page page) {
		AjaxRequestTarget target = super.newAjaxRequestTarget(page);
		target.appendJavascript(SessionExpireWarningDialog.getResetJavascript());
		return target;
	}

	// TODO: When shifting to the Cwm-Data 0.8-Snapshot, swap to the CwmApplication method
	@Override
	protected void configureMountPaths() {
		super.configureMountPaths();
		mount(new QueryStringUrlCodingStrategy("login", getSignInPageClass()));
		mount(new QueryStringUrlCodingStrategy("home", getStudentTOCPageClass()));
		mount(new QueryStringUrlCodingStrategy("thome", getTeacherTOCPageClass()));
		mount(new QueryStringUrlCodingStrategy("ahome", getAdminHomePageClass()));
		mount(new QueryStringUrlCodingStrategy("reading", getStudentReadingPageClass()));
		mount(new QueryStringUrlCodingStrategy("treading", getTeacherReadingPageClass()));
		mount(new QueryStringUrlCodingStrategy("glossary", getGlossaryPageClass()));
		mount(new QueryStringUrlCodingStrategy("notebook", getNotebookPageClass()));
		mount(new QueryStringUrlCodingStrategy("tags", getTagsPageClass()));
		mount(new QueryStringUrlCodingStrategy("questions", getMyQuestionsPageClass()));
		mount(new QueryStringUrlCodingStrategy("questionp", getQuestionPopupPageClass()));
		mount(new QueryStringUrlCodingStrategy("collections", getResponseCollectionsPageClass()));
		mount(new QueryStringUrlCodingStrategy("whiteboard", getWhiteboardPageClass()));
		mount(new QueryStringUrlCodingStrategy("compare", getPeriodResponsePageClass()));
		mount(new QueryStringUrlCodingStrategy("tnotebook", getTeacherNotesPageClass()));
		mount(new QueryStringUrlCodingStrategy("manage", getManageClassesPageClass()));
		mount(new QueryStringUrlCodingStrategy("register", getRegisterPageClass()));
		mount(new QueryStringUrlCodingStrategy("reset", getForgotPasswordPageClass()));
		mount(new QueryStringUrlCodingStrategy("password", getPasswordPageClass()));
		
	}
	
	
	@Override
	public Class<? extends WebPage> getHomePage(Role role) {
		if (role.equals(Role.ADMIN) || role.equals(Role.RESEARCHER)) 
			return getAdminHomePageClass();
		if (role.equals(Role.TEACHER) || role.equals(Role.STUDENT))
			return getTocPageClass(role);
		return getSignInPageClass();
	}

	@Override
	public Class<? extends WebPage> getSignInPageClass() {
		return Login.class;
	}

	@Override
	public Class<? extends WebPage> getHomePage() {
		ISISession s = ISISession.get();
		if (s != null && s.getUser() != null)
			return getHomePage(s.getUser().getRole());
		else
			return getSignInPageClass();
	}

	/**
	 * Get the Class for the Table of Contents for the given role.  If the role
	 * is null, attempts to determine the role from the currently logged in user.  If
	 * there is no user, returns null.
	 * 
	 * @return
	 */
	public Class<? extends WebPage> getTocPageClass(Role role) {
		if (role == null) {
			if (ISISession.get().getUser() != null)
				role = ISISession.get().getUser().getRole();
			else
				return null;
		}
		if (role.subsumes(Role.TEACHER))
			return getTeacherTOCPageClass();
		return getStudentTOCPageClass();
	}
	public Class<? extends ISIStandardPage> getReadingPageClass() {
		if (Role.TEACHER.equals(CwmSession.get().getUser().getRole()))
			return getTeacherReadingPageClass();
		else
			return getStudentReadingPageClass();
	}

	public Class<? extends WebPage> getStudentTOCPageClass() {
		return org.cast.isi.page.StudentToc.class;
	}
	public Class<? extends WebPage> getTeacherTOCPageClass() {
		return org.cast.isi.page.TeacherToc.class;
	}
	public Class<? extends WebPage> getAdminHomePageClass() {
		return org.cast.isi.page.AdminHome.class;
	}
	public Class<? extends WebPage> getGlossaryPageClass() {
		return org.cast.isi.page.GlossaryPage.class;
	}
	public Class<? extends WebPage> getNotebookPageClass() {
		return org.cast.isi.page.Notebook.class;
	}
	public Class<? extends WebPage> getResponseCollectionsPageClass() {
		return org.cast.isi.page.ResponseCollections.class;
	}
	public Class<? extends WebPage> getWhiteboardPageClass() {
		return org.cast.isi.page.Whiteboard.class;
	}
	public Class<? extends WebPage> getTagsPageClass() {
		return org.cast.isi.page.Tags.class;
	}
	public Class<? extends WebPage> getMyQuestionsPageClass() {
		return org.cast.isi.page.MyQuestions.class;
	}
	public Class<? extends WebPage> getQuestionPopupPageClass() {
		return org.cast.isi.page.QuestionPopup.class;
	}
	public Class<? extends WebPage> getPeriodResponsePageClass() {
		return org.cast.isi.page.PeriodResponsePage.class;
	}
	public Class<? extends WebPage> getTeacherNotesPageClass() {
		return org.cast.isi.page.TeacherNotesPopup.class;
	}
	public Class<? extends ISIStandardPage> getStudentReadingPageClass() {
		return org.cast.isi.page.Reading.class;
	}
	public Class<? extends ISIStandardPage> getTeacherReadingPageClass() {
		return org.cast.isi.page.TeacherReading.class;
	}
	public Class<? extends ISIStandardPage> getManageClassesPageClass() {
		return org.cast.isi.page.ManageClasses.class;
	}
	public Class<? extends WebPage> getRegisterPageClass() {
		return org.cast.isi.page.Register.class;
	}
	public Class<? extends WebPage> getForgotPasswordPageClass() {
		return org.cast.isi.page.ForgotPassword.class;
	}
	public Class<? extends WebPage> getPasswordPageClass() {
		return org.cast.isi.page.Password.class;
	}

	public Class<? extends org.cast.cwm.data.Response> getResponseClass() {
		return org.cast.isi.data.ISIResponse.class;
	}
	
	public Class<? extends org.cast.cwm.xml.XmlSection> getXmlSectionClass() {
		return org.cast.isi.ISIXmlSection.class;
	}

	public Component getToolbar (String id, Page page) {
		return (new FreeToolbar("tht")).setVisible(isToolBarOn());
	}

	// TODO change context from type string to enum
	public TinyMCESettings getTinyMCESettings(String context) {
		if (tinyMCESettings == null) {
			tinyMCESettings = new ISITinyMCESettings(Theme.advanced);
		}
		return tinyMCESettings;
	}

	// over ride this class to add custom js, css that is needed by the entire application
	public void getCustomRenderHead(IHeaderResponse response) {
	}


	/**
	 * Get a link to this application's Table of Contents, based on the currently
	 * logged in user.
	 * 
	 * @param id wicket id of the link
	 * @return
	 */
	public BookmarkablePageLink<ISIStandardPage> getTocLink(String id) {
		return getTocLink(id, null);
	}
	
	/**
	 * Get a link to this application's Table of Contents, based on the currently
	 * logged in user.  Setting a valid {@link ContentLoc} will jump to that 
	 * spot in the TOC.
	 * 
	 * @param id wicket id of the link
	 * @param loc location to open in the TOC.
	 * @return
	 */
	public BookmarkablePageLink<ISIStandardPage> getTocLink(String id, ContentLoc loc) {

		Class<? extends WebPage> clazz = getTocPageClass(null);
		BookmarkablePageLink<ISIStandardPage> link;
		if (clazz != null) {
			link = new BookmarkablePageLink<ISIStandardPage>(id, getTocPageClass(null));
		} else { 
			link = new BookmarkablePageLink<ISIStandardPage>(id, ISIApplication.get().getSignInPageClass());
		}
		
		if (loc != null) {
			link.setParameter("loc", loc.getLocation());
		}
		
		return link;
	}
	
	public String getContextPath() {
		return getServletContext().getContextPath();
	}
	
	public String getContextDir() {
		return getServletContext().getRealPath("/");
	}

	public String getContentDir() {
		return (appProperties.getProperty("isi.contentDir")).trim();
	}
		
	public String getSkinDir() {
		return (appProperties.getProperty("isi.skinDir")).trim();
	}

	public String getCustomSkinDir() {
		String csd =  appProperties.getProperty("isi.customSkinDir");
		if (csd != null) {
			return (appProperties.getProperty("isi.customSkinDir")).trim();
		}
		return null;	
	}
	
	public String getTransformationDir() {
		String td =  appProperties.getProperty("isi.transformationDir");
		if (td != null) {
			return (appProperties.getProperty("isi.transformationDir")).trim();
		}
		return getSkinDir();
	}

	public String getGlossaryTransformationFile() {
		String tf =  appProperties.getProperty("isi.xslGlossaryFile");
		if (tf != null) {
			return (appProperties.getProperty("isi.xslGlossaryFile")).trim();
		}
		return "glossary.xsl";
	}

	public String getStudentTransformationFile() {
		String tf =  appProperties.getProperty("isi.xslStudentFile");
		if (tf != null) {
			return (appProperties.getProperty("isi.xslStudentFile")).trim();
		}
		return "student.xsl";
	}

	public String getTocTransformationFile() {
		String tf =  appProperties.getProperty("isi.xslTocFile");
		if (tf != null) {
			return (appProperties.getProperty("isi.xslTocFile")).trim();
		}
		return "toc.xsl";
	}

	public String getEmailTransformationFile() {
		String tf =  appProperties.getProperty("isi.xslEmailFile");
		if (tf != null) {
			return (appProperties.getProperty("isi.xslEmailFile")).trim();
		}
		return "email.xsl";
	}

	public String getCustomTransformationDir() {
		// if there isn't any custom directory then use the default directory
		String ctd =  appProperties.getProperty("isi.customTransformationDir");
		if (ctd != null) {
			return (appProperties.getProperty("isi.customTransformationDir")).trim();
		}
		return getTransformationDir();
	}
	
	public String getDavServer() {
		String davServer = appProperties.getProperty("isi.davServer");
		if (davServer != null) {
			return (appProperties.getProperty("isi.davServer")).trim();
		}
		return null;
	}
	
	public ISIXmlSection getPageNum(int num) {
		return (ISIXmlSection) studentContent.getByLabel(ISIXmlSection.SectionType.PAGE, num);
	}

	
	/**
	 * Configure the default period
	 */
	public IModel<? extends Period> getMDefaultPeriod() {
		// Set the default Period
		String periodName =  appProperties.getProperty("app.defaultPeriod");
		if (periodName != null) {
			periodName = periodName.trim();
			return SiteService.get().getPeriodByName(periodName);		
		} else {
			// error if this period doesn't exist
			log.error("No default period was found");
		}		
		return null;
	}

	/**
	 * Configure the default site
	 */
	public IModel<? extends Site> getMDefaultSite() {
	// Set the default Site
		String siteName =  appProperties.getProperty("app.defaultSite");
		if (siteName != null) {
			siteName = siteName.trim();
			return SiteService.get().getSiteByName(siteName);		
		} else {
			// error if this site doesn't exist
			log.error("No default site was found");
		}
		return null;
	}


	/**
	 * Return the ContentLoc of the most recent page the user visited.
	 * @return the ContentLoc, or null if it can't be determined.
	 */
	public ContentLoc getBookmarkLoc() {
		ISISession session = ISISession.get();
		ContentLoc bookmark = session.getBookmark();
		if (bookmark!= null)
			return bookmark;
		
		// Not in session, need to do query.
		ISIEvent e = ISIResponseService.get().findLatestMatchingEvent(ISISession.get().getUser(), "pageview:reading");
		if (e != null && e.getPage() != null) {
			bookmark = new ContentLoc(e.getPage());
			session.setBookmark(bookmark);
			return bookmark;
		}

		// User has never visited a page before.  Set bookmark to first page.
		ISIXmlSection section = getPageNum(0);
		if (section != null)
			bookmark = new ContentLoc(section);
		session.setBookmark(bookmark);
		return bookmark;
	}
	
	
	public String getPageTitleBase() {
		return "ISI";
	}
		
	/**
	 * Return an icon to use for a given XmlSection.
	 * This will be based on the class attribute of the section, e.g. "reading" or "activity" or "overview".
	 * It will have alt and title set as the title of the section.
	 */
	public WebComponent iconFor(XmlSection section) {
		return iconFor(section, "");
	}
	
	// TODO: I don't think the suffix is needed anymore in distributable.
	public WebComponent iconFor (XmlSection section, String iconSuffix) {
		IndiraImageComponent icon = makeIcon("icon", section.getClassName() + iconSuffix);
		icon.add(new SimpleAttributeModifier("title", section.getTitle()));
		icon.add(new SimpleAttributeModifier("alt", section.getTitle()));
		return icon;
	}
	
	/**
	 * Return basic icon component for a given class of section.
	 * This method should be customized to match naming convention of the images,
	 * and arrange for a default image if the one suggested by the section class
	 * does not exist.
	 * @param wicketId to use for the image
	 * @param sectionClass the class attribute of the section, which determines which image to use
	 * @return an image component
	 */
	public IndiraImageComponent makeIcon (String wicketId, String sectionClass) {
		IndiraImage ii = IndiraImage.get("img/icons/activity_" + sectionClass + ".png");
		if (ii == null) {
			log.debug("Icon not found: {}", sectionClass);
			ii = IndiraImage.get("img/icons/activity_reading.png");  // Default icon to use when no icon matches
		}
		return new IndiraImageComponent(wicketId, new Model<IndiraImage>(ii));
	}
	
	/*============================
	 *== Static Support Methods ==
	 *===========================+
	 */
	
	public static WebComponent statusIconFor(SectionStatus sectionStatus) {
		String imageName = "";
		String alt = "";
		IndiraImage ii = null;
		
		// if there is no sectionStatus record then student hasn't finished this section
		if (sectionStatus == null) {
			imageName = "status_incomplete";
			alt = "Incomplete";
		} else if (sectionStatus.getUnreadStudentMessages() > 0) {
			imageName = "status_message";
			alt = "New feedback from student";
		} else if (!sectionStatus.getCompleted()) {
			imageName = "status_incomplete";
			alt = "Incomplete";
		} else if (Boolean.TRUE.equals(sectionStatus.getReviewed())) {
			imageName = "status_reviewed";
			alt = "Reviewed";
		} else {
			imageName = "status_ready";
			alt = "Ready for review";
		}
		ii = IndiraImage.get("img/icons/" + imageName + ".png");
		IndiraImageComponent icon = new IndiraImageComponent("icon", new Model<IndiraImage>(ii));
		icon.add(new SimpleAttributeModifier("alt", alt));
		icon.add(new SimpleAttributeModifier("title", alt));
		return icon;
	}
	
		
	/*=========================
	 *== Static Link Classes ==
	 *=========================
	 */

	public static class ContentsLink extends Link<ContentLoc> {
		private static final long serialVersionUID = 1L;

		private ContentLoc bookmark = null;
		
		public ContentsLink (String id) {
			super(id);
		}
		
		public ContentsLink (String id, ContentLoc bookmark) {
			this(id);
			this.bookmark = bookmark;
		}
		
		@Override
		public void onClick() {
			if (bookmark == null)
				bookmark = ISIApplication.get().getBookmarkLoc();
			PageParameters ppar = new PageParameters();
			ppar.add("loc", bookmark.getLocation());
			setResponsePage(ISIApplication.get().getReadingPageClass(), ppar);
		}
	}

	public static class LogoutLink extends Link<Page> {
		private static final long serialVersionUID = 1L;
		public LogoutLink(String name) {
			super(name);
		}
		@Override
		public void onClick() {
			AuthDataSession.get().signOut();
			getRequestCycle().setRedirect(true);
			setResponsePage(ISIApplication.get().getSignInPageClass());
		}
	}
	
	/**
	 * This is a link that, when clicked, will throw an exception.  Used for testing. 
	 *
	 * @author jbrookover
	 *
	 */
	public static class ExceptionLink extends Link<Exception> {

		private static final long serialVersionUID = 1L;

		public ExceptionLink(String id) {
			super(id);
		}

		@Override
		public void onClick() {
			throw new IllegalStateException("Testing Exceptions");	
		}
	}
	
	/*======================
	 *== Abstract Methods ==
	 *======================
	 */
	
	/**
	 * Returns the header and footer panel to be used on pages for this application.
	 * @return
	 */
	public abstract HeaderPanel getHeaderPanel(String id, PageParameters parameters);
	public abstract FooterPanel getFooterPanel(String id, PageParameters parameters);
	public abstract AbstractNavBar<?> getNavBar(String id, IModel<? extends XmlSection> sec, boolean teacher); // think I need a type here
		
}
