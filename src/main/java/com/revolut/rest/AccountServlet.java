package com.revolut.rest;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;
import com.revolut.beans.Account;
import com.revolut.beans.AccountUpdate;
import com.revolut.database.AccountDAO;

/**
 * Account Servlet
 * @author josh
 *
 */
public class AccountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AccountDAO accountDAO = new AccountDAO();

	/**
	 * Title : Returns an account matching the given name
	 * URL : TransferMoney/Account?accountName=
	 * Method : GET
	 * URL Params :  Required: accountName=[String]
	 * Response Codes: Success (200 OK), Bad Request (400)
	 * Example: TransferMoney/Account?accountName=TestAccount1
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		AccountResponse accountResponse = new AccountResponse();
		Gson gson = new Gson();
		try {
			String accountName = request.getParameter("accountName");
			if (StringUtils.isNotBlank(accountName)) {
				// Search for account
				Account account = accountDAO.getAccount(accountName);			
				if (account == null) {
					accountResponse.setMessage(String.format("Unable to find an account matching the name %s", accountName));
				} else {
					accountResponse.setAccount(account);
					accountResponse.setMessage(String.format("Successfully retrieved account %s", accountName));
				}
				accountResponse.setStatus(Response.SUCCESS);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				accountResponse.setMessage("An invalid Account Name was received.");
			}
		} catch (Exception e) {
			accountResponse.setMessage("The payload was invalid");
		}
		response.getWriter().append(gson.toJson(accountResponse));
	}

	/**
	 * Title : Creates an account with a given name and opening balance
	 * URL : TransferMoney/Account
	 * Method : POST
	 * Data Params : {accountName = [string], balance = [numeric]}
	 * Response Codes: Success (200 OK), Bad Request (400)
	 * Example: {accountName = "TestAccount1", balance = "100.00"}
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		AccountResponse accountResponse = new AccountResponse();
		Gson gson = new Gson();
		try {
			Account account = gson.fromJson(request.getReader(), Account.class);
			String accountName = account != null ? account.getAccountName() : null;
			BigDecimal initialBalance = account != null ? account.getBalance() : null;
			// Ensure we have a name and an initial balance
			if (StringUtils.isNotBlank(accountName) && initialBalance != null) {
				// Ensure we don't already have an account with this name
				if (accountDAO.getAccount(accountName) == null) {
					String status = accountDAO.createAccount(accountName, initialBalance);
					if (StringUtils.equals(status, Response.SUCCESS)) {
						accountResponse.setAccount(account);
						accountResponse.setStatus(Response.SUCCESS);
						accountResponse.setMessage(String.format("Successfully created account %s", accountName));
						response.setStatus(HttpServletResponse.SC_OK);
					} else {
						// A technical error occurred
						accountResponse.setMessage(String.format("Unable to create account %s", accountName));
					}
				} else {
					accountResponse.setMessage(String.format("An account called %s already exists", accountName));
				}				
			} else {
				accountResponse.setMessage("The Account Name and balance received were not valid");
			}
		} catch (Exception e) {
			accountResponse.setMessage("The payload was invalid");
		}
		response.getWriter().append(gson.toJson(accountResponse));
	}

	/**
	 * Title : Updates a given account with a new name
	 * URL : TransferMoney/Account
	 * Method : PUT
	 * Data Params : {accountName = [string], newAccountName = [string]}
	 * Response Codes: Success (200 OK), Bad Request (400)
	 * Example: {accountName = "TestAccount1", newAccountName = "TestAccount2"} 
	 * 
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		AccountResponse accountResponse = new AccountResponse();
		Gson gson = new Gson();
		try {
			AccountUpdate account = gson.fromJson(request.getReader(), AccountUpdate.class);
			String accountName = account != null ? account.getAccountName() : null;
			String newAccountName = account != null ? account.getNewAccountName() : null;
			// Ensure we have an existing accountName and a new one
			if (StringUtils.isNoneBlank(accountName, newAccountName)) {
				// Ensure there is an account to update
				if (accountDAO.getAccount(accountName) != null) {
					// Check we don't already have an account with the new name
					if (accountDAO.getAccount(newAccountName) == null) {
						String status = accountDAO.updateAccount(accountName, newAccountName);
						if (StringUtils.equals(status, Response.SUCCESS)) {
							accountResponse.setAccount(account);
							accountResponse.setStatus(Response.SUCCESS);
							accountResponse.setMessage(String.format("Successfully updated account %s", newAccountName));
							response.setStatus(HttpServletResponse.SC_OK);
						} else {
							// A technical error occurred
							accountResponse.setMessage(String.format("Unable to update account %s", accountName));
						}
					} else {
						accountResponse.setMessage(String.format("An account called %s already exists", newAccountName));
					}
				} else {
					accountResponse.setMessage(String.format("An account called %s could not be found to update", accountName));
				}
			} else {
				accountResponse.setMessage("The Account Name and new Account Name received were not valid");
			}
		} catch (Exception e) {
			accountResponse.setMessage("The payload was invalid");
		}
		response.getWriter().append(gson.toJson(accountResponse));
	}

	/**
	 * Title : Deletes a given account
	 * URL : TransferMoney/Account
	 * Method : DELETE
	 * Data Params : {accountName = [string]}
	 * Response Codes: Success (200 OK), Bad Request (400)
	 * Example: {accountName = "TestAccount2"}  
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		AccountResponse accountResponse = new AccountResponse();
		Gson gson = new Gson();
		try {
			Account account = gson.fromJson(request.getReader(), Account.class);
			String accountName = account != null ? account.getAccountName() : null;
			if (StringUtils.isNotBlank(accountName) && accountDAO.getAccount(accountName) != null) {
				String status = accountDAO.deleteAccount(accountName);
				if (StringUtils.equals(status, Response.SUCCESS)) {
					accountResponse.setStatus(Response.SUCCESS);
					accountResponse.setMessage(String.format("Successfully deleted an account %s", accountName));
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					accountResponse.setMessage(String.format("Unable to delete account %s", accountName));
				}
			} else {
				accountResponse.setMessage(String.format("No account called %s exists", accountName));
			}
		} catch (Exception e) {
			accountResponse.setMessage("The payload was invalid");
		}
		response.getWriter().append(gson.toJson(accountResponse));
	}

}
