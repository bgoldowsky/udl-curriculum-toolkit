/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.isi.data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.data.Event;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;

/** ISI application's generic Event type.
 * Adds a "target" field to the basic Event, to record which student's data a teacher was looking at.
 */
@Entity
@Getter
@Setter
public class ISIEvent extends Event {
	
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	private User target;
	
	public ISIEvent() {
		super();
	}
	
	@Override
	public void setDefaultValues() {
		super.setDefaultValues();
		User targetStudent = ISISession.get().getStudent();
		if (targetStudent != null)
			target = targetStudent;
	}
}
