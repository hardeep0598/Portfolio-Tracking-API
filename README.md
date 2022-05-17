# Portfolio-Tracking-API

This application is built using SpringBoot and H2 database. This is a portfolio tracking application that allows adding/deleting/updating trades
and can do basic return calculations etc. Its basic functionalities includes - 
- Adding trades for a security
- Updating a trade
- Removing a trade
- Fetching trades
- Fetching portfolio
- Fetching returns

Following are the end-points for APIs

1. add trade -> http://localhost:8080/add-trade
2. update trade by id -> http://localhost:8080/update-trade/:tradeId
3. delete trade by id -> http://localhost:8080/trades/{trade-id}
4. get all trades of user -> http://localhost:8080/trades/all
5. get all holdings of user -> http://localhost:8080/holdings/all
6. get the portfolio of user -> http://localhost:8000/fetch-portfolio
7. get the cumulative returns -> http://localhost:8080/fetch-returns
