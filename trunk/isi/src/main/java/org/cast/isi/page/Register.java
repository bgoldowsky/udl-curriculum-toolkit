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

import java.util.Date;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import net.databinder.auth.components.RSAPasswordTextField;
import net.databinder.auth.valid.EqualPasswordConvertedInputValidator;
import net.databinder.components.hib.DataForm;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FeedbackBorder;
import org.cast.cwm.data.validator.UniqueUserFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator.Field;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.SiteService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.service.ISIEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page enables anonymous user creation.  User must provide some basic validated
 * data and they are then sent an email with a confirmation link.
 * 
 * @author lynnmccormack
 *
 */
public class Register extends ISIBasePage implements IHeaderContributor{
		
	private static final Logger log = LoggerFactory.getLogger(Register.class);
	
	boolean success = false; // has a successful registration already happened?
	protected String studentPassword;

	public Register(PageParameters params) {
		super(params);
		
		String pageTitleEnd = (new StringResourceModel("Register.pageTitle", this, null, "Register").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new Label("applicationTitle", new StringResourceModel("applicationTitle", this, null)));
		add(new Label("applicationSubTitle", new StringResourceModel("applicationSubTitle", this, null)));
		add(new WebMarkupContainer("preSubmitMessage") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return (!success);
			}			
		});
		
		// If user comes in with a registration key, confirm their account.
		if (params.containsKey("username") && params.containsKey("key")) {
			User user = UserService.get().getByUsername(params.getString("username")).getObject();
			if (user != null && params.getString("key").equals(user.getSecurityToken())) {
				UserService.get().confirmUser(user);
				ISISession.get().signIn(user, false);
				EventService eventService = EventService.get();
				eventService.createLoginSession(getRequest());
				eventService.saveEvent("user:created", null, null);
				eventService.saveLoginEvent();
				String completed = new StringResourceModel("Registration.completed", this, null,
						"Congratulations, you have successfullly created a new account.").getString();
				info(completed);
				success = true;
			} else {
				// Incorrect credentials or already confirmed: redirect to login page here.
				log.warn("Failed confirmation attempt: username={}, key={}", params.getString("username"), params.getString("key"));
				this.setRedirect(true);
				this.setResponsePage(Login.class);
				return;
			}
		}
		add (new FeedbackPanel("feedback") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() { return anyMessage(); }
		});
		add (new RegisterForm("registerForm"));

		add (new BookmarkablePageLink<Void>("forgot", ForgotPassword.class).setVisible(!success));
		add (new BookmarkablePageLink<Void>("login", Login.class));
		add(ISIApplication.get().getFooterPanel("pageFooter", params));
		
	}


	protected class RegisterForm extends DataForm<User> {
		private static final long serialVersionUID = 1L;

		private RSAPasswordTextField password;
		private RadioGroup<Role> radioGroup;
		
		protected RegisterForm(String id) {
			super(id, User.class);

			RSAPasswordTextField verifyPassword;
			TextField<String> email;
			TextField<String> verifyEmail;

			// this would be a custom portion
			radioGroup = new RadioGroup<Role>("userType", new Model<Role>(Role.STUDENT));
			add(radioGroup);
			radioGroup.add(new Radio<Role>("teacher", new Model<Role>(Role.TEACHER), radioGroup));
			radioGroup.add(new Radio<Role>("student", new Model<Role>(Role.STUDENT), radioGroup));
			radioGroup.setRequired(true);
			
			// this would be the standard portion of this
			add(new FeedbackBorder("usernameBorder")
				.add(new TextField<String>("username")
						.add(StringValidator.lengthBetween(6, 32))
						.add(new PatternValidator("[\\w-.]+"))
						.add(new UniqueUserFieldValidator(Field.USERNAME))
						.setRequired(true)));
			
			add(new FeedbackBorder("firstnameBorder")
				.add(new TextField<String>("firstName")
					.add(StringValidator.maximumLength(32))
					.setRequired(false)));
			
			add(new FeedbackBorder("lastnameBorder")
				.add(new TextField<String>("lastName")
					.add(StringValidator.maximumLength(32))
					.setRequired(false)));
			
			add(new FeedbackBorder("emailBorder")
				.add(email = (TextField<String>) new TextField<String>("email")
					.add(EmailAddressValidator.getInstance())
					.add(StringValidator.maximumLength(255))
					.add(new UniqueUserFieldValidator(Field.EMAIL))
					.setRequired(true)));
			
			add(new FeedbackBorder("verifyEmailBorder")
				.add(verifyEmail = (TextField<String>) new TextField<String>("verifyEmail", new Model<String>())
					.setRequired(true)));

			add(new FeedbackBorder("passwordBorder")
				.add(password = (RSAPasswordTextField) new RSAPasswordTextField("password", new Model<String>(), this)
					.add(StringValidator.lengthBetween(4, 32))
					.add(new PatternValidator("[\\w!@#$%^&*()-=_+\\\\.,;:/]+"))
					.setRequired(true)));

			add(new FeedbackBorder("verifyPasswordBorder")
				.add(verifyPassword = (RSAPasswordTextField) new RSAPasswordTextField("verifyPassword", new Model<String>(), this)
					.setRequired(true)));
			
			// Passwords and email addresses have to match
			add(new EqualPasswordConvertedInputValidator(password, verifyPassword));
			add(new EqualInputValidator(email, verifyEmail));
			
		}

		@Override
		public boolean isVisible() {
			return !success;
		}

		@Override
		protected void onBeforeSave(HibernateObjectModel<User> mUser) {
			User user = mUser.getObject();
			Role userRole = (Role) radioGroup.getDefaultModelObject();
			user.setRole(userRole);
			user.setCreateDate(new Date());
			user.setValid(false);
			user.generateSecurityToken();
			user.setPassword(password.getConvertedInput());
			String url = "/register?username=" + user.getUsername() + "&key=" + user.getSecurityToken();
			if (userRole.equals(Role.STUDENT)) {
				// add user to the default period
				user.getPeriods().clear();
				user.getPeriods().add(ISIApplication.get().getMDefaultPeriod().getObject());				
				ISIEmailService.get().sendXmlEmail(mUser, ISIEmailService.EMAIL_CONFIRM, url);
			} else {
				createDefaultTeacher(user);
				ISIEmailService.get().sendXmlEmail(mUser, ISIEmailService.EMAIL_CONFIRM_TEACHER, url, studentPassword);
			}
						
			String confirmation = new StringResourceModel("Registration.confirmation", this, null,
					"Thank you! In a few minutes you should get an email.  You will need to click on the link in " +
					"that email in order to confirm your account.").getString();
			info(confirmation);
			success = true;
		}


		// Consider moving this into a service class - LDM
		protected void createDefaultTeacher(User user) {
			
			SortedSet<User> userSet = new TreeSet<User>();
			userSet.add(user);
			
			// create a new site
			Site newSite = SiteService.get().newSite();
			newSite.setName("Site_" + user.getUsername()); // make this unique

			// create a new period
			Period newPeriod = SiteService.get().newPeriod();
			newPeriod.setSite(newSite);
			newPeriod.setName("Class_" + user.getUsername()); // make this unique
			
			// add the period to the user and the site
			SortedSet<Period> periodSet = new TreeSet<Period>();
			periodSet.add(newPeriod);
			user.getPeriods().add(newPeriod);
			newSite.getPeriods().add(newPeriod);			
			
			// a default student must be added to the new period
			User studentUser = UserService.get().newUser();
			studentUser.setRole(Role.STUDENT);
			studentUser.setFirstName("Student");
			studentUser.setLastName(newPeriod.getName());
			studentUser.setUsername(newPeriod.getName());
			studentUser.getPeriods().add(newPeriod);
			studentUser.setValid(true);
			studentUser.setCreateDate(new Date());
			
			// create a random number for the password
			Random randomNumber = new Random();
			Integer studentPasswordInteger = randomNumber.nextInt();
			studentPassword = studentPasswordInteger.toString().substring(0, 6);
			log.debug("this is the student password : {}", studentPassword);
			studentUser.setPassword(studentPassword);			
			
			// add the teacher and the default student to the new default class
			userSet.add(studentUser);
			newPeriod.setUsers(userSet);

			Databinder.getHibernateSession().save(newSite);
			Databinder.getHibernateSession().save(newPeriod);
			Databinder.getHibernateSession().save(studentUser);
		}

	
	}
	
	public void renderHead(final IHeaderResponse response) {
		response.renderCSSReference(new ResourceReference("/css/main.css"));
		super.renderHead(response);		
	}

	@Override
	public String getPageType() {
		return null;
	}

	@Override
	public String getPageName() {
		return "register";
	}

	@Override
	public String getPageViewDetail() {
		return null;
	}

}