package org.cast.isi.component;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class SlideShowComponent extends WebMarkupContainer implements IHeaderContributor{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SlideShowComponent.class);
	
	protected String slideShowId;
	protected Element elt;

	public SlideShowComponent(String id, Element elt) {
		super(id);
		this.slideShowId = elt.getAttribute("id");
		log.debug("ADDING THE SLIDESHOW WITH ID = {}", slideShowId);
	}

	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(new ResourceReference("/js/jquery/jquery.seqSlideshow.js"));
		// The js call that will set up the annotated image.  The hotSpotString contains all the information
		// needed for the annotated image.
		response.renderOnDomReadyJavascript("{$(\"#" + slideShowId + "\").seqSlideshow();}");
		log.debug("RUNNING THE JS NEEDED FOR THE SLIDESHOW ID = {}", slideShowId);
	}

}
