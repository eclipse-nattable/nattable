/*****************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.examples.e4;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

@SuppressWarnings("restriction")
public class LifecycleManager {

    public static final String CLOSE_ON_SHUTDOWN_TAG = "CloseOnShutdown";

    @PreSave
    void preSave(EModelService modelService, MApplication app) {
        List<String> tags = new ArrayList<>();
        tags.add(CLOSE_ON_SHUTDOWN_TAG);
        List<MPart> elementsWithTags = modelService.findElements(app, null,
                MPart.class, tags);

        for (MPart part : elementsWithTags) {
            try {
                part.setToBeRendered(false);
                part.setVisible(false);
                MElementContainer<MUIElement> parent = part.getParent();
                parent.getChildren().remove(part);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
