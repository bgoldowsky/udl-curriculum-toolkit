package org.cast.isi.panel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class LockingResponseList extends ResponseList implements ISectionCompleteToggleListener {

	private static final long serialVersionUID = 1L;

	@Inject
	private ISectionService sectionService;

	public LockingResponseList(String wicketId, IModel<Prompt> mPrompt,
			ResponseMetadata metadata, ContentLoc loc, IModel<User> mUser) {
		super(wicketId, mPrompt, metadata, loc, mUser);
	}
	
	@Override
	public void onBeforeRender() {
		setAllowEdit(!isTeacher() && !isCompleteAndLocked());
		super.onBeforeRender();
	}

	public String getLocation() {
		return loc.getLocation();
	}

	private User getUser() {
		if (mTargetUser == null)
			return null;
		return mTargetUser.getObject();
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
	

}
