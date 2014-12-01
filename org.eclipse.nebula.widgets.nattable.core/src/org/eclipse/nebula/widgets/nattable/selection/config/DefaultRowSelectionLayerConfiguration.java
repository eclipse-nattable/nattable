/*******************************************************************************
 * Copyright (c) 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.config;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Default configuration for row only based selections. Used by the
 * {@link SelectionLayer}.
 */
public class DefaultRowSelectionLayerConfiguration extends DefaultSelectionLayerConfiguration {

    @Override
    protected void addSelectionUIBindings() {
        addConfiguration(new RowOnlySelectionBindings());
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addMoveSelectionConfig() {
        addConfiguration(new RowOnlySelectionConfiguration());
    }
}
