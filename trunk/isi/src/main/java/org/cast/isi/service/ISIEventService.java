package org.cast.isi.service;

import org.cast.cwm.data.Event;
import org.cast.cwm.service.EventService;
import org.cast.isi.data.ISIEvent;

public class ISIEventService extends EventService {

	@Override
	public Event newEvent() {
		return new ISIEvent();
	}

}
