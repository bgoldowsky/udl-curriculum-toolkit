package org.cast.isi;

import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeActions;

/**
 * Override @AnnotationsRoleAuthorizationStrategy to fix bug WICKET-3974.
 * See: https://issues.apache.org/jira/browse/WICKET-3974
 * 
 * TODO: this should no longer be needed after update to Wicket 6.x.
 * 
 */
public class ISIAnnotationsRoleAuthorizationStrategy extends AnnotationsRoleAuthorizationStrategy {

	public ISIAnnotationsRoleAuthorizationStrategy(IRoleCheckingStrategy roleCheckingStrategy) {
		super(roleCheckingStrategy);
	}

	// Copied from superclass
	protected boolean isActionAuthorized(final Class<?> componentClass, final Action action)
	{
		// Check for a single action
		if (!check(action, componentClass.getAnnotation(AuthorizeAction.class)))
		{
			return false;
		}

		// Check for multiple actions
		final AuthorizeActions authorizeActionsAnnotation = componentClass.getAnnotation(AuthorizeActions.class);
		if (authorizeActionsAnnotation != null)
		{
			for (final AuthorizeAction authorizeActionAnnotation : authorizeActionsAnnotation.actions())
			{
				if (!check(action, authorizeActionAnnotation))
				{
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * @param action
	 *            The action to check
	 * @param authorizeActionAnnotation
	 *            The annotations information
	 * @return False if the action is not authorized
	 */
	private boolean check(final Action action, final AuthorizeAction authorizeActionAnnotation)
	{
		if (authorizeActionAnnotation != null)
		{
			if (action.getName().equals(authorizeActionAnnotation.action()))
			{
				// The following 3 lines are the fix
				Roles deniedRoles = new Roles(authorizeActionAnnotation.deny());
				deniedRoles.remove(""); // If deny annotation is not present, deny() will have incorrectly returned "".
 	 	 	 	if (isEmpty(deniedRoles) == false && hasAny(deniedRoles))
				{
					return false;
				}

				Roles roles = new Roles(authorizeActionAnnotation.roles());
				if (!(isEmpty(roles) || hasAny(roles)))
				{
					return false;
				}
			}
		}
		return true;
	}

}
