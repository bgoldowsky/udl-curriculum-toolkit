package org.cast.isi.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.isi.dialog.VideoDialog;
import org.cast.isi.page.ISIStandardPage;

/**
 * An AjaxLink which when clicked creates a {@link VideoDialog} Video in a popup modal
 */
public class VideoLink extends AjaxLink<Void> {
	private static final long serialVersionUID = 1L;
	
	private String videoId;	
	private XmlSectionModel mSection;

	public VideoLink(String id, String _videoId, XmlSectionModel _mSection) {
		super(id);
		this.videoId = _videoId;
		this.mSection = _mSection;
	}

	@Override
	public void onClick(AjaxRequestTarget target) {		
		VideoDialog videoDialog = new VideoDialog(videoId, mSection);		
		ISIStandardPage page = (ISIStandardPage) getPage();
		page.displayDialog(videoDialog, target);
	}
}