package com.revolut.rest;

import java.util.List;

import com.revolut.beans.Transaction;

public class TransactionResponse extends Response {

	private List<Transaction> transactions;

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

}
