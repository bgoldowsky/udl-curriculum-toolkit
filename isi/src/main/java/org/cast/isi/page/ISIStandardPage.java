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
import lombok.Setter;
import net.databinder.auth.hib.AuthDataSession;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.DialogBorder;
import org.cast.cwm.data.component.SessionExpireWarningDialog;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.dialog.AbstractISIAjaxDialog;
import org.cast.isi.panel.TeacherSubHeaderPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for "main" (non-popup) pages of the ISI application.  
 * All non-popup pages should extend this page.
 * It includes the header and footer panels and any other common features
 * across these pages.
 * 
 * @author jacobbrookover
 *
 */
abstract public class ISIStandardPage extends ISIBasePage {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ISIStandardPage.class);

	@Inject
	static IXmlService xmlService;
	
	@Getter @Setter protected ContentLoc loc = null;
	
	public static final String DISPLAY_DIALOG_ID = "displayDialog";
	private static final String LOADING_DIALOG_ID = "loadingDialogBorder";
	private DialogBorder loadingDialog;

	public ISIStandardPage(final PageParameters parameters) {
		super(parameters);
		commonInit(parameters);
	}
	
	public void commonInit(PageParameters parameters) {
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		
		add(ISIApplication.get().getHeaderPanel("headerPanel", parameters).setOutputMarkupId(true));
		// If teacher, then add a sub header panel
		if (ISISession.get().getUser().getRole().subsumes(Role.TEACHER)) {
			add(new TeacherSubHeaderPanel("teacherSubHeader", parameters));
		} else {
			add(new WebMarkupContainer("teacherSubHeader").setVisible(false));			
		}

		add(ISIApplication.get().getFooterPanel("footerPanel", parameters));
		add(new ISISessionExpireWarningDialog("sessionWarning"));
		
		addToolbar("tht");
	}
	
	@Override
	protected void onInitialize() {

		WebMarkupContainer body = new WebMarkupContainer("body") {
			private static final long serialVersionUID = 1L;

            /* heikki TODO
			@Override
			public boolean isTransparentResolver() {
				return true;
			}
			*/
		};
		add (body);

		// Dialog Placeholder
		add(new WebMarkupContainer(DISPLAY_DIALOG_ID).setOutputMarkupId(true));
		
		// Loading Dialog
		loadingDialog = new DialogBorder(LOADING_DIALOG_ID, new Model<String>("Loading..."));
		add(loadingDialog);
		
		// Cancel Link on Loading Dialog
		WebMarkupContainer cancelLink = new WebMarkupContainer("cancelLink");
		cancelLink.add(loadingDialog.getClickToCloseBehavior());
		loadingDialog.getBodyContainer().add(cancelLink);
		super.onInitialize();
	}

	/**
	 * Adds the application's default toolbar (with features like text-to-speech, dictionary, etc).
	 * Pages can override this method to use a different (or no) toolbar.
	 */
	protected void addToolbar (String id) {
		add (ISIApplication.get().getToolbar(id, this));
	}
	
	/** 
	 * By default returns null.  Override to provide more detail.
	 * 
	 * @see org.cast.isi.page.ISIBasePage#getPageViewDetail()
	 */
	public String getPageViewDetail() {
		return null;
	}
	
	public Class<? extends WebPage>getHomePage() {
		return getISIApplication().getHomePage();
	}
	
	public void renderHead(final IHeaderResponse response) {
		renderThemeCSS(response, "css/main.css");
		super.renderHead(response);		
	}
	
	/** 
	 * Return Person who is logged in and viewing this page
	 * @return Person object
	 */
	public User getUser() {
		return (User) AuthDataSession.get().getUser();
	}
	
	public ISIApplication getISIApplication() {
		return (ISIApplication) getApplication();
	}	

	
	/**
	 * TODO: do we want to override behavior of this warning at all?
	 *
	 */
	protected class ISISessionExpireWarningDialog extends SessionExpireWarningDialog {

		private static final long serialVersionUID = 1L;
		
		public ISISessionExpireWarningDialog(String id) {
			super(id);
		}
		
	}

	public void reloadForPeriodStudentChange(final PageParameters parameters) {
		setResponsePage(getPage().getClass(), parameters);
	}			


	/**
	 * Display an AbstractStyledDialog on this page.
	 * 
	 * @param dialog
	 * @param target
	 */
	public void displayDialog(AbstractISIAjaxDialog<?> dialog, AjaxRequestTarget target) {
		replace(dialog);
		dialog.getDialogBorder().open(target);
		target.addComponent(dialog);
	}
	
	/**
	 * Reset the AbstractStyledDialog on this page.  Use this to drop the component
	 * @param target
	 */
	public void resetDialog(AjaxRequestTarget target) {
		WebMarkupContainer empty = new WebMarkupContainer(DISPLAY_DIALOG_ID);
		empty.setOutputMarkupId(true);
		replace(empty);
		target.addComponent(empty);
	}

	
	/**
	 * Add this behavior to any AjaxLink to display a "Loading..." dialog on the client
	 * side as soon as the link is clicked.  The Loading Dialog will be hidden when
	 * the ajax request is completed.
	 * 
	 * @return
	 */
	public IAjaxCallDecorator getLoadingDialogDecorator() {
		return getLoadingDialogDecorator(null);
	}
	
	/**
	 * Add this behavior to any AjaxLink to display a "Loading..." dialog on the client
	 * side as soon as the link is clicked.  The Loading Dialog will be hidden when
	 * the ajax request is completed.  
	 * 
	 * This method allows an optional dialog parameter that will be closed immediately 
	 * before the "Loading..." dialog is opened.  Use this on links in an existing dialog 
	 * to transition to another dialog.  However, this should not be used if the dialog
	 * might not close (e.g. form validation).
	 * 
	 * @param dialog existing dialog to be closed before loading is displayed.
	 * @return
	 */
	public IAjaxCallDecorator getLoadingDialogDecorator(final AbstractISIAjaxDialog<?> dialog) {
		return new AjaxCallDecorator() {

			private static final long serialVersionUID = 1L;

            @Override
            public CharSequence decorateScript(Component c, CharSequence script) {
                return (dialog == null ? "" : dialog.getDialogBorder().getCloseString(false)) + loadingDialog.getOpenString(dialog == null) + script;
            }

            @Override
            public CharSequence decorateOnSuccessScript(Component c, CharSequence script) {
                return loadingDialog.getCloseString(false) + script;
            }

            @Override
            public CharSequence decorateOnFailureScript(Component c, CharSequence script) {
                return loadingDialog.getCloseString(false) + script;
            }
        };
	}
	

}
