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
package org.cast.isi.data.builder;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.builders.ResponseCriteriaBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

@Getter
@Setter
public class ISIResponseCriteriaBuilder extends ResponseCriteriaBuilder {

	private static final long serialVersionUID = 1L;
	
	private Boolean inNotebook;
	private Boolean orderByNotebookInsert;	
	private IModel<List<Site>> siteListModel = null;
	private Boolean canCache = true;

	@Override
	public void build(Criteria criteria) {
		if (inNotebook != null)
			criteria.add(Restrictions.eq("inNotebook", inNotebook.booleanValue()));
		if (orderByNotebookInsert.equals(Boolean.TRUE))
			criteria.addOrder(Order.desc("notebookInsertTime"));		
		super.build(criteria);
	}

	@Override
	public void buildUnordered(Criteria criteria) {
		// only get responses related to specific site(s)
		if (siteListModel != null && !siteListModel.getObject().isEmpty()) {
			criteria.createAlias("user", "user").createAlias("user.periods", "period", JoinType.LEFT_OUTER_JOIN);
			List<Site> siteList = siteListModel.getObject();
			Disjunction siteRestriction = Restrictions.disjunction();
			siteRestriction.add(Restrictions.in("period.site", siteList)); 
			criteria.add(siteRestriction);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);  // Remove duplicate rows as a result of the INNER JOIN
		}		
		super.buildUnordered(criteria);

		// may want to push this to cwm - no cache used for large report data, more than 1000 objects
		criteria.setCacheable(canCache);
	}

	public void detach() {
		if (siteListModel != null)
			siteListModel.detach();
	}
}