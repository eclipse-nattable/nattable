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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.getRandomDate;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.getRandomNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RowDataListFixture {

	public static final String LOT_SIZE_PROP_NAME = "lot_size";
	public static final String SECURITY_ID_PROP_NAME = "security_id";
	public static final String SECURITY_DESCRIPTION_PROP_NAME = "security_description";
	public static final String RATING_PROP_NAME = "rating";
	public static final String ISSUE_DATE_PROP_NAME = "issue_date";
	public static final String PRICING_TYPE_PROP_NAME = "pricing_type";
	public static final String BID_PRICE_PROP_NAME = "bid_price";
	public static final String ASK_PRICE_PROP_NAME = "ask_price";
	public static final String SPREAD_PROP_NAME = "spread";
	public static final String PUBLISH_FLAG_PROP_NAME = "publish_flag";

	public static final String HIGH_52_WEEK_PROP_NAME = "high52Week";
	public static final String LOW_52_WEEK_PROP_NAME = "low52Week";
	public static final String EPS_PROP_NAME = "eps";
	public static final String VOLUME_PROP_NAME = "volume";
	public static final String MARKET_CAP_PROP_NAME = "marketCap";
	public static final String INSTITUTION_OWNED_PROP_NAME = "institutionOwned";

	public static final String FIELD_20_PROP_NAME = "field20";
	public static final String FIELD_21_PROP_NAME = "field21";
	public static final String FIELD_22_PROP_NAME = "field22";
	public static final String FIELD_23_PROP_NAME = "field23";
	public static final String FIELD_24_PROP_NAME = "field24";
	public static final String FIELD_25_PROP_NAME = "field25";
	public static final String FIELD_26_PROP_NAME = "field26";
	public static final String FIELD_27_PROP_NAME = "field27";
	public static final String FIELD_28_PROP_NAME = "field28";
	public static final String FIELD_29_PROP_NAME = "field29";
	public static final String FIELD_30_PROP_NAME = "field30";

	public static final String FIELD_31_PROP_NAME = "field31";
	public static final String FIELD_32_PROP_NAME = "field32";
	public static final String FIELD_33_PROP_NAME = "field33";
	public static final String FIELD_34_PROP_NAME = "field34";
	public static final String FIELD_35_PROP_NAME = "field35";
	public static final String FIELD_36_PROP_NAME = "field36";
	public static final String FIELD_37_PROP_NAME = "field37";
	public static final String FIELD_38_PROP_NAME = "field38";
	public static final String FIELD_39_PROP_NAME = "field39";
	public static final String FIELD_40_PROP_NAME = "field40";

	public static final PricingTypeBean PRICING_MANUAL = new PricingTypeBean("MN");
	public static final PricingTypeBean PRICING_AUTO = new PricingTypeBean("AT");

	/**
	 * @return list containing 13 {@link RowDataFixture}.
	 *         The ISIN is unique and randomly generated.
	 */
	public static List<RowDataFixture> getList() {
		List<RowDataFixture> listFixture = new ArrayList<RowDataFixture>();
		listFixture.addAll(Arrays.asList(
            new RowDataFixture("US" + getRandomNumber(), "B Ford Motor", "a", new Date(), PRICING_MANUAL, 4.7912, 20, 1500000, true, 6.75, 1.01, -7.03, 114000000, 2000000000, 5000000000D),
			new RowDataFixture("ABC" + getRandomNumber(), "A Alphabet Co.", "AAA", getRandomDate(), PRICING_AUTO, 1.23456, 10, 10000, true, 5.124, .506, 1.233, 2000000, 50000000, 4500000),
			new RowDataFixture("US" + getRandomNumber(), "C General Electric Co", "B", getRandomDate(), PRICING_MANUAL, 10.1244, 30, 1500000, false, 30.74, 5.73, 1.62, 93350000, 142000000, 70000000),
			new RowDataFixture("US" + getRandomNumber(), "E Nissan Motor Co., Ltd.", "AA", getRandomDate(), PRICING_MANUAL, 7.7891, 50, 80000, true, 17.97, 5.59, 0.50, 489000, 250000000, 250000),
			new RowDataFixture("US" + getRandomNumber(), "D Toyota Motor Corp.", "aaa", getRandomDate(), PRICING_MANUAL, 62.5789, 40, 450000, true, 104.40, 55.41, -2.85, 849000, 1242000000, 2000000),
			new RowDataFixture("US" + getRandomNumber(), "F Honda Motor Co., Ltd.", "aa", getRandomDate(), PRICING_MANUAL, 23.7125, 60, 6500000, false, 36.29, 17.35, -21.83, 1050000, 53000000, 70000),
			new RowDataFixture("US" + getRandomNumber(), "G General Motors Corporation", "B-", getRandomDate(), PRICING_MANUAL, 2.9811, 70, 2585000, true, 18.18, .27, .110, 58714700, 25900000, 2800000),
			new RowDataFixture("US" + getRandomNumber(), "H Yahoo! Inc", "C", new Date(), PRICING_AUTO, 12.9811, 80, 99000, true, 26.86, 8.94, 0.00, 22000000, 22740000000D, 14000000000D),
			new RowDataFixture("US" + getRandomNumber(), "I Microsoft", "BB", getRandomDate(), PRICING_AUTO, 22.5506, 90, 6250000, false, 29.57, 14.87, 1.74, 57000000, 196000000000D, 80000000000D),
			new RowDataFixture("US" + getRandomNumber(), "J Google Inc.", "AAA", getRandomDate(), PRICING_AUTO, 330.9315, 100, 8550000, true, 579.10, 247.30, 13.67, 3000000, 136000000000D, 70000000000D),
			new RowDataFixture("US" + getRandomNumber(), "K Research In Motion Limited", "AA", getRandomDate(), PRICING_MANUAL, 43.0311, 110, 55000, true, 150.30, 44.23, 3.67, 587295, 51000000000D, 0),
			new RowDataFixture("US" + getRandomNumber(), "L Apple Inc.", "AAA", getRandomDate(), PRICING_AUTO, 102.4817, 120, 115000, false, 186.78, 78.20, 1.67, 17000000, 125000000000D, 14000000000D),
			new RowDataFixture("US" + getRandomNumber(), "M Nokia Corp.", "A-", getRandomDate(), PRICING_AUTO, 12.0500, 130, 315000, true, 28.34, 8.47, 1.08, 17390000, 58400000000D, 10000000000D)
		));

		return listFixture;
	}

	public static List<RowDataFixture> getList(int listSize) {
		List<RowDataFixture> largeList = new ArrayList<RowDataFixture>();
		final int smallListSize = getList().size();
		
		for (int i = 0; i < listSize/smallListSize; i++) {
			largeList.addAll(getList());
		}
		final int remainder = listSize % smallListSize;
		largeList.addAll(getList().subList(0, remainder));
		return largeList;
	}

	public static String[] getPropertyNames() {
		return new String[] {
				SECURITY_ID_PROP_NAME,  // string w/format validation AAA000
				SECURITY_DESCRIPTION_PROP_NAME,  // free text
				RATING_PROP_NAME,  // combo: aaa, aa, a, etc
				ISSUE_DATE_PROP_NAME,  // date w/formatting
				PRICING_TYPE_PROP_NAME,  // combo: manual, automatic
				BID_PRICE_PROP_NAME,  // float w/decimal place formatting (ticking)
				ASK_PRICE_PROP_NAME,  // float w/decimal place formatting (ticking)
				SPREAD_PROP_NAME,  // calculated: ask - bid (ticking)
				LOT_SIZE_PROP_NAME,  // integer (ticking)
				PUBLISH_FLAG_PROP_NAME,
				HIGH_52_WEEK_PROP_NAME,
				LOW_52_WEEK_PROP_NAME,
				EPS_PROP_NAME,
				VOLUME_PROP_NAME,
				MARKET_CAP_PROP_NAME,
				INSTITUTION_OWNED_PROP_NAME,
				FIELD_20_PROP_NAME,
				FIELD_21_PROP_NAME,
				FIELD_22_PROP_NAME,
				FIELD_23_PROP_NAME,
				FIELD_24_PROP_NAME,
				FIELD_25_PROP_NAME,
				FIELD_26_PROP_NAME,
				FIELD_27_PROP_NAME,
				FIELD_28_PROP_NAME,
				FIELD_29_PROP_NAME,
				FIELD_30_PROP_NAME,
				FIELD_31_PROP_NAME,
				FIELD_32_PROP_NAME,
				FIELD_33_PROP_NAME,
				FIELD_34_PROP_NAME,
				FIELD_35_PROP_NAME,
				FIELD_36_PROP_NAME,
				FIELD_37_PROP_NAME,
				FIELD_38_PROP_NAME,
				FIELD_39_PROP_NAME,
				FIELD_40_PROP_NAME
		};
	}

	public static Map<String, String> getPropertyToLabelMap() {
		Map<String, String> propertyToLabelMap = new LinkedHashMap<String, String>();
		propertyToLabelMap.put(SECURITY_ID_PROP_NAME, "ISIN");
		propertyToLabelMap.put(SECURITY_DESCRIPTION_PROP_NAME, "Sec Desc");
		// rating
		propertyToLabelMap.put(ISSUE_DATE_PROP_NAME, "Issue Date");
		propertyToLabelMap.put(PRICING_TYPE_PROP_NAME, "Pricing Type");
		propertyToLabelMap.put(BID_PRICE_PROP_NAME, "Bid");
		propertyToLabelMap.put(ASK_PRICE_PROP_NAME, "Ask");
		// spread
		propertyToLabelMap.put(LOT_SIZE_PROP_NAME, "Size (mil)");
		propertyToLabelMap.put(PUBLISH_FLAG_PROP_NAME, "Publish");

		propertyToLabelMap.put(HIGH_52_WEEK_PROP_NAME, "52 Week High");
		propertyToLabelMap.put(LOW_52_WEEK_PROP_NAME, "52 Week Low");
		propertyToLabelMap.put(EPS_PROP_NAME, "EPS");
		propertyToLabelMap.put(VOLUME_PROP_NAME, "Volume");
		propertyToLabelMap.put(MARKET_CAP_PROP_NAME, "Market Cap.");
		propertyToLabelMap.put(INSTITUTION_OWNED_PROP_NAME, "Institution Owned");

		propertyToLabelMap.put(FIELD_20_PROP_NAME, "Field 20");
		propertyToLabelMap.put(FIELD_21_PROP_NAME, "Field 21");
		propertyToLabelMap.put(FIELD_22_PROP_NAME, "Field 22");
		propertyToLabelMap.put(FIELD_23_PROP_NAME, "Field 23");
		propertyToLabelMap.put(FIELD_24_PROP_NAME, "Field 24");
		propertyToLabelMap.put(FIELD_25_PROP_NAME, "Field 25");
		propertyToLabelMap.put(FIELD_26_PROP_NAME, "Field 26");
		propertyToLabelMap.put(FIELD_27_PROP_NAME, "Field 27");
		propertyToLabelMap.put(FIELD_28_PROP_NAME, "Field 28");
		propertyToLabelMap.put(FIELD_29_PROP_NAME, "Field 29");
		propertyToLabelMap.put(FIELD_30_PROP_NAME, "Field 30");

		propertyToLabelMap.put(FIELD_31_PROP_NAME, "Field 31");
		propertyToLabelMap.put(FIELD_32_PROP_NAME, "Field 32");
		propertyToLabelMap.put(FIELD_33_PROP_NAME, "Field 33");
		propertyToLabelMap.put(FIELD_34_PROP_NAME, "Field 34");
		propertyToLabelMap.put(FIELD_35_PROP_NAME, "Field 35");
		propertyToLabelMap.put(FIELD_36_PROP_NAME, "Field 36");
		propertyToLabelMap.put(FIELD_37_PROP_NAME, "Field 37");
		propertyToLabelMap.put(FIELD_38_PROP_NAME, "Field 38");
		propertyToLabelMap.put(FIELD_39_PROP_NAME, "Field 39");
		propertyToLabelMap.put(FIELD_40_PROP_NAME, "Field 40");

		return propertyToLabelMap;
	}

	public static List<String> getPropertyNamesAsList() {
		return Arrays.asList(RowDataListFixture.getPropertyNames());
	}

	/**
	 * Get the index of the property name. This will be same as the order
	 *    in which the columns/properties were initially supplied.
	 */
	public static int getColumnIndexOfProperty(String propertyName) {
		return RowDataListFixture.getPropertyNamesAsList().indexOf(propertyName);
	}
}
