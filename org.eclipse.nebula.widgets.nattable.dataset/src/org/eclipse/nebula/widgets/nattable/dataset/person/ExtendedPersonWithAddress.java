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

import java.util.Date;
import java.util.List;

public class ExtendedPersonWithAddress extends PersonWithAddress {

    private String password;
    private List<String> favouriteFood;
    private List<String> favouriteDrinks;
    private int age;
    private String filename;

    @SuppressWarnings("deprecation")
    public ExtendedPersonWithAddress(int id, String firstName, String lastName,
            Gender gender, boolean married, Date birthday, Address address,
            String password, String description, double money,
            List<String> favouriteFood, List<String> favouriteDrinks) {
        super(id, firstName, lastName, gender, married, birthday, address);

        this.password = password;
        this.description = description;
        this.money = money;
        this.favouriteFood = favouriteFood;
        this.favouriteDrinks = favouriteDrinks;
        this.age = new Date().getYear() - getBirthday().getYear();
    }

    @SuppressWarnings("deprecation")
    public ExtendedPersonWithAddress(Person person, Address address,
            String password, String description, double money,
            List<String> favouriteFood, List<String> favouriteDrinks) {
        super(person, address);

        this.password = password;
        this.description = description;
        this.money = money;
        this.favouriteFood = favouriteFood;
        this.favouriteDrinks = favouriteDrinks;
        this.age = new Date().getYear() - getBirthday().getYear();
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the favouriteFood
     */
    public List<String> getFavouriteFood() {
        return this.favouriteFood;
    }

    /**
     * @param favouriteFood
     *            the favouriteFood to set
     */
    public void setFavouriteFood(List<String> favouriteFood) {
        this.favouriteFood = favouriteFood;
    }

    /**
     * @return the favouriteDrinks
     */
    public List<String> getFavouriteDrinks() {
        return this.favouriteDrinks;
    }

    /**
     * @param favouriteDrinks
     *            the favouriteDrinks to set
     */
    public void setFavouriteDrinks(List<String> favouriteDrinks) {
        this.favouriteDrinks = favouriteDrinks;
    }

    /**
     * @return the age
     */
    public int getAge() {
        return this.age;
    }

    /**
     * @param age
     *            the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * @param filename
     *            the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

}
