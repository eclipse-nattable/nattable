package org.eclipse.nebula.widgets.nattable.examples.fixtures.person;

import java.util.Date;

public class Person {
	public enum Gender {
		MALE, FEMALE
	}
	
	private String firstName;
	private String lastName;
	private Gender gender;
	private boolean married;
	private Date birthday;
	
	public Person() { }
	
	public Person(String firstName, String lastName, Gender gender, boolean married, Date birthday) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.married = married;
		this.birthday = birthday;
	}
	
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}
	
	/**
	 * @param gender the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	/**
	 * @return the married
	 */
	public boolean isMarried() {
		return married;
	}
	
	/**
	 * @param married the married to set
	 */
	public void setMarried(boolean married) {
		this.married = married;
	}
	
	/**
	 * @return the birthday
	 */
	public Date getBirthday() {
		return birthday;
	}
	
	/**
	 * @param birthday the birthday to set
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
}
