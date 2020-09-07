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
package org.eclipse.nebula.widgets.nattable.dataset.person;

public class Address {

    private String street;
    private int housenumber;
    private int postalCode;
    private String city;

    /**
     * @return the street
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * @param street
     *            the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return the housenumber
     */
    public int getHousenumber() {
        return this.housenumber;
    }

    /**
     * @param housenumber
     *            the housenumber to set
     */
    public void setHousenumber(int housenumber) {
        this.housenumber = housenumber;
    }

    /**
     * @return the postalCode
     */
    public int getPostalCode() {
        return this.postalCode;
    }

    /**
     * @param postalCode
     *            the postalCode to set
     */
    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return this.city;
    }

    /**
     * @param city
     *            the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

}
