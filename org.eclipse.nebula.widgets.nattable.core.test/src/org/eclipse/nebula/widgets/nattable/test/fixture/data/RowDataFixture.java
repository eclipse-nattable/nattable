/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.data;

import static org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture.PRICING_MANUAL;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.getRandomDate;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

public class RowDataFixture {

    public String security_id;
    public String security_description;
    public String rating;
    public Date issue_date;
    public PricingTypeBean pricing_type;
    public double bid_price;
    public double ask_price;
    public int lot_size;
    public boolean publish_flag;

    public double high52Week;
    public double low52Week;
    public double eps;
    public double volume;
    public double marketCap;
    public double institutionOwned;

    public String field20;
    public String field21;
    public String field22;
    public String field23;
    public String field24;
    public String field25;
    public String field26;
    public String field27;
    public String field28;
    public String field29;
    public String field30;

    public boolean field31;
    public boolean field32;
    public Date field33;
    public Date field34;
    public double field35;
    public double field36;
    public double field37;
    public double field38;
    public double field39;
    public double field40;

    public static final IRowIdAccessor<RowDataFixture> rowIdAccessor = new IRowIdAccessor<RowDataFixture>() {
        @Override
        public Serializable getRowId(RowDataFixture rowObject) {
            return rowObject.getSecurity_description();
        }
    };

    public RowDataFixture(String security_id, String security_description,
            String rating, Date issue_date, PricingTypeBean pricing_type,
            double bid_price, double ask_price, int lot_size,
            boolean publish_flag, double high52Week, double low52Week,
            double eps, double volume, double marketCap, double institutionOwned) {
        super();
        this.security_id = security_id;
        this.security_description = security_description;
        this.rating = rating;
        this.issue_date = issue_date;
        this.pricing_type = pricing_type;
        this.bid_price = bid_price;
        this.ask_price = ask_price;
        this.lot_size = lot_size;
        this.publish_flag = publish_flag;
        this.high52Week = high52Week;
        this.low52Week = low52Week;
        this.eps = eps;
        this.volume = volume;
        this.marketCap = marketCap;
        this.institutionOwned = institutionOwned;

        // Filler
        this.field20 = "field20";
        this.field21 = "field21";
        this.field22 = "field22";
        this.field23 = "field23";
        this.field24 = "field24";
        this.field25 = "field25";
        this.field26 = "field26";
        this.field27 = "field27";
        this.field28 = "field28";
        this.field29 = "field29";
        this.field30 = "field30";

        this.field31 = true;
        this.field32 = false;
        this.field33 = new Date(45124575);
        this.field34 = new Date(754512457);
        this.field35 = 350000;
        this.field36 = 360000;
        this.field37 = 370000;
        this.field38 = 380000;
        this.field39 = 390000;
        this.field40 = 400000;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RowDataFixture other = (RowDataFixture) obj;
        if (this.issue_date == null) {
            if (other.issue_date != null)
                return false;
        } else if (!this.issue_date.equals(other.issue_date))
            return false;
        if (this.security_id == null) {
            if (other.security_id != null)
                return false;
        } else if (!this.security_id.equals(other.security_id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.issue_date == null) ? 0 : this.issue_date.hashCode());
        result = prime * result + ((this.security_id == null) ? 0 : this.security_id.hashCode());
        return result;
    }

    /**
     * Convenience method to quickly get a new instance
     */
    public static RowDataFixture getInstance(String descrition, String rating) {
        return new RowDataFixture("US" + ObjectUtils.getRandomNumber(1000),
                descrition, rating, getRandomDate(), PRICING_MANUAL, 1.000, 10,
                1000, true, 1.00, 1.01, -.01, 1000, 1000, 1000D);
    }

    @Override
    public String toString() {
        return "RowDataFixture [security_id=" + this.security_id + ", security_description=" + this.security_description + ", rating=" + this.rating
                + ", issue_date="
                + this.issue_date + ", pricing_type=" + this.pricing_type + ", bid_price=" + this.bid_price + ", ask_price=" + this.ask_price + ", lot_size="
                + this.lot_size
                + ", publish_flag=" + this.publish_flag + ", high52Week=" + this.high52Week + ", low52Week=" + this.low52Week + ", eps=" + this.eps
                + ", volume=" + this.volume
                + ", marketCap=" + this.marketCap + ", institutionOwned=" + this.institutionOwned + ", field20=" + this.field20 + ", field21=" + this.field21
                + ", field22="
                + this.field22 + ", field23=" + this.field23 + ", field24=" + this.field24 + ", field25=" + this.field25 + ", field26=" + this.field26
                + ", field27=" + this.field27
                + ", field28=" + this.field28 + ", field29=" + this.field29 + ", field30=" + this.field30 + ", field31=" + this.field31 + ", field32="
                + this.field32 + ", field33="
                + this.field33 + ", field34=" + this.field34 + ", field35=" + this.field35 + ", field36=" + this.field36 + ", field37=" + this.field37
                + ", field38=" + this.field38
                + ", field39=" + this.field39 + ", field40=" + this.field40 + "]";
    }

    public String getSecurity_id() {
        return this.security_id;
    }

    public void setSecurity_id(String security_id) {
        this.security_id = security_id;
    }

    public String getSecurity_description() {
        return this.security_description;
    }

    public void setSecurity_description(String security_description) {
        this.security_description = security_description;
    }

    public String getRating() {
        return this.rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Date getIssue_date() {
        return this.issue_date;
    }

    public void setIssue_date(Date issue_date) {
        this.issue_date = issue_date;
    }

    public PricingTypeBean getPricing_type() {
        return this.pricing_type;
    }

    public void setPricing_type(PricingTypeBean pricing_type) {
        this.pricing_type = pricing_type;
    }

    public double getBid_price() {
        return this.bid_price;
    }

    public void setBid_price(double bid_price) {
        this.bid_price = bid_price;
    }

    public double getAsk_price() {
        return this.ask_price;
    }

    public void setAsk_price(double ask_price) {
        this.ask_price = ask_price;
    }

    public int getLot_size() {
        return this.lot_size;
    }

    public void setLot_size(int lot_size) {
        this.lot_size = lot_size;
    }

    public boolean isPublish_flag() {
        return this.publish_flag;
    }

    public void setPublish_flag(boolean publish_flag) {
        this.publish_flag = publish_flag;
    }

    public double getSpread() {
        return this.ask_price - this.bid_price;
    }

    public double getHigh52Week() {
        return this.high52Week;
    }

    public void setHigh52Week(double high52Week) {
        this.high52Week = high52Week;
    }

    public double getLow52Week() {
        return this.low52Week;
    }

    public void setLow52Week(double low52Week) {
        this.low52Week = low52Week;
    }

    public double getEps() {
        return this.eps;
    }

    public void setEps(double eps) {
        this.eps = eps;
    }

    public double getVolume() {
        return this.volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getMarketCap() {
        return this.marketCap;
    }

    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }

    public double getInstitutionOwned() {
        return this.institutionOwned;
    }

    public void setInstitutionOwned(double institutionOwned) {
        this.institutionOwned = institutionOwned;
    }

    public String getField20() {
        return this.field20;
    }

    public void setField20(String field20) {
        this.field20 = field20;
    }

    public String getField21() {
        return this.field21;
    }

    public void setField21(String field21) {
        this.field21 = field21;
    }

    public String getField22() {
        return this.field22;
    }

    public void setField22(String field22) {
        this.field22 = field22;
    }

    public String getField23() {
        return this.field23;
    }

    public void setField23(String field23) {
        this.field23 = field23;
    }

    public String getField24() {
        return this.field24;
    }

    public void setField24(String field24) {
        this.field24 = field24;
    }

    public String getField25() {
        return this.field25;
    }

    public void setField25(String field25) {
        this.field25 = field25;
    }

    public String getField26() {
        return this.field26;
    }

    public void setField26(String field26) {
        this.field26 = field26;
    }

    public String getField27() {
        return this.field27;
    }

    public void setField27(String field27) {
        this.field27 = field27;
    }

    public String getField28() {
        return this.field28;
    }

    public void setField28(String field28) {
        this.field28 = field28;
    }

    public String getField29() {
        return this.field29;
    }

    public void setField29(String field29) {
        this.field29 = field29;
    }

    public String getField30() {
        return this.field30;
    }

    public void setField30(String field30) {
        this.field30 = field30;
    }

    public boolean isField31() {
        return this.field31;
    }

    public void setField31(boolean field31) {
        this.field31 = field31;
    }

    public boolean isField32() {
        return this.field32;
    }

    public void setField32(boolean field32) {
        this.field32 = field32;
    }

    public Date getField33() {
        return this.field33;
    }

    public void setField33(Date field33) {
        this.field33 = field33;
    }

    public Date getField34() {
        return this.field34;
    }

    public void setField34(Date field34) {
        this.field34 = field34;
    }

    public double getField35() {
        return this.field35;
    }

    public void setField35(double field35) {
        this.field35 = field35;
    }

    public double getField36() {
        return this.field36;
    }

    public void setField36(double field36) {
        this.field36 = field36;
    }

    public double getField37() {
        return this.field37;
    }

    public void setField37(double field37) {
        this.field37 = field37;
    }

    public double getField38() {
        return this.field38;
    }

    public void setField38(double field38) {
        this.field38 = field38;
    }

    public double getField39() {
        return this.field39;
    }

    public void setField39(double field39) {
        this.field39 = field39;
    }

    public double getField40() {
        return this.field40;
    }

    public void setField40(double field40) {
        this.field40 = field40;
    }
}
