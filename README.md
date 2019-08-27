# money-transfer
REST API for transaction demonstration

API documentation:

* Start app: ```./gradlew moneytransfer```<br/><br/>
* Create account: ```curl -X POST http://localhost:8080/accounts``` <br/><br/>
* Get account by id: ```curl -X GET http://localhost:8080/account/{accountId}``` <br/><br/>
* Get all accounts: ```curl -X GET http://localhost:8080/accounts``` <br/><br/>
* Debit account: ```curl -X PUT http://localhost:8080/accounts/{accountId}/debit/{amount}``` <br/><br/>
* Withdraw money from account by id: <br/>
```curl -X PUT http://localhost:8080/account/{accountId}/withdraw/{amount}``` <br/><br/>
* Tranfer money from ```fromAccountId``` to ```toAccountId```: <br/>
```curl -X PUT http://localhost:8080//accounts/from/{fromAccountId}/to/{toAccountId}/transfer/{amount}```
