package org.cast.isi.panel;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseMetadata;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.data.ContentLoc;

public class PeriodResponseList extends ResponseList {

	private static final long serialVersionUID = 1L;
	
	private IModel<Period> mPeriod;

	public PeriodResponseList(String wicketId, IModel<Prompt> mPrompt, ResponseMetadata metadata, ContentLoc loc, IModel<Period> mPeriod) {
		super(wicketId, mPrompt, metadata, loc, null);
		this.mPeriod = mPeriod;
	}

	@Override
	protected ISortableDataProvider<Response> getResponseProvider() {
		 ISortableDataProvider<Response> provider = responseService.getResponseProviderForPromptAndPeriod(promptModel, mPeriod);
		// response list sort order is set by application configuration
		ISIApplication app = ISIApplication.get();
		provider.getSortState().setPropertySortOrder(app.getResponseSortField(), app.getResponseSortState());
		return provider;
	}

	@Override
	protected EditableResponseViewer getEditableResponseViewer(String wicketId, IModel<Response> mResponse, ResponseMetadata metadata, ContentLoc loc) {
		EditableResponseViewer viewer = super.getEditableResponseViewer(wicketId, mResponse, metadata, loc);
		viewer.setShowAuthor(true);
		boolean isMine = mResponse.getObject().getUser().equals(ISISession.get().getUser());
		if (!isMine) {
			viewer.setAllowEdit(false);
			viewer.setAllowNotebook(false);
			viewer.setAllowWhiteboard(false);
		}
		return viewer;
	}

}
