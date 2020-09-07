/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence.command;

/**
 * Event that will be fired if the NatTable states/view configurations are
 * changed using the PersistenceDialog.
 *
 * <p>
 * Will only contain the name of the view configuration (might also be known as
 * state prefix) and the type of change made to it. It will not carry the
 * Properties itself.
 *
 * @see IStateChangedListener
 */
public class StateChangeEvent {

    /**
     * Enum for the type of change that was done to a NatTable view
     * configuration.
     */
    public enum StateChangeType {
        /**
         * A view configuration has changed. This normally occurs if an existing
         * view configuration gets overridden.
         */
        CHANGE,
        /**
         * A view configuration was created by saving a current NatTable state
         * via the PersistenceDialog.
         */
        CREATE,
        /**
         * A view configuration was deleted by using the PersistenceDialog.
         */
        DELETE
    }

    /**
     * The type of change that was done to a view configuration.
     */
    private final StateChangeType type;

    /**
     * The name of the view configuration (or prefix) that has been changed.
     */
    private final String viewConfigName;

    /**
     * Create a new {@link StateChangeEvent} for the given view configuration
     * name and type of change.
     *
     * @param viewConfigName
     *            The name of the view configuration (or prefix) that has been
     *            changed.
     * @param type
     *            The type of change that was done to a view configuration.
     */
    public StateChangeEvent(String viewConfigName, StateChangeType type) {
        this.viewConfigName = viewConfigName;
        this.type = type;
    }

    /**
     * @return The type of change that was done to a view configuration.
     */
    public StateChangeType getType() {
        return this.type;
    }

    /**
     * @return The name of the view configuration (or prefix) that has been
     *         changed.
     */
    public String getViewConfigName() {
        return this.viewConfigName;
    }

}
