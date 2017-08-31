package com.revolut.beans;

public class AccountUpdate extends Account {
	private String newAccountName;

	public String getNewAccountName() {
		return newAccountName;
	}

	public void setNewAccountName(String newAccountName) {
		this.newAccountName = newAccountName;
	}

}
