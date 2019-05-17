/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.config;

/**
 * Config labels for the group header layer.
 *
 * @since 1.6
 */
public final class GroupHeaderConfigLabels {

    private GroupHeaderConfigLabels() {
        // empty private constructor for constants class
    }

    /**
     * Label that is applied to collapsed groups in column/row header layer.
     */
    public static final String GROUP_COLLAPSED_CONFIG_TYPE = "GROUP_COLLAPSED"; //$NON-NLS-1$

    /**
     * Label that is applied to expanded groups in column/row header layer.
     */
    public static final String GROUP_EXPANDED_CONFIG_TYPE = "GROUP_EXPANDED"; //$NON-NLS-1$

}
