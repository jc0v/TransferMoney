package com.revolut.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.revolut.beans.Transaction;
import com.revolut.rest.Response;


public class TransactionDAO {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public String doTransfer(String fromAccountName, String toAccountName, BigDecimal amount) {
		String status = Response.ERROR;
		Connection con = null;
		PreparedStatement fromPs = null;
		PreparedStatement toPs = null;
		PreparedStatement transPs = null;
		try {
			con = DBConnection.getConnection();
			con.setAutoCommit(false);
			// First update the from account balance
			fromPs = con.prepareStatement("UPDATE ACCOUNT SET BALANCE = BALANCE - ? WHERE ACCOUNTNAME = ?");
			fromPs.setBigDecimal(1, amount);
			fromPs.setString(2, fromAccountName);
			fromPs.executeUpdate();

			// Then update the to account balance
			toPs = con.prepareStatement("UPDATE ACCOUNT SET BALANCE = BALANCE + ? WHERE ACCOUNTNAME = ?");
			toPs.setBigDecimal(1, amount);
			toPs.setString(2, toAccountName);
			toPs.executeUpdate();

			// Then create a new transaction entry
			transPs = con.prepareStatement("INSERT INTO TRANSACTION (FromAccountId, ToAccountId, Amount, TransactionDate) "
					+ "VALUES (SELECT ID FROM ACCOUNT WHERE ACCOUNTNAME = ?, SELECT ID FROM ACCOUNT WHERE ACCOUNTNAME = ?, ?, ?)");
			transPs.setString(1, fromAccountName);
			transPs.setString(2, toAccountName);
			transPs.setBigDecimal(3, amount);
			transPs.setTimestamp(4, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			transPs.executeUpdate();
			con.commit();
			status = Response.SUCCESS;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Do Transfer", e);
			try {
				if (con != null) {
					logger.log(Level.SEVERE, "Rolling back Transfer", e);
					con.rollback();
				}
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Unable to Roll back", e1);
			}
		} finally {
			try {
				if (fromPs != null) {
					fromPs.close();
				}
				if (toPs != null) {
					toPs.close();
				}
				if (transPs != null) {
					transPs.close();
				}
				if (con != null) {
					con.close();
				}	
			} catch (SQLException e) {}
		}
		return status;
	}


	public List<Transaction> getAccountTransactions(String fromAccountName, String toAccountName) {
		List<Transaction> transactions = new ArrayList<>();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getConnection();
			String SQL_BASE = "SELECT trans.*, toAccount.ACCOUNTNAME AS TOACCOUNTNAME, fromAccount.ACCOUNTNAME AS FROMACCOUNTNAME FROM TRANSACTION trans "
					+ "INNER JOIN ACCOUNT toAccount on toAccount.ID = trans.TOACCOUNTID "
					+ "INNER JOIN ACCOUNT fromAccount on fromAccount.ID = trans.FROMACCOUNTID";
			if (StringUtils.isAllBlank(fromAccountName, toAccountName)) {
				ps = con.prepareStatement(SQL_BASE);
			} else if (StringUtils.isNotBlank(fromAccountName) && StringUtils.isBlank(toAccountName)) {
				ps = con.prepareStatement(SQL_BASE + " WHERE fromAccount.ACCOUNTNAME = ? ");
				ps.setString(1, fromAccountName);
			} else if (StringUtils.isBlank(fromAccountName) && StringUtils.isNotBlank(toAccountName)) {
				ps = con.prepareStatement(SQL_BASE);
				ps = con.prepareStatement(SQL_BASE + " WHERE toAccount.ACCOUNTNAME = ? ");
				ps.setString(1, toAccountName);
			} else {
				ps = con.prepareStatement(SQL_BASE);
				ps = con.prepareStatement(SQL_BASE + " WHERE fromAccount.ACCOUNTNAME = ? AND toAccount.ACCOUNTNAME = ? ");
				ps.setString(1, fromAccountName);
				ps.setString(2, toAccountName);
			}

			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Transaction trans = new Transaction();
				trans.setId(result.getLong("Id"));
				trans.setFromAccountName(result.getString("FROMACCOUNTNAME"));
				trans.setToAccountName(result.getString("TOACCOUNTNAME"));
				trans.setAmount(result.getBigDecimal("Amount"));
				trans.setTransactionDate(result.getDate("TransactionDate"));
				transactions.add(trans);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Get Transactions", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
			}
		}
		return transactions;
	}

	//DELETE
	public void reverseTransaction(long transactionId) {

	}
}
