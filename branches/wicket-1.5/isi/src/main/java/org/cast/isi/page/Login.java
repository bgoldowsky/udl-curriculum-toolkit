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

import net.databinder.auth.AuthApplication;
import net.databinder.auth.hib.AuthDataSession;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Application;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.cast.cwm.components.ShyLabel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.mediaplayer.MediaPlayerPanel;
import org.cast.cwm.service.IEventService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class Login extends ISIBasePage implements IHeaderContributor {
	private static final long serialVersionUID = 1L;

	static final Logger log = LoggerFactory.getLogger(Login.class);

	@Inject
	private IEventService eventService;

	@SuppressWarnings("unchecked")
	public Login(PageParameters params) {
		super(params);

		pageTitle = (new StringResourceModel("Login.pageTitle", this, null, "Login").getString());
		setPageTitle(pageTitle);
		add(new Label("pageTitle", pageTitle));
		add(new ShyLabel("loginMessage", new ResourceModel("Login.message", "")));
		add (new BookmarkablePageLink<Void>("home", ISIApplication.get().getHomePage()).setVisible(ISIApplication.get().isGuestAccessAllowed()));
		
		addApplicationTitles();

		add (new BookmarkablePageLink<Void>("forgot", ISIApplication.get().getForgotPasswordPageClass()).setVisible(ISIApplication.get().isEmailOn()));
		add (new BookmarkablePageLink<Void>("register", ISIApplication.get().getRegisterPageClass()).setVisible(ISIApplication.get().isSelfRegisterOn()));
		
		AuthApplication<User> app = null;
		try { app = ((AuthApplication<User>)Application.get()); } catch (ClassCastException e) { }
		if (app == null || !app.getSignInPageClass().isInstance(this))
			throw new UnauthorizedInstantiationException(Login.class);
		if (params != null) {
			String username = params.get("username").toString();
			String token = params.get("token").toString();

			if (username != null && token != null) {
				User user = app.getUser(username);
				if (user != null && app.getToken(user).equals(token)) {
					AuthDataSession.get().signIn(user, true);
				}
				setResponsePage(((Application)app).getHomePage());
				getSession().bind();
				return;
			}
		}
		add(new SignInForm("form"));
		add(ISIApplication.get().getFooterPanel("pageFooter", params));
	}

	protected class SignInForm extends Form<User> {
		private static final long serialVersionUID = 1L;
		private RequiredTextField<String> username;
		private PasswordTextField password;

		protected SignInForm(String id) {
			super(id);
			add(new FeedbackPanel("login-feedback"));
			add((new FormComponentFeedbackBorder("username-border")).add(username = new RequiredTextField<String>("username", new Model<String>())));
			add((new FormComponentFeedbackBorder("password-border")).add(password = new PasswordTextField("password", new Model<String>())));
			password.setRequired(true);
			FormComponentLabel usernameLabel =  (new FormComponentLabel("usernameLabel", username));
			add(usernameLabel);
			FormComponentLabel passwordLabel =  (new FormComponentLabel("passwordLabel", password));
			add(passwordLabel);
		}

		@Override
		protected void onSubmit()	{
			log.info("Login::onSubmit");
			ISISession session = ISISession.get();
			if(session.signIn((String)username.getModelObject(), (String)password.getModelObject())){

				eventService.createLoginSession(getRequest());
				eventService.saveLoginEvent();
				
				User user = session.getUser();
				if (!user.isValid()) {
					String notConfirmed = new StringResourceModel("Login.notConfirmed", this, null, "Account not confirmed.").getString();
					error(notConfirmed);
					return;
				}

				// Set default Period
				if (user != null && user.usesPeriods()) {
					session.setCurrentPeriodModel(new HibernateObjectModel<Period>(user.getPeriods().iterator().next()));
					Site currentSite = ISISession.get().getCurrentPeriodModel().getObject().getSite();
					IModel<Site> mCurrentSite = new Model<Site>(currentSite);
					session.setCurrentSiteModel(mCurrentSite);
				}				

				if (!continueToOriginalDestination()) {
					setResponsePage(getApplication().getHomePage());
					session.bind();
				}
				
			} else {
				log.warn("Login failed, user {}, password {}", username.getModelObject(), password.getModelObject());
				String loginFailed = new StringResourceModel("Login.signInFailed", this, null, "Invalid username and/or password.").getString();
				error(loginFailed);
			}
		}


	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		renderThemeCSS(response, "css/login.css");
		renderThemeCSS(response, "css/main.css");
		// setup jwplayer to enable videos to be run on the login page
		response.renderJavaScriptReference(new JavaScriptResourceReference(MediaPlayerPanel.class, "jwplayer.js"));
		super.renderHead(response);
	}

	public  String getPageType() {
		return "Login";
	}

	public String getPageName() {
		return null;
	}

	public String getPageViewDetail() {
		return null;
	}

}