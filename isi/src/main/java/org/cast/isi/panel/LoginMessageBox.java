package org.cast.isi.panel;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.cast.isi.ISIApplication;
import org.cast.isi.page.Login;

public class LoginMessageBox extends Panel {

	private static final long serialVersionUID = 1L;

	public LoginMessageBox(String id) {
		super(id);
		add(new BookmarkablePageLink<Login>("login", ISIApplication.get().getSignInPageClass()));
	}

}
