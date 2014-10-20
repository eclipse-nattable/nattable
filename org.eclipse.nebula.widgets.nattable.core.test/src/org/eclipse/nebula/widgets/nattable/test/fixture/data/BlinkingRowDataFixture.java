/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;

/**
 * Bean wired with a property change listener for Glazed lists. See Glazed Lists
 * ObservableElementList Screencast for details
 *
 */
public class BlinkingRowDataFixture extends RowDataFixture {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PropertyChangeListener changeListener;

    public static final IRowIdAccessor<BlinkingRowDataFixture> rowIdAccessor = new IRowIdAccessor<BlinkingRowDataFixture>() {
        @Override
        public Serializable getRowId(BlinkingRowDataFixture rowObject) {
            return rowObject.getSecurity_description();
        }
    };

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
