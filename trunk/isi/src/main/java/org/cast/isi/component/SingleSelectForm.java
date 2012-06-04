package org.cast.isi.component;

import lombok.Getter;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.User;
import org.cast.isi.ISISession;
import org.cast.isi.service.IISIResponseService;

import com.google.inject.Inject;

public abstract class SingleSelectForm extends Form<Prompt> {

	private static final long serialVersionUID = 1L;

	@Getter
	protected IModel<Response> mResponse;
	
	protected IModel<User> mUser;
	protected IModel<User> mTargetUser;
	
	@Inject
	protected IISIResponseService responseService;

	public SingleSelectForm(String id) {
		super(id);
	}

	public SingleSelectForm(String id, IModel<Prompt> mcPrompt) {
		this(id, mcPrompt, ISISession.get().getUserModel(), ISISession.get().getTargetUserModel());
	}

	public SingleSelectForm(String id, IModel<Prompt> mcPrompt, IModel<User> userModel, IModel<User> targetUserModel) {
		super(id, mcPrompt);
		this.mUser = userModel;
		this.mTargetUser = targetUserModel;
		if (!mTargetUser.getObject().equals(mUser.getObject()))
			setEnabled(false);
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		mResponse = responseService.getResponseForPrompt(getModel(), mTargetUser);
	}

	/**
	 * Find the currently selected SingleSelectItem
	 * @return the selected item, or null if there is none.
	 */
	protected SingleSelectItem getSelectedItem() {
		return (SingleSelectItem) visitChildren(SingleSelectItem.class, new IVisitor<SingleSelectItem>() {
			public Object component(SingleSelectItem component) {
				if (component.isSelected()) {
					// Halt traversal by returning this component
					return component;
				} else {
					return CONTINUE_TRAVERSAL;
				}
			}
		});
		
	}
	
	@Override
	protected void onDetach() {
		if (mResponse != null)
			mResponse.detach();
		if (mUser != null) {
			mUser.detach();
		}
		if (mTargetUser != null) {
			mTargetUser.detach();
		}
		super.onDetach();
	}
	
	protected User getUser() {
		if (mUser == null)
			return null;
		return mUser.getObject();
	}	


}