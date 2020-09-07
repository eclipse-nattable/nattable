/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

/**
 * This class helps us test the event flow by providing access to the
 * ILayerEvent fired.
 */
public class LayerListenerFixture implements ILayerListener {

    // Received events are kept in order
    private final List<ILayerEvent> receivedEvents = new LinkedList<ILayerEvent>();

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        this.receivedEvents.add(event);
    }

    public List<ILayerEvent> getReceivedEvents() {
        return this.receivedEvents;
    }

    public void clearReceivedEvents() {
        this.receivedEvents.clear();
    }

    public int getEventsCount() {
        return this.receivedEvents.size();
    }

    public boolean containsInstanceOf(Class<? extends ILayerEvent> class1) {
        for (ILayerEvent inEvent : this.receivedEvents) {
            if (inEvent.getClass().isAssignableFrom(class1)) {
                return true;
            }
        }
        return false;
    }

    public ILayerEvent getReceivedEvent(Class<? extends ILayerEvent> class1) {
        for (ILayerEvent inEvent : this.receivedEvents) {
            if (inEvent.getClass().isAssignableFrom(class1)) {
                return inEvent;
            }
        }
        return null;
    }

}
