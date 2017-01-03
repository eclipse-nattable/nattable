/*****************************************************************************
 * Copyright (c) 2015, 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRegionCommand;

/**
 * Command to configure row heights. Instead of knowing the row indexes to
 * resize, it can be used to search for rows that have a special label applied.
 *
 * @since 1.4
 */
public class RowSizeConfigurationCommand extends AbstractRegionCommand {

    /**
     * The height to set. Can be <code>null</code> in combination with
     * {@link #percentageSizing} == <code>true</code> to enable percentage
     * sizing with equal row heights.
     */
    public final Integer newRowHeight;
    /**
     * Boolean flag to specify whether the height should be set as percentage or
     * as pixel value.
     */
    public final boolean percentageSizing;

    /**
     *
     * @param label
     *            The label that needs to be applied to a row in order to
     *            process the resize command. Is used to determine the row
     *            index. If <code>null</code> the default size will be set.
     * @param newRowHeight
     *            The height to set. Can be <code>null</code> in combination
     *            with {@link #percentageSizing} == <code>true</code> to enable
     *            percentage sizing with equal row heights.
     * @param percentageSizing
     *            Boolean flag to specify whether the height should be set as
     *            percentage or as pixel value.
     */
    public RowSizeConfigurationCommand(String label, Integer newRowHeight, boolean percentageSizing) {
        super(label);
        this.newRowHeight = newRowHeight;
        this.percentageSizing = percentageSizing;
    }

    @Override
    public AbstractRegionCommand cloneForRegion() {
        return new RowSizeConfigurationCommand(null, this.newRowHeight, this.percentageSizing);
    }
}
