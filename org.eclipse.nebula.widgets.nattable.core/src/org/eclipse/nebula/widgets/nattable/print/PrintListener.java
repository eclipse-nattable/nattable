/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.print;

/**
 * Listener interface that is used to listen to print events like print started
 * and print finished.
 *
 * @since 1.6
 */
public interface PrintListener {

    /**
     * This method gets executed before the print operation is started.
     */
    void printStarted();

    /**
     * This method gets executed after the print operation is finished and the
     * layer states are restored for UI rendering.
     */
    void printFinished();

}
