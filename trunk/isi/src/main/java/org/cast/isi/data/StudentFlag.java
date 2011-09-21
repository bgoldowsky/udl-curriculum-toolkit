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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.cast.cwm.data.Period;
import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

@NamedQueries({
	@NamedQuery(
			name="StudentFlag.queryByPeriodAndFlaggerAndFlagee",
			query="select f from StudentFlag f where f.period=:period and f.flagger=:flagger and f.flagee=:flagee"),
	@NamedQuery(
			name="StudentFlag.queryByFlaggerAndFlagee",
			query="select f from StudentFlag f where f.flagger=:flagger and f.flagee=:flagee"),
	@NamedQuery(
			name="StudentFlag.queryByFlagger",
			query="select f from StudentFlag f where f.flagger=:flagger")
})

@Entity
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class StudentFlag extends PersistedObject {

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	private long id;

	@ManyToOne(optional=false)
	private User flagger;

	@ManyToOne(optional=false)
	private User flagee;
	
	private Date timestamp;
	
	@ManyToOne(optional=true)
	private Period period;
		
	public StudentFlag() {
		
	}
	
	@Override
	public Long getId() {
		return id;
	}

	public User getFlagger() {
		return flagger;
	}

	public User getFlagee() {
		return flagee;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Period getPeriod() {
		return period;
	}

	public void setFlagger(User flagger) {
		this.flagger = flagger;
	}

	public void setFlagee(User flagee) {
		this.flagee = flagee;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

}
