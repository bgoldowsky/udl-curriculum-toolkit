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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import lombok.Getter;
import lombok.Setter;
import net.databinder.auth.hib.AuthDataSession;
import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;
import net.jeremybrooks.knicker.AccountApi;
import net.jeremybrooks.knicker.KnickerException;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.CwmSession;
import org.cast.cwm.IInputStreamProvider;
import org.cast.cwm.InputStreamNotFoundException;
import org.cast.cwm.ThemeDirectoryRequestMapper;
import org.cast.cwm.components.CwmPopupSettings;
import org.cast.cwm.components.Icon;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.DialogBorder;
import org.cast.cwm.data.component.SessionExpireWarningDialog;
import org.cast.cwm.dav.DavClientManager;
import org.cast.cwm.dav.DavResource;
import org.cast.cwm.glossary.Glossary;
import org.cast.cwm.glossary.GlossaryService;
import org.cast.cwm.glossary.GlossaryTransformer;
import org.cast.cwm.service.HighlightService;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.IResponseService;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.service.UserPreferenceService;
import org.cast.cwm.tag.TagService;
import org.cast.cwm.xml.FileResource;
import org.cast.cwm.xml.IDocumentObserver;
import org.cast.cwm.xml.XmlDocument;
import org.cast.cwm.xml.XmlDocumentList;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.parser.DtbookParser;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.cwm.xml.service.XmlService;
import org.cast.cwm.xml.transform.EnsureUniqueWicketIds;
import org.cast.cwm.xml.transform.FilterElements;
import org.cast.cwm.xml.transform.TransformChain;
import org.cast.cwm.xml.transform.XslTransformer;
import org.cast.isi.data.ContentElement;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.ISIEvent;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.page.AuthoredPopup;
import org.cast.isi.page.ExceptionPage;
import org.cast.isi.page.GlossaryPage;
import org.cast.isi.page.ISIStandardPage;
import org.cast.isi.page.Login;
import org.cast.isi.page.Notebook;
import org.cast.isi.page.PeriodResponsePage;
import org.cast.isi.page.TeacherNotesPopup;
import org.cast.isi.page.Whiteboard;
import org.cast.isi.panel.AbstractNavBar;
import org.cast.isi.panel.AlternateNavBar1;
import org.cast.isi.panel.DefaultHeaderPanel;
import org.cast.isi.panel.DefaultNavBar;
import org.cast.isi.panel.FooterPanel;
import org.cast.isi.panel.FreeToolbar;
import org.cast.isi.panel.HeaderPanel;
import org.cast.isi.panel.PageNavPanel;
import org.cast.isi.panel.TeacherHeaderPanel;
import org.cast.isi.service.FeatureService;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.IISIResponseService;
import org.cast.isi.service.ILinkPropertiesService;
import org.cast.isi.service.IPageClassService;
import org.cast.isi.service.IQuestionService;
import org.cast.isi.service.ISIEmailService;
import org.cast.isi.service.ISIEventService;
import org.cast.isi.service.ISIResponseService;
import org.cast.isi.service.ISectionService;
import org.cast.isi.service.LinkPropertiesService;
import org.cast.isi.service.PageClassService;
import org.cast.isi.service.QuestionService;
import org.cast.isi.service.SectionService;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wicket.contrib.tinymce.settings.TinyMCESettings;
import wicket.contrib.tinymce.settings.TinyMCESettings.Theme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	@Getter public static final PopupSettings authoredPopupSettings = new CwmPopupSettings ("authoredp", POPUP_FLAGS).setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
	
	// These settings are generally overridden by applications in the configuration file
	@Getter @Setter protected String sectionElement = "level3"; // XML element that is one section, usually
	@Getter @Setter protected String pageElement    = "level4";    // XML element that is one page, usually
	@Getter protected String alternateNavBar = null;
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
	@Getter protected boolean sectionToggleTextLinksOn = false;
	@Getter protected boolean sectionToggleImageLinksOn = true;
	@Getter protected boolean tocSectionTogglesOn = true;
	@Getter protected boolean tocSectionCompleteIconsOn = false;
	@Getter protected boolean tocSectionIncompleteIconsOn = false;
	@Getter protected boolean collectionsScoreSummaryOn = false;
	@Getter protected boolean compareScoreSummaryOn = false;

	
	@Getter protected String glossaryLinkType = DEFAULT_GLOSSARY_TYPE;
	@Getter protected boolean useAuthoredResponseType = false; // false for backwards compatibility
	@Getter protected String responseSortField = "createDate";
	@Getter protected SortOrder responseSortState = SortOrder.DESCENDING;
	@Getter protected ResponseMetadata responseMetadata; // the default type of responses
	@Getter protected ArrayList<IResponseType> defaultResponseTypes = new ArrayList<IResponseType>();
	protected TinyMCESettings tinyMCESettings = null;

	// highlighter settings from the config file
	@Getter protected boolean highlightsPanelOn = true;

	// rss feed info from the config file
	@Getter protected boolean rssFeedOn = false; 
	@Getter protected String rssFeedUrl;
	@Getter protected int maxRssFeedItems;
	
	// navbar section icons types from the config file
	@Getter protected String navbarSectionIconsTeacher = "status";
	@Getter protected String navbarSectionIconsStudent = "class";


	// Service Classes and Plugins
	@Getter @Setter protected Glossary glossary;
	@Getter @Setter protected ISITagLinkBuilder tagLinkBuilder = new ISITagLinkBuilder();
	@Getter @Setter protected ISITagLinkBuilder tagPopupLinkBuilder = null;
	
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

	protected IXmlService xmlService;

	@Override
	public void loadAppProperties() {
		super.loadAppProperties();
		// get the configuration from the properties file
		configureApplicationProperties();
	}
	
	@Override
	protected void loadServices() {
		log.debug("Loading XML Service");
		super.loadServices();
		xmlService = XmlService.get();
		xmlService.setXmlSectionClass(getXmlSectionClass());
		log.debug("Finished loading XML Service");
		log.debug("Loading Response Service");
		ISIResponseService.useAsServiceInstance();
		ISIResponseService.get().setResponseClass(getResponseClass());
		log.debug("Finished loading Response Service");
	}

	@Override
	protected List<Module> getInjectionModules() {
		List<Module> modules = super.getInjectionModules();
		modules.add(new Module() {
        public void configure(Binder binder) {
    		log.debug("Binding ISI Services");
			// Temporary while we lose the singleton.
   			binder.bind(IXmlService.class).toInstance(xmlService);
			// Temporary while we lose the singleton.
			binder.bind(IISIResponseService.class).toInstance(ISIResponseService.get());
			//TODO: Deal with extended interfaces properly.
			binder.bind(IResponseService.class).toInstance(ISIResponseService.get());
   			binder.bind(ISectionService.class).to(SectionService.class).in(Scopes.SINGLETON);
   			binder.bind(IPageClassService.class).to(PageClassService.class).in(Scopes.SINGLETON);
   			binder.bind(IFeatureService.class).to(FeatureService.class).in(Scopes.SINGLETON);
   			binder.bind(ILinkPropertiesService.class).to(LinkPropertiesService.class).in(Scopes.SINGLETON);
   			binder.bind(IEventService.class).to(ISIEventService.class).in(Scopes.SINGLETON);
   			binder.bind(IQuestionService.class).to(QuestionService.class);
   			binder.bind(IUserPreferenceService.class).to(UserPreferenceService.class).in(Scopes.SINGLETON);
   			log.debug("finished binding ISI Services");
    		}
        });
        return modules;
	}
	
	protected void init() {
		log.debug("Starting ISI Application Init");

		// Set xml content Section and Page based on property file - these have to be set before
		// the super.init is called
		sectionElement = configuration.getProperty("isi.sectionElement");
		pageElement = configuration.getProperty("isi.pageElement");
		super.init();
		
		ISIEmailService.useAsServiceInstance();
		responseMetadata  = new ResponseMetadata();		
		
		getMarkupSettings().setDefaultBeforeDisabledLink( "" );
		getMarkupSettings().setDefaultAfterDisabledLink( "" ); 
		
		// Strip tags so that jQuery traversal doesn't break
		getMarkupSettings().setStripWicketTags(true);
				
		// Tells ResourceFinder to look in skin directory before looking in context dir.
		if (getCustomSkinDir() != null) 
			getResourceSettings().addResourceFolder(getCustomSkinDir());
		getResourceSettings().addResourceFolder(getSkinDir());
		
		// Content elements are taggable
		TagService.get().configureTaggableClass('P', ContentElement.class);
        String requestedTags = configuration.getProperty("isi.defaultTags");
        if(StringUtils.isEmpty(requestedTags)) {
            requestedTags = "";
        }
		if (!Strings.isEmpty(requestedTags))
			TagService.get().setDefaultTags(Arrays.asList(requestedTags.split("\\s*,\\s*")));

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

		getResourceSettings().setPackageResourceGuard(new ISIPackageResourceGuard());

        this.getRequestCycleListeners().add(new AbstractRequestCycleListener() {
            @Override
            public IRequestHandler onException(RequestCycle cycle, Exception x) {
                PageParameters pageParameters = requestParameters2PageParameters(cycle.getRequest().getQueryParameters());
                return new RenderPageRequestHandler(new PageProvider(new ExceptionPage(pageParameters, new RuntimeException(x))));
            }
        });

		// Generally helpful log statement.
		if (!RuntimeConfigurationType.DEVELOPMENT.equals(getConfigurationType())) {
			log.warn("********************** Wicket is running in Deployment Mode **********************");
		}
		log.debug("Finished ISI Application Init");
	}

    /**
     * Converts requestParameters to PageParameters.
     *
     * @param requestParameters
     * @return
     */
    private PageParameters requestParameters2PageParameters(IRequestParameters requestParameters) {
        PageParameters pageParameters = new PageParameters();
        for(String name : requestParameters.getParameterNames()) {
            List<StringValue> values = requestParameters.getParameterValues(name);
            for(StringValue value : values) {
                pageParameters.add(name, value.toString());
            }
        }
        return pageParameters;
    }

	protected void registerHighlighters() {
		// These letters are the same as used in the markup.  Don't change one without changing the other
		// or just change the css and leave these alone.
		boolean yHighlighterOn = setBooleanProperty("isi.highlighter.yellow.isOn", true);
		boolean yHighlighterEditable = setBooleanProperty("isi.highlighter.yellow.nameEditable", true);

		boolean bHighlighterOn = setBooleanProperty("isi.highlighter.blue.isOn", true);
		boolean bHighlighterEditable = setBooleanProperty("isi.highlighter.blue.nameEditable", true);

		boolean gHighlighterOn = setBooleanProperty("isi.highlighter.green.isOn", true);
		boolean gHighlighterEditable = setBooleanProperty("isi.highlighter.green.nameEditable", true);

		HighlightService.get().addHighlighter('Y', yHighlighterOn, yHighlighterEditable);
		HighlightService.get().addHighlighter('B', bHighlighterOn, bHighlighterEditable);
		HighlightService.get().addHighlighter('G', gHighlighterOn, gHighlighterEditable);		
	}

	
	/**
	 * Method to call with any BookmarkablePageLink that will set appropriate PopupSettings
	 * (or it can be extended to do other link configuration if necessary).
	 * 
	 * @param link BookmarkablePageLink
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
		} else if (AuthoredPopup.class.isAssignableFrom(link.getPageClass())) {
			link.setPopupSettings(authoredPopupSettings);
		}
	}
	
	@Override
	protected void configureHibernate(Configuration ac) {
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
		log.debug("Configuring ISI Application Properties");
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
		sectionToggleTextLinksOn = setBooleanProperty("isi.sectionToggleTextLinks.isOn", sectionToggleTextLinksOn);
		sectionToggleImageLinksOn = setBooleanProperty("isi.sectionToggleImageLinks.isOn", sectionToggleImageLinksOn);
		tocSectionTogglesOn = setBooleanProperty("isi.tocSectionToggles.isOn", tocSectionTogglesOn);
		tocSectionCompleteIconsOn = setBooleanProperty("isi.tocSectionCompleteIcons.isOn", tocSectionCompleteIconsOn);
		tocSectionIncompleteIconsOn = setBooleanProperty("isi.tocSectionIncompleteIcons.isOn", tocSectionIncompleteIconsOn);
		useAuthoredResponseType = setBooleanProperty("isi.useAuthoredResponseType.isOn", useAuthoredResponseType);	
		rssFeedOn = setBooleanProperty("isi.rssFeed.isOn", rssFeedOn);
		emailOn = setBooleanProperty("isi.email.isOn", emailOn);
		selfRegisterOn = setBooleanProperty("isi.selfRegister.isOn", selfRegisterOn);
		collectionsScoreSummaryOn = setBooleanProperty("isi.collectionsScoreSummary.isOn", collectionsScoreSummaryOn);
		compareScoreSummaryOn = setBooleanProperty("isi.compareScoreSummary.isOn", compareScoreSummaryOn);
		alternateNavBar = setStringProperty("isi.navBarType", alternateNavBar);
		
		navbarSectionIconsTeacher = setStringProperty("isi.navbar.sectionIcons.teacher", navbarSectionIconsTeacher);
		navbarSectionIconsStudent = setStringProperty("isi.navbar.sectionIcons.student", navbarSectionIconsStudent);

		String urlValue = configuration.getProperty("app.url");
		if (urlValue != null) {
			url = urlValue.trim();
			log.info("using this URL starter for email links: {}", url);
		}
		

		/* if the glossary is on, decide what type of glossary link is used */
		if (glossaryOn == true) {
			String glossaryTypeProperty =  configuration.getProperty("isi.glossary.type");
			if (glossaryTypeProperty != null && 
					(glossaryTypeProperty.trim().equals(GLOSSARY_TYPE_INLINE) || 
							glossaryTypeProperty.trim().equals(GLOSSARY_TYPE_MAIN) || 
							glossaryTypeProperty.trim().equals(GLOSSARY_TYPE_MODAL))) {
				glossaryLinkType = glossaryTypeProperty.trim();
			}
			log.info("Type of of glossary link is  = {}", glossaryLinkType);
		}

		// Wordnik API key, used if available for free dictionary
		String wordnikKey = configuration.getProperty("isi.wordnikApiKey");
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
			rssFeedUrl =  configuration.getProperty("isi.rssFeed.url").trim();
			log.info("Using this URL for the RSS feed: {}", rssFeedUrl);
			maxRssFeedItems =  Integer.parseInt(configuration.getProperty("isi.rssFeed.maxItems").trim());
			log.info("The RSS feed will have at most {} items displayed.", maxRssFeedItems);
		}

		log.debug("Finished configuring ISI Application Properties");
	}

	private String setStringProperty(String property, String defaultPropertyValue) {
		String propertyValue =  configuration.getProperty(property);
		if (propertyValue != null) {
			log.info("Value of {} is  = {}", property, propertyValue);
			return propertyValue.trim();
		}
		log.info("Value of {} is = {}", property, defaultPropertyValue);
		return defaultPropertyValue; 
	}
	
	protected Boolean setBooleanProperty(String property, boolean defaultPropertyValue) {
		String propertyValue =  configuration.getProperty(property);
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
		log.debug("Configuring ISI Response Types");
		// Set default response types based on comma separated values in the property file
		String responseTypeList = configuration.getProperty("isi.defaultResponse.type");
		String[] responseTypes = null;
		if (responseTypeList != null) {
			responseTypes = responseTypeList.split("\\s*,\\s*");	
		}
		if (responseTypes == null) {
			log.info("No Response Types Set - using the Defaults: text, image, audio, file and table");
			defaultResponseTypes.add(getResponseType("HTML"));
			defaultResponseTypes.add(getResponseType("AUDIO"));
			defaultResponseTypes.add(getResponseType("SVG"));
			defaultResponseTypes.add(getResponseType("UPLOAD"));
			defaultResponseTypes.add(getResponseType("TABLE"));
		} else {
			for (String responseType : responseTypes) {
				log.info("Adding the application Response type {}", responseType);
				if (responseType.toLowerCase().trim().equals("text"))
					defaultResponseTypes.add(getResponseType("HTML"));				
				else if (responseType.toLowerCase().trim().equals("image"))
					defaultResponseTypes.add(getResponseType("SVG"));
				else if (responseType.toLowerCase().trim().equals("audio"))
					defaultResponseTypes.add(getResponseType("AUDIO"));
				else if (responseType.toLowerCase().trim().equals("file"))
					defaultResponseTypes.add(getResponseType("UPLOAD"));
				else if (responseType.toLowerCase().trim().equals("table"))
					defaultResponseTypes.add(getResponseType("TABLE"));
				else log.error("This Response Type {} is NOT valid and will NOT be added", responseType);
			}
		}
		if (defaultResponseTypes.isEmpty())
			log.error("There are no valid response types defined");
		
		for (IResponseType rt : defaultResponseTypes) {
			responseMetadata.addType(rt);
		}
		log.debug("Finished configuring ISI Response Types");
	}

	/**
	 * determine any custom response sorting
	 */
	public void configureResponseSort() {
		String field = configuration.getProperty("isi.response.sortField");
		String state = configuration.getProperty("isi.response.sort");

		if (field != null) {
			responseSortField = field.trim();
			log.info("The Response will be sorted by: {}", responseSortField);
		}
		if (state != null) {
			state = state.trim();
			if (state.equalsIgnoreCase("ascending")) 
				responseSortState = SortOrder.ASCENDING;
			else if (state.equalsIgnoreCase("descending")) 
				responseSortState = SortOrder.DESCENDING;
			else if (state.equalsIgnoreCase("none")) 
				responseSortState = SortOrder.NONE;
			log.info("The Response have a sort order: {}", state);
		}
	}

    /**
	 * Load XML documents
	 */
	protected void loadXmlFiles() {
		log.debug("Loading ISI XML Files");
		
		String davServer  = getDavServer();
		if (davServer != null) {
			final String davUser = configuration.getProperty("isi.davUser");
			final String davPassword = configuration.getProperty("isi.davPassword");
			
			DavClientManager manager = DavClientManager.get();
			manager.setDefaultAuthentication (davUser, davPassword);
			manager.createClient (davServer, davServer, getContentDir());
		}
		
		// Load glossary if there is one
		String glossaryFileName = configuration.getProperty("isi.glossaryFile");
		if (glossaryOn == false) {
			log.debug("Glossary is turned off");
		} else if (Strings.isEmpty(glossaryFileName)) {
			log.debug("No glossary file");
		} else { // when the glossary is on

            final XmlDocument glossaryDoc;
            if (davServer != null) {
                IInputStreamProvider glossaryResource = new DavResource(davServer, getContentDir() + "/" + glossaryFileName);
                glossaryDoc = xmlService.loadXmlDocument("glossary", glossaryResource, new DtbookParser(), null);
            } else {
                File glossaryFile = new File(getContentDir(), glossaryFileName);
                glossaryDoc = xmlService.loadXmlDocument("glossary", glossaryFile, new DtbookParser(), null);
            }
			
			// Set up Glossary
			Databinder.ensureSession(new SessionUnit() {
				public Object run(Session sess) {
					glossary = GlossaryService.get().parseXmlGlossaryDocument(glossaryDoc);
					return null;
				}
			});
		}
				
		// Load student content files
		String fileList = configuration.getProperty("isi.studentContentFiles");
        if(StringUtils.isEmpty(fileList)) {
            fileList = DEFAULT_STUDENT_CONTENT_FILE_NAMES;
        }
        else {
            fileList = fileList.trim();
        }
		studentContentFiles = fileList.split("\\s*,\\s*");		
		documentObservers.add(new XmlDocumentObserver(getSectionElement(), getPageElement())); // Use set so sub-classed applications can add to it as well
		for (String file : studentContentFiles) {
            XmlDocument doc;
            if (davServer != null) {
                log.debug("attempting to load DavResource file = {}", getContentDir() + "/" + file);
                log.debug("loading the DavResource on the Server = {}", davServer);
                IInputStreamProvider resource = new DavResource(davServer, getContentDir() + "/" + file);
                doc = xmlService.loadXmlDocument(file, resource, new DtbookParser(), documentObservers);
            } else {
                log.debug("attempting to load Resource file = {}", getContentDir() + "/" + file);
                File resource = new File(getContentDir(), file);
                doc = xmlService.loadXmlDocument(file, resource, new DtbookParser(), documentObservers);
            }
			studentContent.add(doc);
		}
		
		// Load email content files
		if (emailOn) {
			String emailFileName = EMAIL_FILE_NAME;
			if (davServer != null) {
                IInputStreamProvider emailResource = new DavResource(davServer, getContentDir() + "/" + emailFileName);
                emailContent = xmlService.loadXmlDocument("email", emailResource, new DtbookParser(), null);
            } else {
				File emailResource = new File(getContentDir(), emailFileName);
                emailContent = xmlService.loadXmlDocument("email", emailResource, new DtbookParser(), null);
            }
		}
		log.debug("Finished loading ISI XML Files");
	}

	/**
	 * Load XSL transformer files
	 */
	protected void loadXslFiles() {
		log.debug("Loading ISI XSL Files");
		// load the transformation directories into the list managed by XmlService
		// load the custom directory first, then the base directory (if they are different)
		xmlService.addTransformerDirectory(getCustomTransformationDir());
		if (!getCustomTransformationDir().equals(getTransformationDir()))
			xmlService.addTransformerDirectory(getTransformationDir());

		// Transformers
		xmlService.loadXSLTransformer("glossary", getGlossaryTransformationFile(), true);
		xmlService.loadXSLTransformer("toc", getTocTransformationFile(), true);
		
		// check for the student transformation file in the custom dir first then base dir
		File studentXslFile = new File(getCustomTransformationDir(), getStudentTransformationFile());
		if (!studentXslFile.exists())
			studentXslFile = new File(getTransformationDir(), getStudentTransformationFile());

        final File studentXslFileFinal = studentXslFile;

		// For comparing responses, need to filter down to a single response area and invoke custom XSL
		TransformChain compareChain = new TransformChain(
				new FilterElements(),
				new XslTransformer(xmlService.findXslResource("compare-responses.xsl")),
				new EnsureUniqueWicketIds());
		xmlService.registerTransformer("compare-responses", compareChain);

		// For viewing single-select response in whiteboard or notebook, need to filter down to a single response area and invoke custom XSL
		TransformChain viewChain = new TransformChain(
				new FilterElements(),
				new XslTransformer(xmlService.findXslResource("view-response.xsl")),
				new EnsureUniqueWicketIds());
		xmlService.registerTransformer("view-response", viewChain);
		
		// Construct transformation pipeline for student content: glossary -> XSL -> unique wicket:ids
        IInputStreamProvider inputStreamProvider = new IInputStreamProvider() {
            public InputStream getInputStream() throws InputStreamNotFoundException {
                try {
                    return new FileResource(studentXslFileFinal).getResourceStream().getInputStream();
                }
                catch (ResourceStreamNotFoundException x) {
                    throw new InputStreamNotFoundException(x);
                }
            }

            public Time lastModifiedTime() {
                // TODO heikki
                return Time.now();
            }
        };
		TransformChain transformchain = new TransformChain(
				new XslTransformer(xmlService.findXslResource("strip-class.xsl")),
				new GlossaryTransformer(glossary),
				new FilterElements(),
				new XslTransformer(inputStreamProvider),
				new EnsureUniqueWicketIds());
		xmlService.registerTransformer("student", transformchain);	
		
		if (emailOn) {
			// For sending emails to users
			xmlService.loadXSLTransformer("email", getEmailTransformationFile(), true);
		}
		log.debug("Finished loading ISI XSL Files");

	}

	
	public static ISIApplication get() {
		return (ISIApplication) Application.get();
	}

	
	@Override
	public org.apache.wicket.Session newSession(Request request, Response response) {
		return new ISISession(request);
	}


    /* see getCustomRenderHead()
	@Override
	public AjaxRequestTarget newAjaxRequestTarget (final Page page) {
		AjaxRequestTarget target = super.newAjaxRequestTarget(page);
		target.appendJavaScript(SessionExpireWarningDialog.getResetJavascript());
		return target;
	}
	*/

	// TODO: When shifting to the Cwm-Data 0.8-Snapshot, swap to the CwmApplication method
	@Override
	protected void configureMountPaths() {
		super.configureMountPaths();

        File themeDir = new File(ISIApplication.get().getSkinDir());
        getRootRequestMapperAsCompound().add(new ThemeDirectoryRequestMapper(themeDir, "img", "css", "js"));

		mountPage("login", getSignInPageClass());
		mountPage("home", getStudentTOCPageClass());
        mountPage("thome", getTeacherTOCPageClass());
        mountPage("ahome", getAdminHomePageClass());
        mountPage("reading", getStudentReadingPageClass());
        mountPage("treading", getTeacherReadingPageClass());
        mountPage("glossary", getGlossaryPageClass());
        mountPage("notebook", getNotebookPageClass());
        mountPage("tags", getTagsPageClass());
        mountPage("questions", getMyQuestionsPageClass());
        mountPage("questionp", getQuestionPopupPageClass());
        mountPage("collections", getResponseCollectionsPageClass());
        mountPage("whiteboard", getWhiteboardPageClass());
        mountPage("compare", getPeriodResponsePageClass());
        mountPage("tnotebook", getTeacherNotesPageClass());
        mountPage("manage", getManageClassesPageClass());
        mountPage("register", getRegisterPageClass());
        mountPage("reset", getForgotPasswordPageClass());
        mountPage("password", getPasswordPageClass());
        mountPage("authoredp", getAuthoredPopupPageClass());
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
		User user = CwmSession.get().getUser();
		if (user!=null && Role.TEACHER.equals(user.getRole()))
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
	public Class<? extends WebPage> getAuthoredPopupPageClass() {
		return org.cast.isi.page.AuthoredPopup.class;
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
        response.renderOnDomReadyJavaScript(SessionExpireWarningDialog.getResetJavascript());
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
		return (configuration.getProperty("isi.contentDir")).trim();
	}
		
	public String getSkinDir() {
		return (configuration.getProperty("isi.skinDir")).trim();
	}

	public String getCustomSkinDir() {
		String csd =  configuration.getProperty("isi.customSkinDir");
		if (csd != null) {
			return (configuration.getProperty("isi.customSkinDir")).trim();
		}
		return null;	
	}
	
	public String getTransformationDir() {
		String td =  configuration.getProperty("isi.transformationDir");
		if (td != null) {
			return (configuration.getProperty("isi.transformationDir")).trim();
		}
		return getSkinDir();
	}

	public String getGlossaryTransformationFile() {
		String tf =  configuration.getProperty("isi.xslGlossaryFile");
		if (tf != null) {
			return (configuration.getProperty("isi.xslGlossaryFile")).trim();
		}
		return "glossary.xsl";
	}

	public String getStudentTransformationFile() {
		String tf =  configuration.getProperty("isi.xslStudentFile");
		if (tf != null) {
			return (configuration.getProperty("isi.xslStudentFile")).trim();
		}
		return "student.xsl";
	}

	public String getTocTransformationFile() {
		String tf =  configuration.getProperty("isi.xslTocFile");
		if (tf != null) {
			return (configuration.getProperty("isi.xslTocFile")).trim();
		}
		return "toc.xsl";
	}

	public String getEmailTransformationFile() {
		String tf =  configuration.getProperty("isi.xslEmailFile");
		if (tf != null) {
			return (configuration.getProperty("isi.xslEmailFile")).trim();
		}
		return "email.xsl";
	}

	public String getCustomTransformationDir() {
		// if there isn't any custom directory then use the default directory
		String ctd =  configuration.getProperty("isi.customTransformationDir");
		if (ctd != null) {
			return (configuration.getProperty("isi.customTransformationDir")).trim();
		}
		return getTransformationDir();
	}
	
	public String getDavServer() {
		String davServer = configuration.getProperty("isi.davServer");
		if (davServer != null) {
			return (configuration.getProperty("isi.davServer")).trim();
		}
		return null;
	}
	
	/**
	 * Return the ISIXMLSection correspoding to a page number. 
	 * @param num the 1-based page number
	 * @return corresponding ISIXMLSection, or null if page number is out of range.
	 */
	public ISIXmlSection getPageNum(int num) {
		return (ISIXmlSection) studentContent.getByLabel(ISIXmlSection.SectionType.PAGE, num);
	}
	
	/**
	 * Return the number of pages of content.  Page numbers range from 0 to the given number - 1.
	 * @return
	 */
	public int getPageCount() {
		return studentContent.getLabelCount(ISIXmlSection.SectionType.PAGE);
	}

	
	/**
	 * Configure the default period
	 */
	public IModel<? extends Period> getMDefaultPeriod() {
		// Set the default Period
		String periodName =  configuration.getProperty("isi.defaultPeriod");
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
		ISIXmlSection section = getPageNum(1);
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
		Icon icon = makeIcon("icon", section.getClassName());
		icon.setmAltText(Model.of(section.getTitle()));
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
	public Icon makeIcon (String wicketId, String sectionClass) {
		return new Icon(wicketId, iconPathForSectionClassName(sectionClass));
	}
	
	/**
	 * What image to use as the icon for a section, based on the section's class attribute.
	 * TODO: assumes that the image exists; there should be some way to configure this more flexibly.
	 * @param sectionClassName
	 * @return
	 */
	protected String iconPathForSectionClassName (String sectionClassName) {
		if (!Strings.isEmpty(sectionClassName))
			return "img/icons/activity_" + sectionClassName + ".png";
		else
			return "img/icons/activity_reading.png";
	}
	
	public WebComponent teacherStatusIconFor(ISIXmlSection section, SectionStatus sectionStatus) {
		String imageName = "";
		IModel<String> mAlt;
		IModel<ISIXmlSection> sectionModel = new Model<ISIXmlSection>(section);
		
		// if there is no sectionStatus record then student hasn't finished this section
		if (sectionStatus == null) {
			imageName = "status_incomplete";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.incomplete", 
					"%s is incomplete", 
					sectionModel);
		} else if (sectionStatus.getUnreadStudentMessages() > 0) {
			imageName = "status_message";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.newfeedback", 
					"%s has new feedback from student", 
					sectionModel);
		} else if (!sectionStatus.getCompleted()) {
			imageName = "status_incomplete";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.incomplete", 
					"%s is incomplete", 
					sectionModel);
		} else if (sectionStatus.getReviewed()) {
			imageName = "status_reviewed";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.reviewed", 
					"%s has been reviewed", 
					sectionModel);
		} else {
			imageName = "status_ready";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.ready", 
					"%s is ready for review", 
					sectionModel);
		}
		return new Icon("icon", "img/icons/" + imageName + ".png", mAlt, mAlt);
	}
	
	public WebComponent studentStatusIconFor(ISIXmlSection section, SectionStatus sectionStatus) {
		String imageName = "";
		IModel<String> mAlt;
		IModel<ISIXmlSection> sectionModel = new Model<ISIXmlSection>(section);
		
		if ((sectionStatus == null) || (!sectionStatus.getCompleted())) {
			imageName = "status_incomplete";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.incomplete", 
					"%s is incomplete", 
					sectionModel);
		} else {
			imageName = "status_complete";
			mAlt = formatAltString(
					"isi.sectionStatusIconAlt.complete", 
					"%s is completed", 
					sectionModel);
		}
		return new Icon("icon", "img/icons/" + imageName + ".png", mAlt, mAlt);
	}
	
	private IModel<String> formatAltString(String propertyKey, String defaultFormat, IModel<ISIXmlSection> sectionModel) {
		return new StringResourceModel(
				propertyKey, 
				sectionModel, 
				String.format(
						defaultFormat, 
						sectionModel.getObject().getTitle())
				);
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
            getSession().bind();
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
	
	/**
	 * Returns the header and footer panel to be used on pages for this application.
	 * @return
	 */
	public HeaderPanel getHeaderPanel(String id, PageParameters parameters) {
//		the header panel expects the application to add buttons		
		if (Role.TEACHER.equals(CwmSession.get().getUser().getRole()))
			return new TeacherHeaderPanel(id, parameters);
		else
			return new DefaultHeaderPanel(id, parameters);
	}

	public FooterPanel getFooterPanel(String id, PageParameters parameters) {
		return new FooterPanel(id, parameters);
	}

	public AbstractNavBar<? extends XmlSection> getTopNavigation(String id, IModel<XmlSection> sec, boolean teacher) {
		if (alternateNavBar != null) {
			if (alternateNavBar.equals("alternate1")) {
				return new AlternateNavBar1(id, sec, teacher);
			}
		}
		return new DefaultNavBar(id, sec, teacher);
	}

	public AbstractNavBar<? extends XmlSection> getBottomNavigation (String id, IModel<XmlSection> mSection, boolean teacher) {
		return new PageNavPanel("pageNavPanelBottom", mSection);
	}
}
