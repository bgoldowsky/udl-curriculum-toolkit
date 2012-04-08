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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.glossary.BaseGlossaryPanel;
import org.cast.cwm.glossary.Glossary;
import org.cast.cwm.glossary.IGlossaryEntry;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.XmlSectionModel;
import org.cast.cwm.xml.component.XmlComponent;
import org.cast.isi.ISIApplication;
import org.cast.isi.ISISession;
import org.cast.isi.ISIXmlComponent;
import org.cast.isi.data.WordCard;
import org.cast.isi.service.WordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlossaryPanel extends BaseGlossaryPanel {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(GlossaryPanel.class);
	protected boolean readOnly;
	protected Map<Character,List<IModel<WordCard>>> wordCardMap = null;
	protected IModel<List<WordCard>> wordCardList = null;

	public GlossaryPanel(String id, IModel<? extends IGlossaryEntry> entry) {
		super(id, entry);
			
		boolean isTeacher = ISISession.get().getUser().getRole().subsumes(Role.TEACHER);
		if (isTeacher)
			readOnly = true;
		// don't allow teachers to add new word cards
		add(new WebMarkupContainer("newWordButton").setVisible(!isTeacher));
	}

	@Override
	protected Glossary getGlossary() {		  
		return ISIApplication.get().getGlossary();
	}

	@Override
	public List<IModel<? extends IGlossaryEntry>> getGlossaryEntriesForLetter(Character c) {
		// First time, fetch word card list and sort into a map
		synchronized(this) {
			if (wordCardMap == null) {
				wordCardMap = new HashMap<Character, List<IModel<WordCard>>>();
				wordCardList = WordService.get().listWordCards(getMUser().getObject());

				for (WordCard card : wordCardList.getObject()) {
					if (card.isCurrent()) {
						Character initial = Character.toLowerCase(card.getHeadword().charAt(0));
						if (!Character.isLowerCase(initial)) {
							initial = '#'; // not a regular letter
						}
						if (!wordCardMap.containsKey(initial))
							wordCardMap.put(initial, new LinkedList<IModel<WordCard>>());
						wordCardMap.get(initial).add(new HibernateObjectModel<WordCard>(card));
					}
				}
			}
		}

		// Now read map of word cards and interleave with regular glossary words.
		List<IModel<? extends IGlossaryEntry>> list = new ArrayList<IModel<? extends IGlossaryEntry>>();
		list.addAll(super.getGlossaryEntriesForLetter(c));
		if (wordCardMap.containsKey(c)) {
			list.addAll(wordCardMap.get(c));
		}
		
		Collections.sort(list, new Comparator<IModel<? extends IGlossaryEntry>>() {
				public int compare(IModel<? extends IGlossaryEntry> e1, IModel<? extends IGlossaryEntry> e2) {
					return (e1.getObject().getSortForm().compareToIgnoreCase(e2.getObject().getSortForm()));
				}
			});

		return list;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Class<WebPage> getGlossaryPageClass() {
		return 	(Class<WebPage>) ISIApplication.get().getGlossaryPageClass();

	}			

	@Override
	protected XmlComponent newXmlComponent(String id, IModel<? extends IXmlPointer> model) {
		ISIXmlComponent isiXmlComponent = new ISIXmlComponent(id, (XmlSectionModel)model, "glossary");
		isiXmlComponent.setInGlossary(true);
		return isiXmlComponent;
	}

	
	@Override
	protected Component newDefinitionContainer(IGlossaryEntry entry) {

		WebMarkupContainer wmc = new WebMarkupContainer("glossword");

		if (entry != null) { 
			IModel<? extends IXmlPointer> xmlSection = entry.getXmlPointer();
			wmc.add(new Label("name", entry.getHeadword()));
			
			if (entry instanceof WordCard) {
				wmc.add(new WordCardPanel("definition", new HibernateObjectModel<WordCard>((WordCard)entry)));

			} else {
				if (xmlSection.getObject() != null) { 
					// this is a glossary word
					wmc.add(newXmlComponent("definition", xmlSection));
				} else { 
					log.error("Could not find glossary item for {}", entry);
					wmc.add(new Label("definition", "Could not find glossary entry for " + entry.getHeadword()));
				}
			}
		} else {  
			// there is no word
			wmc.add(new EmptyPanel("name"));
			wmc.add(new EmptyPanel("definition"));
		}
		return wmc;
	}

	protected IModel<User> getMUser() {
		return ISISession.get().getTargetUserModel();
	}
	
}