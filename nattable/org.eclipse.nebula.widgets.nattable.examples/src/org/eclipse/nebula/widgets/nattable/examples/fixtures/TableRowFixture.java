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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import static org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture.PRICING_AUTO;
import static org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture.PRICING_MANUAL;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.getRandomDate;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.getRandomNumber;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

public class TableRowFixture implements TableRow {

	public String securityId;
	public String securityDescription;
	public String rating;
	public Date issueDate;
	public PricingTypeBean pricingType;
	public Double bidPrice;
	public Double askPrice;
	public int lotSize;
	public Boolean publishFlag;

	public Double high52Week;
	public Double low52Week;
	public Double eps;
	public Double volume;
	public Double marketCap;
	public Double institutionOwned;

	public final String[] propertyNames = {
			"securityId", "securityDescription", "rating", "issueDate", "pricingType",
			"bidPrice", "askPrice", "lotSize", "publishFlag", "high52Week", "low52Week", "eps",
			"volume", "marketCap", "institutionOwned"};

	public static final IRowIdAccessor<TableRowFixture> rowIdAccessor = new IRowIdAccessor<TableRowFixture>() {
		public Serializable getRowId(TableRowFixture rowObject) {
			return rowObject.getSecurityId();
		}
	};

	public TableRowFixture(
			String security_id,
			String security_description,
			String rating,
			Date issue_date,
			PricingTypeBean pricing_type,
			double bid_price,
			double ask_price,
			int lot_size,
			boolean publish_flag,
			double high52Week,
			double low52Week,
			double eps,
			double volume,
			double marketCap,
			double institutionOwned) {
		super();
		this.securityId = security_id;
		this.securityDescription = security_description;
		this.rating = rating;
		this.issueDate = issue_date;
		this.pricingType = pricing_type;
		this.bidPrice = bid_price;
		this.askPrice = ask_price;
		this.lotSize = lot_size;
		this.publishFlag = publish_flag;
		this.high52Week = high52Week;
		this.low52Week = low52Week;
		this.eps = eps;
		this.volume = volume;
		this.marketCap = marketCap;
		this.institutionOwned = institutionOwned;
	}

	@Override
	public boolean equals(Object obj) {
		final TableRowFixture that = (TableRowFixture) obj;
		return new EqualsBuilder()
					.append(this.getSecurityId(), that.getSecurityId())
					.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
					.append(this.getSecurityId())
					.hashCode();
	}
	/**
	 * Convenience method to quickly get a new instance
	 */
	public static TableRowFixture getInstance(String descrition, String rating) {
		return new TableRowFixture("US" + ObjectUtils.getRandomNumber(1000), descrition, rating, getRandomDate(), PRICING_MANUAL,
				1.000, 10, 1000, true, 1.00, 1.01, -.01, 1000, 1000, 1000D);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	public Object getData() {
		return this;
	}

	public Object getIdentifier() {
		return securityId;
	}

	public Object getValue(int columnIndex) {
		return getValue(propertyNames[columnIndex]);
	}

	public void setValue(int columnIndex, Object value) {
		setValue(propertyNames[columnIndex], value);
	}

	public Object getValue(String propertyName) {
		try{
			return this.getClass().getMethod("get" + StringUtils.capitalize(propertyName)).invoke(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setValue(String propertyName, Object value) {
		try {
			String setterName = "set" + StringUtils.capitalize(propertyName);
			Method[] methods = this.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if(method.getName().equals(setterName)){
					Class<?> class1 = method.getParameterTypes()[0];
					method.invoke(this, class1.cast(value));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Accessors

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public String getSecurityDescription() {
		return securityDescription;
	}

	public void setSecurityDescription(String securityDescription) {
		this.securityDescription = securityDescription;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public PricingTypeBean getPricingType() {
		return pricingType;
	}

	public void setPricingType(PricingTypeBean pricingType) {
		this.pricingType = pricingType;
	}

	public Double getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(Double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public Double getAskPrice() {
		return askPrice;
	}

	public void setAskPrice(Double askPrice) {
		this.askPrice = askPrice;
	}

	public int getLotSize() {
		return lotSize;
	}

	public void setLotSize(int lotSize) {
		this.lotSize = lotSize;
	}

	public Boolean getPublishFlag() {
		return publishFlag;
	}

	public void setPublishFlag(Boolean publishFlag) {
		this.publishFlag = publishFlag;
	}

	public Double getHigh52Week() {
		return high52Week;
	}

	public void setHigh52Week(Double high52Week) {
		this.high52Week = high52Week;
	}

	public Double getLow52Week() {
		return low52Week;
	}

	public void setLow52Week(Double low52Week) {
		this.low52Week = low52Week;
	}

	public Double getEps() {
		return eps;
	}

	public void setEps(Double eps) {
		this.eps = eps;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public Double getMarketCap() {
		return marketCap;
	}

	public void setMarketCap(Double marketCap) {
		this.marketCap = marketCap;
	}

	public Double getInstitutionOwned() {
		return institutionOwned;
	}

	public void setInstitutionOwned(Double institutionOwned) {
		this.institutionOwned = institutionOwned;
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}

	public static List<TableRowFixture> getList() {
		List<TableRowFixture> listFixture = new ArrayList<TableRowFixture>();

		listFixture.addAll(Arrays.asList(
            new TableRowFixture("US" + getRandomNumber(), "B Ford Motor", "a", getRandomDate(), PRICING_MANUAL, 4.7912, 20, 1500000, true, 6.75, 1.01, -7.03, 114000000, 2000000000, 5000000000D),
			new TableRowFixture("ABC" + getRandomNumber(), "A Alphabet Co.", "AAA", getRandomDate(), PRICING_AUTO, 1.23456, 10, 10000, true, 5.124, .506, 1.233, 2000000, 50000000, 4500000),
			new TableRowFixture("US" + getRandomNumber(), "C General Electric Co", "B", getRandomDate(), PRICING_MANUAL, 10.1244, 30, 1500000, false, 30.74, 5.73, 1.62, 93350000, 142000000, 70000000),
			new TableRowFixture("US" + getRandomNumber(), "E Nissan Motor Co., Ltd.", "AA", getRandomDate(), PRICING_MANUAL, 7.7891, 50, 80000, true, 17.97, 5.59, 0.50, 489000, 250000000, 250000),
			new TableRowFixture("US" + getRandomNumber(), "D Toyota Motor Corp.", "aaa", getRandomDate(), PRICING_MANUAL, 62.5789, 40, 450000, true, 104.40, 55.41, -2.85, 849000, 1242000000, 2000000),
			new TableRowFixture("US" + getRandomNumber(), "F Honda Motor Co., Ltd.", "aa", getRandomDate(), PRICING_MANUAL, 23.7125, 60, 6500000, false, 36.29, 17.35, -21.83, 1050000, 53000000, 70000),
			new TableRowFixture("US" + getRandomNumber(), "G General Motors Corporation", "B-", getRandomDate(), PRICING_MANUAL, 2.9811, 70, 2585000, true, 18.18, .27, .110, 58714700, 25900000, 2800000),
			new TableRowFixture("US" + getRandomNumber(), "H Yahoo! Inc", "C", getRandomDate(), PRICING_AUTO, 12.9811, 80, 99000, true, 26.86, 8.94, 0.00, 22000000, 22740000000D, 14000000000D),
			new TableRowFixture("US" + getRandomNumber(), "I Microsoft", "BB", getRandomDate(), PRICING_AUTO, 22.5506, 90, 6250000, false, 29.57, 14.87, 1.74, 57000000, 196000000000D, 80000000000D),
			new TableRowFixture("US" + getRandomNumber(), "J Google Inc.", "AAA", getRandomDate(), PRICING_AUTO, 330.9315, 100, 8550000, true, 579.10, 247.30, 13.67, 3000000, 136000000000D, 70000000000D),
			new TableRowFixture("US" + getRandomNumber(), "K Research In Motion Limited", "AA", getRandomDate(), PRICING_MANUAL, 43.0311, 110, 55000, true, 150.30, 44.23, 3.67, 587295, 51000000000D, 0),
			new TableRowFixture("US" + getRandomNumber(), "L Apple Inc.", "AAA", getRandomDate(), PRICING_AUTO, 102.4817, 120, 115000, false, 186.78, 78.20, 1.67, 17000000, 125000000000D, 14000000000D),
			new TableRowFixture("US" + getRandomNumber(), "M Nokia Corp.", "A-", getRandomDate(), PRICING_AUTO, 12.0500, 130, 315000, true, 28.34, 8.47, 1.08, 17390000, 58400000000D, 10000000000D)
		));

		return listFixture;
	}
}
