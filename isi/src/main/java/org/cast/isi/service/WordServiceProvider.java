package org.cast.isi.service;

import org.cast.cwm.service.ICwmSessionService;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class WordServiceProvider implements Provider<IWordService> {

	@Inject
	protected ICwmSessionService cwmSessionService;

	public IWordService get() {
		if (cwmSessionService.getUser().isGuest()) {
			return new GuestWordService();
		}
		return new WordService();
	}

	
}
