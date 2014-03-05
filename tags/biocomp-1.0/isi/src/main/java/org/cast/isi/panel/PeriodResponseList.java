/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the UDL Curriculum Toolkit:
 * see <http://code.google.com/p/udl-curriculum-toolkit>.
 *
 * The UDL Curriculum Toolkit is free software: you can redistribute and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The UDL Curriculum Toolkit is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
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
