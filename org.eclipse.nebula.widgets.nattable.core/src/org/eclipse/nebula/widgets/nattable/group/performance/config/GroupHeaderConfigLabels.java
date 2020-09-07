/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
