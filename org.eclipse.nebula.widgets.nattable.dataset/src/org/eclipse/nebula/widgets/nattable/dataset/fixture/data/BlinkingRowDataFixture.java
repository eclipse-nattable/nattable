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
package org.eclipse.nebula.widgets.nattable.dataset.fixture.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean wired with a property change listener for Glazed lists. See Glazed Lists
 * ObservableElementList Screencast for details
 *
 */
public class BlinkingRowDataFixture extends RowDataFixture {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PropertyChangeListener changeListener;

    public BlinkingRowDataFixture(PropertyChangeListener changeListener, RowDataFixture rowDataFixture) {
        super(rowDataFixture.getSecurity_id(),
                rowDataFixture.getSecurity_description(),
                rowDataFixture.getRating(),
                rowDataFixture.getIssue_date(),
                rowDataFixture.getPricing_type(),
                rowDataFixture.getBid_price(),
                rowDataFixture.getAsk_price(),
                rowDataFixture.getLot_size(),
                rowDataFixture.isPublish_flag(),
                rowDataFixture.getHigh52Week(),
                rowDataFixture.getLow52Week(),
                rowDataFixture.getEps(),
                rowDataFixture.getVolume(),
                rowDataFixture.getMarketCap(),
                rowDataFixture.getInstitutionOwned());
        this.changeListener = changeListener;
        addPropertyChangeListener(changeListener);
    }

    public static List<BlinkingRowDataFixture> getList(PropertyChangeListener changeListener) {
        List<RowDataFixture> list = RowDataListFixture.getList();
        List<BlinkingRowDataFixture> blinkingList = new ArrayList<BlinkingRowDataFixture>();

        for (RowDataFixture rowDataFixture : list) {
            blinkingList.add(new BlinkingRowDataFixture(changeListener, rowDataFixture));
        }
        return blinkingList;
    }

    // Methods invoked by Glazed lists to add/remove property change listeners

    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.support.removePropertyChangeListener(l);
        this.support.removePropertyChangeListener(this.changeListener);
    }

    // Accessors modified to fire property change events

    @Override
    public void setBid_price(double bid_price) {
        double oldBidPrice = this.bid_price;
        this.bid_price = bid_price;
        this.support.firePropertyChange("bid_price", Double.valueOf(oldBidPrice),
                Double.valueOf(this.bid_price));
    }

    @Override
    public void setAsk_price(double ask_price) {
        double oldAskPrice = this.ask_price;
        this.ask_price = ask_price;
        this.support.firePropertyChange("ask_price", Double.valueOf(oldAskPrice),
                Double.valueOf(this.ask_price));
    }

    @Override
    public String toString() {
        return getSecurity_description();
    }

}
