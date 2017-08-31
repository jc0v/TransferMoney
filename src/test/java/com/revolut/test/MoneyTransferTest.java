package com.revolut.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.gson.Gson;
import com.revolut.rest.AccountResponse;
import com.revolut.rest.AccountServlet;
import com.revolut.rest.Response;
import com.revolut.rest.TransactionResponse;
import com.revolut.rest.TransactionServlet;

/**
 * Class to test MoneyTransfer servlets
 * @author joshu
 *
 */
public class MoneyTransferTest {

	private Gson gson = new Gson();
	private AccountServlet accountServlet = new AccountServlet();
	private TransactionServlet transServlet = new TransactionServlet();

	@Test
	public void testCreateAccount() throws Exception {
		createAccount("TestAccount1", "50.00");
		AccountResponse accountResponse = getAccount("TestAccount1");
		//Validate
		assertTrue("Status of get account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, accountResponse.getStatus()));
		assertTrue("Name of returned account incorrect",
				StringUtils.equals("TestAccount1", accountResponse.getAccount().getAccountName()));
	}

	@Test
	public void testTransferMoney() throws Exception {
		// Create a few accounts  
		createAccount("TestAccount2", "50.00");
		createAccount("TestAccount3", "100.00");
		createAccount("TestAccount4", "150.00");

		// Transfer 50 from account 4 to account 2
		doTransfer("TestAccount4", "TestAccount2", "50.00");

		// Transfer 10 from account 3 to accounts 2 and 4
		doTransfer("TestAccount3", "TestAccount2", "10.00");
		doTransfer("TestAccount3", "TestAccount4", "10.00");

		// New balances should be
		// Acc 2 = 110 (50 + 100 + 10)
		// Acc 3 = 80 (100 - 10 - 10)
		// Acc 3 = 110 (150 - 50 + 10)

		// Validate
		AccountResponse account2 = getAccount("TestAccount2");
		AccountResponse account3 = getAccount("TestAccount3");
		AccountResponse account4 = getAccount("TestAccount4");

		assertTrue(
				String.format("Account balance of TestAccount2 should be 110.00 and is %.2f",
						account2.getAccount().getBalance()),
				new BigDecimal("110.00").compareTo(account2.getAccount().getBalance()) == 0);
		assertTrue(
				String.format("Account balance of TestAccount3 should be 80.00 and is %.2f",
						account2.getAccount().getBalance()),
				new BigDecimal("80.00").compareTo(account3.getAccount().getBalance()) == 0);
		assertTrue(
				String.format("Account balance of TestAccount4 should be 110.00 and is %.2f",
						account2.getAccount().getBalance()),
				new BigDecimal("110.00").compareTo(account4.getAccount().getBalance()) == 0);
	}

	@Test
	public void testGetTransactions() throws Exception {
		// Create a few accounts
		createAccount("TestAccount5", "100.00");
		createAccount("TestAccount6", "200.00");
		createAccount("TestAccount7", "350.00");

		// Do some transfers
		doTransfer("TestAccount5", "TestAccount6", "50.00");
		doTransfer("TestAccount6", "TestAccount5", "20.00");
		doTransfer("TestAccount6", "TestAccount7", "10.00");
		doTransfer("TestAccount6", "TestAccount7", "40.00");
		doTransfer("TestAccount7", "TestAccount6", "50.00");

		// Three transactions should have been created
		TransactionResponse fromAccount6 = getTransactions("TestAccount6", "");
		TransactionResponse toAccount7 = getTransactions("", "TestAccount7");
		TransactionResponse fromAccount6ToAccount7 = getTransactions("TestAccount6", "TestAccount7");

		// Validate
		assertTrue(String.format("Expected 3 transactions from TestAccount6 but found %d",
				fromAccount6.getTransactions().size()), fromAccount6.getTransactions().size() == 3);
		assertTrue(String.format("Expected 2 transactions to TestAccount7 but found %d",
				toAccount7.getTransactions().size()), toAccount7.getTransactions().size() == 2);
		assertTrue(String.format("Expected 2 transactions from TestAccount6 to TestAccount7 but found %d",
				fromAccount6ToAccount7.getTransactions().size()), fromAccount6ToAccount7.getTransactions().size() == 2);
	}
	
	@Test
	public void testChangeAccountName() throws Exception {
		createAccount("TestAccount8", "350.00");
		changeAccount("TestAccount8", "TestAccount9");
		
		AccountResponse oldAccount = getAccount("TestAccount8");
		AccountResponse newAccount = getAccount("TestAccount9");
		
		//Validate
		assertTrue("Status of get account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, oldAccount.getStatus()));
		assertTrue("Message returned from get account is not correct",
				StringUtils.equals(String.format("Unable to find an account matching the name %s", "TestAccount8"), oldAccount.getMessage()));
		
		assertTrue("Status of get account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, newAccount.getStatus()));
		assertTrue("Message returned from get account is not correct",
				StringUtils.equals(String.format("Successfully retrieved account %s", "TestAccount9"), newAccount.getMessage()));
	}
	
	@Test
	public void testDeleteAccount() throws Exception {
		createAccount("TestAccount10", "10.00");
		AccountResponse account = getAccount("TestAccount10");

		//Validate
		assertTrue("Status of get account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, account.getStatus()));
		assertTrue("Message returned from get account is not correct",
				StringUtils.equals(String.format("Successfully retrieved account %s", "TestAccount10"), account.getMessage()));

		deleteAccount("TestAccount10");
		
		// Check we can no longer get the deleted account
		AccountResponse deletedAccount = getAccount("TestAccount10");		
		assertTrue("Status of get account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, deletedAccount.getStatus()));
		assertTrue("Message returned from get account is not correct",
				StringUtils.equals(String.format("Unable to find an account matching the name %s", "TestAccount10"), deletedAccount.getMessage()));
	}
	
	@Test
	public void testTransferMoneyErrors() throws Exception {
		// Insufficient balance scenario
		createAccount("TestAccount11", "10.00");
		createAccount("TestAccount12", "10.00");
		
		TransactionResponse insufficientBalance = doTransfer("TestAccount11", "TestAccount12", "11.00");
		assertTrue(StringUtils.equals(Response.ERROR, insufficientBalance.getStatus()));
		assertTrue(StringUtils.equals("From account with name TestAccount11 does not have enough money to perform this transfer", insufficientBalance.getMessage()));
		// Check balance is still 10
		AccountResponse account = getAccount("TestAccount11");
		assertTrue(new BigDecimal("10.00").compareTo(account.getAccount().getBalance()) == 0);
		
		// Transfer negative scenario
		TransactionResponse invalidAmount = doTransfer("TestAccount11", "TestAccount12", "-11.00");
		assertTrue(StringUtils.equals(Response.ERROR, invalidAmount.getStatus()));
		assertTrue(StringUtils.equals("The Account Names and amount received were not valid", invalidAmount.getMessage()));		
	}

	private void createAccount(String accountName, String initialBalance) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Create json string for account creation
		BufferedReader br = new BufferedReader(
				new StringReader(String.format("{accountName = \"%s\", balance = \"%s\"}", accountName, initialBalance)));
		when(request.getReader()).thenReturn(br);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		when(response.getWriter()).thenReturn(writer);

		// Fire off request
		accountServlet.doPost(request, response);

		// Get the json response and parse to Response object
		String responseString = sw.toString();
		AccountResponse accountResponse = gson.fromJson(responseString, AccountResponse.class);

		//Validate
		assertTrue("Status of create account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, accountResponse.getStatus()));
		assertTrue("Message returned from create account is not correct",
				StringUtils.equals(String.format("Successfully created account %s", accountName),
						accountResponse.getMessage()));
	}

	private AccountResponse getAccount(String accountName) throws Exception {
		HttpServletRequest getRequest = mock(HttpServletRequest.class);
		HttpServletResponse getResponse = mock(HttpServletResponse.class);

		// Setup params
		when(getRequest.getParameter("accountName")).thenReturn(accountName);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		when(getResponse.getWriter()).thenReturn(writer);

		//Fire off
		accountServlet.doGet(getRequest, getResponse);

		// Get the json response and parse to Response object
		String responseString = sw.toString();
		AccountResponse accountResponse = gson.fromJson(responseString, AccountResponse.class);

		return accountResponse;
	}

	private TransactionResponse doTransfer(String fromAccountName, String toAccountName, String amount) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Create json string for account creation
		BufferedReader br = new BufferedReader(
				new StringReader(String.format("{fromAccountName = \"%s\", toAccountName = \"%s\", amount = \"%s\"}",
						fromAccountName, toAccountName, amount)));
		when(request.getReader()).thenReturn(br);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		when(response.getWriter()).thenReturn(writer);

		// Fire off request
		transServlet.doPost(request, response);

		// Get the json response and parse to Response object
		String responseString = sw.toString();
		TransactionResponse transResponse = gson.fromJson(responseString, TransactionResponse.class);
		return transResponse;
	}

	private TransactionResponse getTransactions(String fromAccountName, String toAccountName) throws Exception {
		HttpServletRequest getRequest = mock(HttpServletRequest.class);
		HttpServletResponse getResponse = mock(HttpServletResponse.class);

		// Setup params
		when(getRequest.getParameter("fromAccountName")).thenReturn(fromAccountName);
		when(getRequest.getParameter("toAccountName")).thenReturn(toAccountName);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		when(getResponse.getWriter()).thenReturn(writer);

		//Fire off
		transServlet.doGet(getRequest, getResponse);

		// Get the json response and parse to Response object
		String responseString = sw.toString();
		TransactionResponse transResponse = gson.fromJson(responseString, TransactionResponse.class);

		//Validate
		assertTrue("Status of get transactions is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, transResponse.getStatus()));
		return transResponse;
	}
	
	private void changeAccount(String accountName, String newAccountName) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Create json string for account change
		BufferedReader br = new BufferedReader(
				new StringReader(String.format("{accountName = \"%s\", newAccountName = \"%s\"}", accountName, newAccountName)));
		when(request.getReader()).thenReturn(br);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		when(response.getWriter()).thenReturn(writer);

		// Fire off request
		accountServlet.doPut(request, response);

		// Get the json response and parse to Response object
		String responseString = sw.toString();
		AccountResponse accountResponse = gson.fromJson(responseString, AccountResponse.class);

		//Validate
		assertTrue("Status of change account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, accountResponse.getStatus()));
		assertTrue("Message returned from update account is not correct",
				StringUtils.equals(String.format("Successfully updated account %s", newAccountName),
						accountResponse.getMessage()));
	}
	
	private void deleteAccount(String accountName) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		// Create json string for account change
		BufferedReader br = new BufferedReader(
				new StringReader(String.format("{accountName = \"%s\"}", accountName)));
		when(request.getReader()).thenReturn(br);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		when(response.getWriter()).thenReturn(writer);

		// Fire off request
		accountServlet.doDelete(request, response);

		// Get the json response and parse to Response object
		String responseString = sw.toString();
		AccountResponse accountResponse = gson.fromJson(responseString, AccountResponse.class);

		//Validate
		assertTrue("Status of delete account is not SUCCESS",
				StringUtils.equals(Response.SUCCESS, accountResponse.getStatus()));
		assertTrue("Message returned from delete account is not correct",
				StringUtils.equals(String.format("Successfully deleted an account %s", accountName),
						accountResponse.getMessage()));
	}
}
