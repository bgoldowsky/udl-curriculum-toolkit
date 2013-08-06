package org.cast.isi.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A simple link that can be searched for (and disabled).  Also adds
 * a class attribute if disabled.
 */
public abstract class EditDisableLink<T> extends AjaxFallbackLink<T> {

	private static final long serialVersionUID = 1L;

	public EditDisableLink(String id) {
		super(id);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("class", "button" + (isEnabled() ? "" : " off"));
	}
	
	/**
	 * Get a visitor that will visit all {@link EditDisableLink} instances and enable or disable them.
	 * 
	 * @param target
	 * @param enable
	 * @return
	 */
	public static IVisitor<EditDisableLink<?>, Void> getVisitor(final AjaxRequestTarget target, final boolean enable) {

		return new IVisitor<EditDisableLink<?>, Void>() {

            public void component(EditDisableLink<?> link, IVisit<Void> visit) {
                link.setEnabled(enable);
                target.add(link);
                visit.dontGoDeeper();
            }
        };
	}
}