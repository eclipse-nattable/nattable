/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.command;

/**
 * Abstract command that can be used to send a command into a specific region of
 * a composition like a grid.
 *
 * @since 1.5
 */
public abstract class AbstractRegionCommand extends AbstractContextFreeCommand {

    /**
     * The label that needs to be applied in order to process the command. If
     * the label matches a region, the command will be only processed down the
     * layer stack of the corresponding region. If the label is
     * <code>null</code> or a cell label, the command will be processed by all
     * or the first layer in the composition that has a handler configured.
     */
    public final String label;

    /**
     *
     * @param label
     *            The label that needs to be applied to a row in order to
     *            process the resize command. Is used to determine the row
     *            index. If <code>null</code> the default size will be set.
     */
    public AbstractRegionCommand(String label) {
        this.label = label;
    }

    /**
     * Creates and returns a clone of this instance with necessary modifications
     * for further processing. Typically the label information is removed or
     * adjusted for processing further down the region layer stack.
     *
     * @return A clone of this command prepared for further processing down the
     *         region layer stack.
     */
    public abstract AbstractRegionCommand cloneForRegion();
}
