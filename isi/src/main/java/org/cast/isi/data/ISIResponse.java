/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cast.isi.data;

import java.util.Date;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.data.Response;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

// FIXME
@NamedQueries({
	@NamedQuery(
			name="ISIResponse.getAllWhiteboardResponses",
			query="select r from ISIResponse r where r.inWhiteboard=TRUE order by r.whiteboardInsertTime desc"),
	@NamedQuery(
			name="ISIResponse.getPeriodWhiteboardResponses",
			query="select r from ISIResponse r join r.user.periods per where per=:period and r.inWhiteboard=TRUE and r.valid=TRUE order by r.whiteboardInsertTime desc")
})

/**
 * In ISI applications, Responses can have some additional data attached to them -
 * a title and occasionally photo/file credits.
 * @author bgoldowsky
 *
 */
@Entity
@Getter
@Setter
public class ISIResponse extends Response {
	
	private static final long serialVersionUID = 1L;

	private String title;
	
	private boolean inNotebook = false;
	
	private boolean inWhiteboard = false;
	
	private Date notebookInsertTime;
	
	private Date whiteboardInsertTime;

}
