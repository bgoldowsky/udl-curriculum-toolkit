package org.cast.isi;

import org.apache.wicket.markup.html.SecurePackageResourceGuard;

public class ISIPackageResourceGuard extends SecurePackageResourceGuard {

		public ISIPackageResourceGuard() {
			super(new SimpleCache(100));
			addPattern("+*.pdf");
			addPattern("+**/mediaplayer/skins/**/*.xml");
		}

}
