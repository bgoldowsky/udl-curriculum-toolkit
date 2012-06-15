package org.cast.isi.panel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class LockingResponseButtons extends ResponseButtons implements ISectionCompleteToggleListener {

	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;

	private IModel<User> mUser;

	public LockingResponseButtons(String id, IModel<Prompt> mPrompt,
			ResponseMetadata metadata, ContentLoc loc, IModel<User> mUser) {
		super(id, mPrompt, metadata, loc);
		this.mUser = mUser;
	}
	
	@Override
	public boolean isVisible() {
		return !isTeacher() && !isCompleteAndLocked();
	}

	public String getLocation() {
		ISIXmlSection sectionAncestor = loc.getSection().getSectionAncestor();
		return sectionAncestor.getContentLoc().getLocation();
	}

	private User getUser() {
		if (mUser == null)
			return null;
		return mUser.getObject();
	}

	private boolean isTeacher() {
		User user = getUser();
		return (user != null) && user.getRole().subsumes(Role.TEACHER);
	}

	private boolean isCompleteAndLocked() {
		ISIXmlSection section = loc.getSection();
		String location = getLocation();
		return section.isLockResponse() && isComplete(location, getUser());
	}
	
	private boolean isComplete(String location, User user) {
		return nullSafeBoolean(sectionService.sectionIsCompleted(user, location));
	}

	private boolean nullSafeBoolean(Boolean b) {
		return (b != null) && b;
	}
	
	@Override
	protected void onDetach() {
		if (mUser != null)
			mUser.detach();
		super.onDetach();
	}
}
