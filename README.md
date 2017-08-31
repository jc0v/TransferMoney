# TransferMoney

To run the application execute the following from the command line

**gradlew tomcatRun**

To run the unit tests execute the following from the command line

**gradlew test**

## Suggested operating steps
1. Create some accounts
2. Transfer money between accounts
3. View transaction history  

For more see API Documentation below

## API Documentation

* Title : Returns an account matching the given name
* URL : TransferMoney/Account?accountName=
* Method : GET
* URL Params :  Required: accountName=[String]
* Response Codes: Success (200 OK), Bad Request (400)
* Example: TransferMoney/Account?accountName=TestAccount1  
-----
* Title : Creates an account with a given name and opening balance
* URL : TransferMoney/Account
* Method : POST
* Data Params : {accountName = [string], balance = [numeric]}
* Response Codes: Success (200 OK), Bad Request (400)
* Example: {accountName = "TestAccount1", balance = "100.00"}  
-----
* Title : Updates a given account with a new name
* URL : TransferMoney/Account
* Method : PUT
* Data Params : {accountName = [string], newAccountName = [string]}
* Response Codes: Success (200 OK), Bad Request (400)
* Example: {accountName = "TestAccount1", newAccountName = "TestAccount2"}  
-----  
* Title : Deletes a given account
* URL : TransferMoney/Account
* Method : DELETE
* Data Params : {accountName = [string]}
* Response Codes: Success (200 OK), Bad Request (400)
* Example: {accountName = "TestAccount2"}  
-----  
* Title : Transfers an amount from one account to another
* URL : TransferMoney/Transaction
* Method : POST
* Data Params : {fromAccountName = [string], toAccountName= [string], amount = [numeric]}
* Response Codes: Success (200 OK), Bad Request (400)
* Example: {fromAccountName = "TestAccount1", toAccountName = "TestAccount2", amount = "10.00"}  
-----
* Title : Returns all transactions matching given criteria
* URL : TransferMoney/Transaction
* Method : GET
* URL Params :  Optional: fromAccountName=[String], toAccountName=[String]
* Response Codes: Success (200 OK), Bad Request (400)
* Example: TransferMoney/Transaction?fromAccountName=TestAccount1&toAccountName=TestAccount2  
