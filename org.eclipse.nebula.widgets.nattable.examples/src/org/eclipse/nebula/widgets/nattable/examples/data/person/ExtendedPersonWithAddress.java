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
package org.eclipse.nebula.widgets.nattable.examples.data.person;

import java.util.Date;
import java.util.List;


/**
 * @author Dirk Fauth
 *
 */
public class ExtendedPersonWithAddress extends PersonWithAddress {

	private String password;
	private String description;
	private List<String> favouriteFood;
	private List<String> favouriteDrinks;
	private int age;
	private double money;
	private String filename;
	
	@SuppressWarnings("deprecation")
	public ExtendedPersonWithAddress(int id, String firstName, String lastName,
			Gender gender, boolean married, Date birthday, Address address,
			String password, String description, double money, List<String> favouriteFood, 
			List<String> favouriteDrinks) {
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
			String password, String description, double money, List<String> favouriteFood, 
			List<String> favouriteDrinks) {
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
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the favouriteFood
	 */
	public List<String> getFavouriteFood() {
		return favouriteFood;
	}

	/**
	 * @param favouriteFood the favouriteFood to set
	 */
	public void setFavouriteFood(List<String> favouriteFood) {
		this.favouriteFood = favouriteFood;
	}

	/**
	 * @return the favouriteDrinks
	 */
	public List<String> getFavouriteDrinks() {
		return favouriteDrinks;
	}

	/**
	 * @param favouriteDrinks the favouriteDrinks to set
	 */
	public void setFavouriteDrinks(List<String> favouriteDrinks) {
		this.favouriteDrinks = favouriteDrinks;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * @return the money
	 */
	public double getMoney() {
		return money;
	}

	/**
	 * @param money the money to set
	 */
	public void setMoney(double money) {
		this.money = money;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
