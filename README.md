# Portfolio-Tracking-API

This application is built using Springboot and H2 database.

Following are the end-points for APIs

1. add trade -> http://localhost:8080/add-trade
2. update trade by id -> http://localhost:8080/update-trade/:tradeId
3. delete trade by id -> http://localhost:8080/trades/{trade-id}
4. get all trades of user -> http://localhost:8080/trades/all
5. get all holdings of user -> http://localhost:8080/holdings/all
6. get the portfolio of user -> http://localhost:8000/fetch-portfolio
7. get the cumulative returns -> http://localhost:8080/fetch-returns