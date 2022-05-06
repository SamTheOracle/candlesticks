
# Candlesticks
Allows to retrieve last 30 minutes candlesticks

## Content
- Decision making
- Implementation
- Start the project

### Decision making
I have decided to not use the given boilerate, and to rely on the framework [Quarkus](https://quarkus.io/).
The reasons I picked that framework are the following:
- I have already used it.
- It has great documentation and it is pretty easy to get started with.
- It has an awesome test suit, starting [Testcontainers](https://www.testcontainers.org/).
- Solid community you can ask stuff to
- Implementation of a small subset of Java EE specifications for CDI that I often use at work.

The main idea to solve the task is to persist all the data coming for the provider, leaving a periodic
task to "grind" this data into candlesticks.
I used this approach because I want to be able, in the future, to handle even more data, since it is pretty hard to
work on it real time. 

For the persistence layer, I picked [MongoDB](https://www.mongodb.com/). This is due to the fact that the data comes from a provider. We do not know for sure that data will always be that way,
so it is much simpler to store it using this kind of database.
Of course, in this task quotes and instruments will always be that way, but still, there are not complex relations between entities to justify a relational database.

### Implementation
The project is a set of modules:
- events module contains the instrument and quote event type. 
- entities module contains the MongoDB data that is going to be persisted. They basically enrich the incoming data with a timestamp
- streamhandler module handles the stream of data, by persisting the events and by fetching them when asked
- server module contains the api and the periodic task that grinds quote to candlestick
Excluding events module, all other modules have also a set of tests that can be found in the test folder.
##### Events module
It is basically the same data provided in the boilerplate. The addition I made are manly to interface: ```CandleStickInstrument``` and ```CandleStickQuote```
that contains all the property an instrument and a quote should have.
#### Entities module
There are three entities: ```Quote```, ```Instrument``` and ```Candlestick```. They all expose persistence methods
using the [Panache](https://quarkus.io/guides/mongodb-panache) library.
#### StreamHandler module
It handle the stream of data. The interface ```StreamHandler``` allows to:
- handle a ```CandleStickInstrument```. It persists it in the database and if the action type is ```DELETE```, it deletes the instrument. It does not remove the quotes, and neither the candlesticks associated with it up till that moment.
- handle a ```CandleStickQuote```. It just persists the quote.
- fetch a stream of data, by passing a range.
#### Server module
It has a periodic task (by default 5 secs, but that can be configured via environment) that takes the data using ```StreamHandler``` in the last minute, and transforms it into candlesticks.
The Api returns an error if the given instrument does not exist, otherwise it fetches the candlesticks in the last 30 minutes.
If candlesticks are missing in the given 30 minutes range, it tries to retrieve candlesticks from the past ones. If even in that case
there are no candles, a default candlestick with prices being ```0.0``` is passed to the client.
Last but not least, it listens to the websockets and uses ```StreamHandler``` to handle the events.

### Start the project
I recommend to use the given ```docker-compose.yml``` and ```use docker-compose up -d```. It downloads mongo database and the public image oracolo/candlesticks that has the app.
The app listens on port 9000 so you can try the api ```http://localhost:9000/candlesticks/<isin>```.
If you do not want to use docker, you can try the following. From the root folder:
- ```mvn clean install```. If you do not want to see the tests going, ```mvn clean install -DskipTest```. Attention, all tests are integration tests and needs to ```Docker``` to start the test containers (and the ports should be free).
- ```cd server```, ```./mvnw clean compile quarkus:dev``` will start the server
- start the provider jar with the websockets.

it should be good to go.

P.S. in the docker-compose.yml there are the env variables for the connection to mongodb and the server for the api