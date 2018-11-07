# Flow Currency Rates

[Assignment description](./Assignment.md)

You can run the solution by cloning this repository and executing
`sbt run`
Press Return to stop the app.

Application provides a basic index page with example endpoint invocations on
http://localhost:9000/

## API

Exposed API contains only GET requests and query parameters so it's convenient to use from a browser.
Fixer platform connectivity is established under the hood - one can override used Fixer Access Key by setting `FIXER_ACCESS_KEY` env variable.

### Currency rates endpoints
- http://localhost:9000/rates?base=USD
- http://localhost:9000/rates?base=USD&target=CAD
- http://localhost:9000/rates?base=USD&timestamp=2016-05-01T14:34:46Z
- http://localhost:9000/rates?base=USD&target=CAD&timestamp=2016-05-01T14:34:46Z

### Currency rates monitoring
- http://localhost:9000/monitoring - lists all currently monitored currency rates
- http://localhost:9000/monitoring/start?currency=USD - starts monitoring currency rates for USD
- http://localhost:9000/monitoring/stop?currency=USD - stops monitoring currency rates for USD

## Libraries used

- Akka HTTP - faster than Play Framework; there was no need to use for example templates, forms, database support etc
- Akka Streams - ideal for endless source of data like a loop of currency rate requests for monitoring
- WireMock - mock http server for unit tests
- Mockito - useful methods for verifying interactions between the test objects
- Play JSON - personal taste :)
