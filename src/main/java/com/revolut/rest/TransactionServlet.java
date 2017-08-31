package com.revolut.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.revolut.beans.Account;
import com.revolut.beans.Transaction;
import com.revolut.database.AccountDAO;
import com.revolut.database.TransactionDAO;

/**
 * Transaction Servlet
 * @author josh
 *
 */
public class TransactionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AccountDAO accountDAO = new AccountDAO();
	private TransactionDAO transDAO = new TransactionDAO();

	/**
	 * Title : Transfers an amount from one account to another
	 * URL : TransferMoney/Transaction
	 * Method : POST
	 * Data Params : {fromAccountName = [string], toAccountName= [string], amount = [numeric]}
	 * Response Codes: Success (200 OK), Bad Request (400)
	 * Example: {fromAccountName = "TestAccount1", toAccountName = "TestAccount2", amount = "10.00"}
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		TransactionResponse transResponse = new TransactionResponse();
		Gson gson = new Gson();
		try {
			String fromAccountName = request.getParameter("fromAccountName");
			String toAccountName = request.getParameter("toAccountName");
			if (StringUtils.isNoneBlank(fromAccountName, toAccountName)
					&& StringUtils.equals(fromAccountName, toAccountName)) {
				transResponse.setMessage("The to and from accounts cannot be the same");
			} else {
				// Params are valid so lets search for transactions
				List<Transaction> transactions = transDAO.getAccountTransactions(fromAccountName, toAccountName);
				if (transactions.isEmpty()) {
					transResponse.setMessage("No transactions were found matching the criteria");
				} else {
					transResponse.setTransactions(transactions);
					transResponse.setMessage(String.format("%d Transactions found", transactions.size()));
				}
				transResponse.setStatus(Response.SUCCESS);
				response.setStatus(HttpServletResponse.SC_OK);
			}
		} catch (Exception e) {
			transResponse.setMessage("The payload was invalid");
		}
		response.getWriter().append(gson.toJson(transResponse));
	}

	/**
	 * Title : Returns all transactions matching given criteria
	 * URL : TransferMoney/Transaction
	 * Method : GET
	 * URL Params :  Optional: fromAccountName=[String], toAccountName=[String]
	 * Response Codes: Success (200 OK), Bad Request (400)
	 * Example: TransferMoney/Transaction?fromAccountName=TestAccount1&toAccountName=TestAccount2 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		TransactionResponse transResponse = new TransactionResponse();
		Gson gson = new Gson();
		try {
			Transaction trans = gson.fromJson(request.getReader(), Transaction.class);
			String fromAccountName = trans != null ? trans.getFromAccountName() : null;
			String toAccountName = trans != null ? trans.getToAccountName() : null;
			BigDecimal amount = trans != null ? trans.getAmount() : null;
			// Validate params
			if (StringUtils.isNoneBlank(fromAccountName, toAccountName) && amount != null
					&& amount.compareTo(BigDecimal.ZERO) == 1) {
				// Params are OK
				Account fromAccount = accountDAO.getAccount(fromAccountName);
				if (fromAccount != null) {
					// From account exists
					if (accountDAO.getAccount(toAccountName) != null) {
						// To account exists
						// Shave off any more than 2 decimal places
						amount = amount.setScale(2, RoundingMode.DOWN);
						if (fromAccount.getBalance().compareTo(amount) == -1) {
							transResponse.setMessage(String.format(
									"From account with name %s does not have enough money to perform this transfer", fromAccountName));
						} else {
							// From account has enough money to make transfer
							// Good to go
							String status = transDAO.doTransfer(fromAccountName, toAccountName, amount);
							if (StringUtils.equals(status, Response.SUCCESS)) {
								transResponse.setStatus(Response.SUCCESS);
								transResponse.setMessage(
										String.format("Successfully performed transfer from %s to %s", fromAccountName, toAccountName));
								response.setStatus(HttpServletResponse.SC_OK);
							} else {
								transResponse.setMessage(
										String.format("Unable to performed transfer from %s to %s", fromAccountName, toAccountName));
							}
						}
					} else {
						transResponse.setMessage(String.format("To account with name %s does not exist", toAccountName));
					}
				} else {
					transResponse.setMessage(String.format("From account with name %s does not exist", fromAccountName));
				}
			} else {
				transResponse.setMessage("The Account Names and amount received were not valid");
			}
		} catch (Exception e) {
			transResponse.setMessage("The payload was invalid");
		}
		response.getWriter().append(gson.toJson(transResponse));
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		TransactionResponse transResponse = new TransactionResponse();
		Gson gson = new Gson();
		transResponse.setMessage("Put operation is not currently supported");
		response.getWriter().append(gson.toJson(transResponse));
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		TransactionResponse transResponse = new TransactionResponse();
		Gson gson = new Gson();
		transResponse.setMessage("Delete operation is not currently supported");
		response.getWriter().append(gson.toJson(transResponse));
	}

}
