package org.cast.isi.component;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;
import org.cast.cwm.xml.XmlSection;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlSection;
import org.cast.isi.data.ContentLoc;
import org.cast.isi.data.SectionStatus;
import org.cast.isi.service.IFeatureService;
import org.cast.isi.service.ISectionService;

import com.google.inject.Inject;

public class SectionCompleteImageContainer extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	@Inject
	protected ISectionService sectionService;

	@Inject
	protected IFeatureService featureService;

	private IModel<User> targetUserModel;

	private ContentLoc sectionContentLocation;

	public SectionCompleteImageContainer(String id,
			IModel<XmlSection> model) {
		super(id, model);
		this.sectionContentLocation = getContentLoc(getSectionAncestor(getISIXmlSection(model)));
		this.targetUserModel = ISISession.get().getTargetUserModel();
	}

	@Override
	public void onBeforeRender() {
		addOrReplace(getImage());
		super.onBeforeRender();
	}

	protected Image getImage() {
		Image image;
		if (isComplete()) {
			image = new DoneImage("doneImg");
			image.setVisible(featureService.isTocSectionCompleteIconsOn());
		}
		else {
			image = new NotDoneImage("doneImg");
			image.setVisible(featureService.isTocSectionIncompleteIconsOn());
		}
		return image;
	}

	protected boolean isComplete() {
		SectionStatus status = sectionService.getSectionStatus(getUser(), sectionContentLocation);			
		if (status == null)
			return false;
		return status.getCompleted();
	}
	
	protected User getUser() {
		return targetUserModel.getObject();
	}

	private static ISIXmlSection getSectionAncestor(ISIXmlSection isiXmlSection) {
		return isiXmlSection.getSectionAncestor();
	}

	private static ISIXmlSection getISIXmlSection(IModel<XmlSection> model) {
		return (ISIXmlSection) model.getObject();
	}

	private static ContentLoc getContentLoc(ISIXmlSection xmlSection) {
		return xmlSection.getContentLoc();
	}


}
