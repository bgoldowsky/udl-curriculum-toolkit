package org.cast.isi.page;

import java.util.Date;

import net.databinder.auth.components.RSAPasswordTextField;
import net.databinder.auth.valid.EqualPasswordConvertedInputValidator;
import net.databinder.components.hib.DataForm;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
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
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FeedbackBorder;
import org.cast.cwm.data.validator.UniqueUserFieldValidator;
import org.cast.cwm.data.validator.UniqueUserFieldValidator.Field;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.service.ISIEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Register extends ISIBasePage implements IHeaderContributor{
	
	boolean success = false; // has a successful registration already happened?
	
	private static final Logger log = LoggerFactory.getLogger(Register.class);

	public Register(PageParameters params) {
		super(params);
		
		String pageTitleEnd = (new StringResourceModel("Register.pageTitle", this, null, "Register").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new Label("applicationTitle", new StringResourceModel("applicationTitle", this, null)));
		add(new Label("applicationSubTitle", new StringResourceModel("applicationSubTitle", this, null)));
		
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
						"Congratulations, you have successfullly created a new account. You may now begin working in the UDL Curriculum Toolkit and your work will be saved in your account.").getString();
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

			radioGroup = new RadioGroup<Role>("userType", new Model<Role>());
			add(radioGroup);
			radioGroup.add(new Radio<Role>("teacher", new Model<Role>(Role.TEACHER), radioGroup));
			radioGroup.add(new Radio<Role>("student", new Model<Role>(Role.STUDENT), radioGroup));
			radioGroup.setRequired(true);
			radioGroup.setLabel(Model.of("Type of User"));
			
			
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
			
			add (new BookmarkablePageLink<Void>("forgot", ForgotPassword.class));
			add (new BookmarkablePageLink<Void>("login", Login.class));
		}

		@Override
		public boolean isVisible() {
			return !success;
		}

		@Override
		protected void onBeforeSave(HibernateObjectModel<User> model) {
			User user = model.getObject();
			user.setRole((Role) radioGroup.getDefaultModelObject());
			user.setCreateDate(new Date());
			user.setValid(false);
			user.generateSecurityToken();
			user.setPassword(password.getConvertedInput());
			String url = "/register?username=" + user.getUsername() + "&key=" + user.getSecurityToken();
			ISIEmailService.get().sendXmlEmail(model, ISIEmailService.EMAIL_CONFIRM, url);

			// TODO: put user in the default class
			
			String confirmation = new StringResourceModel("Registration.confirmation", this, null,
					"Thank you! In a few minutes you should get an email.  You will need to click on the link in that email in order to confirm your account.").getString();
			info(confirmation);
			success = true;
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