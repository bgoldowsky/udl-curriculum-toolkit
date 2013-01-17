package org.cast.isi.component;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.cast.cwm.service.IUserPreferenceService;
import org.cast.isi.CollapseBoxBehavior;
import org.cast.isi.ISISession;

import com.google.inject.Inject;

/**
 * Event logging and state saving behavior for a collapse box.  See @CollapseBoxBehavior@ 
 * for more details.
 * 
 * @author lynnmccormack
 *
 */
public final class StateSavingCollapseBoxBehavior extends CollapseBoxBehavior {
	
	private static final long serialVersionUID = 1L;

	private final String userPreferenceName;
	
	@Inject
	IUserPreferenceService userPreferenceService;

	/**
	 * @param event - the type of ajax event to track (e.g., onclick)
	 * @param type - event type
	 * @param pageName - usually supplied by the page - can be null
	 * @param userPreferenceName - unique name for a user preference
	 */
	public StateSavingCollapseBoxBehavior(String event, String type, String pageName, String userPreferenceName) {
		super(event, type, pageName);
		this.userPreferenceName = userPreferenceName;
	}

	@Override
	protected void onEvent(AjaxRequestTarget target) {
		String action = RequestCycle.get().getRequest().getParameter("action");
		boolean toggleState;
		if (action.equals("open")) {
			toggleState = true;
		} else {
			toggleState = false;
		}
		userPreferenceService.setUserPreferenceBoolean(ISISession.get().getUserModel(), userPreferenceName, toggleState);
		super.onEvent(target);
	}
}