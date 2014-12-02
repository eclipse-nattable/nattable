/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.action;

/**
 * Specialisation of IMouseAction for specifying an action that is executed on
 * mouse click. It simply adds the attribute for exclusiveness, which allows to
 * configure if the action should be executed or not if there is a single click
 * and a double click action configured.
 */
public interface IMouseClickAction extends IMouseAction {

    /**
     * Configuration of this IMouseClickAction to specify the behaviour when
     * this action is configured to be a single click action, and there is also
     * a double click action registered.
     * <p>
     * If this method returns <code>true</code>, this means either the single
     * <b>OR</b> the double click action is executed. Returning
     * <code>false</code> will execute the single click action immediately and
     * the double click action additionally.
     * <p>
     * <b>Note:</b> Being an exclusive action means that the double click action
     * will be executed and the single click action will be cancelled. This also
     * means that the single click action will not be performed until the double
     * click action time is waited.
     *
     * @return <code>true</code> if this action is exclusive, <code>false</code>
     *         if not.
     */
    boolean isExclusive();
}
