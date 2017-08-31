package com.revolut.rest;

import com.revolut.beans.Account;

public class AccountResponse extends Response {
	private Account account;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
