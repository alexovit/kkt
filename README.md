# Kafka - Kotlin - Telegram
This is an example application that sends the data from Kafka broker to the Telegram channel. It uses Spring boot, Kotlin language and a Protobuffer as a data protocol

## Run tests
* Register new Telegram bot and acquire a token
* Create new public Telegram channel getting a channel id
* Add new bot to the Telegram channel as an admin
* Open `application.yml` add token ("123832:fa12...") and channel id ("@crml..")
```yaml
bot:
  token: 678049323:AAF...
  username: test-bot
  chat-id: "@crt..."
```
* Run:
```bash
./gradlew --debug test
```

## Run standalone
```bash
./gradlew build -x test && docker compose up -d
```
