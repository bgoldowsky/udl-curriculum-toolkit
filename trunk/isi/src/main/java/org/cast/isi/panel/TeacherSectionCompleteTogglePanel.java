package org.cast.isi.panel;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.data.SectionStatus;


public class TeacherSectionCompleteTogglePanel extends SectionCompleteTogglePanel {

	private static final long serialVersionUID = 1L;

	public TeacherSectionCompleteTogglePanel(String id,
			XmlSectionModel mSection, IModel<User> mTargetUser) {
		super(id, mSection, mTargetUser);
	}

	@Override
	protected void addSectionToggleButton() {
		add(new TeacherSectionCompleteToggleTextLink("toggleComplete", mSection, mTargetUser));
	}

	@Override
	protected String getInstructions() {
		if (isReviewed())
			return getStringResource("isi.sectionToggleLinks.instructions.markNotReviewed", "Mark Section Not Reviewed");
		else if (isComplete())
			return getStringResource("isi.sectionToggleLinks.instructions.markReviewed", "Mark Section Reviewed");
		else 
			return getStringResource("isi.sectionToggleLinks.instructions.incomplete", "Incomplete Student Work");
	}

	protected boolean isReviewed() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getReviewed();
	}

	@Override
	protected boolean showCompletedImage() {
		return isReviewed();
	}

}
