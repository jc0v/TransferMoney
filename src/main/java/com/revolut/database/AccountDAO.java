package com.revolut.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.revolut.beans.Account;
import com.revolut.rest.Response;

/**
 * DAO for performing Account related tasks
 * @author josh
 *
 */
public class AccountDAO {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Creates a new account with a given name and opening balance
	 * @param accountName
	 * @param initialBalance
	 * @return
	 */
	public String createAccount(String accountName, BigDecimal initialBalance) {
		String status = Response.ERROR;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getConnection();
			ps = con.prepareStatement("INSERT INTO ACCOUNT(ACCOUNTNAME, BALANCE) VALUES (?, ?)");
			ps.setString(1, accountName);
			ps.setBigDecimal(2, initialBalance);
			ps.executeUpdate();
			status = Response.SUCCESS;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Create Account", e);
			try {
				if (con != null) {
					logger.log(Level.SEVERE, "Rolling back Create Account", e);
					con.rollback();
				}
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Unable to Roll back", e1);
			}
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}	
			} catch (SQLException e) {}
		}
		return status;
	}

	/**
	 * Updates the name on an account
	 * @param accountName
	 * @return
	 */
	public String updateAccount(String accountName, String newAccountName) {
		String status = Response.ERROR;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getConnection();
			ps = con.prepareStatement("UPDATE ACCOUNT set ACCOUNTNAME = ? WHERE ACCOUNTNAME = ?");
			ps.setString(1, newAccountName);
			ps.setString(2, accountName);
			ps.executeUpdate();
			status = Response.SUCCESS;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Update Account", e);
			try {
				if (con != null) {
					logger.log(Level.SEVERE, "Rolling back Update Account", e);
					con.rollback();
				}
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Unable to Roll back", e1);
			}
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}	
			} catch (SQLException e) {}
		}
		return status;
	}

	/**
	 * Deletes an account with a given name
	 * @param accountName
	 * @param initialBalance
	 * @return
	 */
	public String deleteAccount(String accountName) {
		String status = Response.ERROR;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getConnection();
			ps = con.prepareStatement("DELETE FROM ACCOUNT WHERE ACCOUNTNAME = ?");
			ps.setString(1, accountName);
			ps.executeUpdate();
			status = Response.SUCCESS;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Delete Account", e);
			try {
				if (con != null) {
					logger.log(Level.SEVERE, "Rolling back Delete Account", e);
					con.rollback();
				}
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Unable to Roll back", e1);
			}
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}	
			} catch (SQLException e) {}
		}
		return status;
	}

	/**
	 * Returns the account matching the name provided.
	 * Returns null if no match.
	 * @param accountName
	 * @return
	 */
	public Account getAccount(String accountName) {
		Account account = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DBConnection.getConnection();
			ps = con.prepareStatement("SELECT * FROM ACCOUNT WHERE ACCOUNTNAME = ?");
			ps.setString(1, accountName);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				account = new Account();
				account.setId(result.getLong("Id"));
				account.setAccountName(result.getString("AccountName"));
				account.setBalance(result.getBigDecimal("Balance"));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception caught in Get Account", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}	
			} catch (SQLException e) {}
		}
		return account;		
	}
}
