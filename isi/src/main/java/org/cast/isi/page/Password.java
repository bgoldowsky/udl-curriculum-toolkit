package org.cast.isi.page;

import net.databinder.auth.components.RSAPasswordTextField;
import net.databinder.auth.valid.EqualPasswordConvertedInputValidator;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.User;
import org.cast.cwm.data.component.FeedbackBorder;
import org.cast.cwm.data.validator.CorrectPasswordValidator;
import org.cast.cwm.service.EventService;
import org.cast.cwm.service.UserService;
import org.cast.isi.ISIApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Password extends ISIBasePage implements IHeaderContributor {

	private boolean haveKey = false;
	
	static final Logger log = LoggerFactory.getLogger(Password.class);

	public Password(PageParameters params) {
		super(params);

		String pageTitleEnd = (new StringResourceModel("Password.pageTitle", this, null, "Password").getString());
		setPageTitle(pageTitleEnd);
		add(new Label("pageTitle", new PropertyModel<String>(this, "pageTitle")));
		add(new Label("applicationTitle", new StringResourceModel("applicationTitle", this, null)));
		add(new Label("applicationSubTitle", new StringResourceModel("applicationSubTitle", this, null)));
		
		// User may come in with a secret key from the "Forgot Password" page.
		if (params.containsKey("username") && params.containsKey("key")) {
			User user = UserService.get().getByUsername(params.getString("username")).getObject();
			if (user != null && params.getString("key").equals(user.getSecurityToken())) {
				haveKey = true;
				add (new ChangePasswordForm ("form", user));
			} else {
				log.warn("Failed reset-password attempt: username={}, key={}", params.getString("username"), params.getString("key"));
				error("Incorrect URL");
				add (new ChangePasswordForm ("form", null));
			}
		} else {			
			add(new ChangePasswordForm("form", CwmSession.get().getUser()));
		}
		add(ISIApplication.get().getFooterPanel("pageFooter", params));
		add (new BookmarkablePageLink<Void>("register", Register.class));
		add (new BookmarkablePageLink<Void>("login", Login.class));			
	}
	

	protected class ChangePasswordForm extends Form<User> {
		private PasswordTextField password;
		private PasswordTextField verifyPassword;
		private WebMarkupContainer fields;

		private static final long serialVersionUID = 1L;

		protected ChangePasswordForm(String id, User user) {
			super(id, new HibernateObjectModel<User>(user));
			add(new FeedbackPanel("feedback") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() { return anyMessage(); }
			});
			
			add (fields = new WebMarkupContainer("fields"));
			if (user == null)
				fields.setVisible(false);
			
			fields.add (new WebMarkupContainer("oldPassContainer")
				.add((new FeedbackBorder("oldPassBorder"))
						.add(new RSAPasswordTextField("oldPass", new Model<String>(), this)
							.setLabel(new Model<String>("Old Password"))
							.add (new CorrectPasswordValidator())))
				.setVisible(!haveKey));
			
			fields.add((new FeedbackBorder("passwordBorder"))
				.add(password = (PasswordTextField) new PasswordTextField("password", new Model<String>())
					.setLabel(new Model<String>("New Password"))
					.add(StringValidator.lengthBetween(4, 32))
					.add(new PatternValidator("[\\w!@#$%^&*()-=_+\\\\.,;:/]+"))
					.setRequired(true)));

			fields.add((new FeedbackBorder("verifyPasswordBorder"))
				.add(verifyPassword = (PasswordTextField) new PasswordTextField("verifyPassword", new Model<String>())
					.setLabel(new Model<String>("Verify New Password"))
					.setRequired(true)));
			
			add(new EqualPasswordConvertedInputValidator(password, verifyPassword));
		}

		@Override
		protected void onSubmit()	{
			User user = getModelObject();
			// TODO:  commit this change in a better way
			Databinder.getHibernateSession().beginTransaction();
			user.setPassword(password.getModelObject());
			user.setSecurityToken(null);
			Databinder.getHibernateSession().getTransaction().commit();
			info("Password changed.");
			fields.setVisible(false); // don't show form when confirming success
			if (CwmSession.get().isSignedIn())
				EventService.get().saveEvent("user:change password", null, null); // TODO - do we care about this? If so, make it work for non-logged-in case.
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
		return "Password";
	}

	@Override
	public String getPageViewDetail() {
		return null;
	}
}