package org.cast.isi.panel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.data.SectionStatus;

public class StudentSectionCompleteTogglePanel extends SectionCompleteTogglePanel {

	private static final long serialVersionUID = 1L;

	public StudentSectionCompleteTogglePanel(String id,
			XmlSectionModel mSection, IModel<User> mTargetUser) {
		super(id, mSection, mTargetUser);
	}

	@Override
	protected void addSectionToggleButton() {
		add(new StudentSectionCompleteToggleTextLink("toggleComplete", mSection, mTargetUser));
	}

	@Override
	protected String getInstructions() {
		if (isLocked())
			return getStringResource("isi.sectionToggleLinks.instructions.locked","");
		else if (isComplete())
			return getStringResource("isi.sectionToggleLinks.instructions.markIncomplete","");
		else 
			return getStringResource("isi.sectionToggleLinks.instructions.markComplete","");
	}

	protected boolean isLocked() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getLocked();
	}

	@Override
	protected boolean showCompletedImage() {
		return isComplete();
	}
	
}
