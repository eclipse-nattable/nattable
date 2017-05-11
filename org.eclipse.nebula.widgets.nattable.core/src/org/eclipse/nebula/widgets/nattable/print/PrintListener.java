/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
     * This method gets executed after the print operation is finished and the layer
     * states are restored for UI rendering.
     */
    void printFinished();

}
