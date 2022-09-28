# tsuser-online-notify

## Table of Contents

* [What is tsuser-online-notify?](#what-is-tsuser-online-notify)
* [Features](#features)
* [Requirements](#requirements)
* [Build](#build)
* [Setup](#setup)
* [Author](#author)
* [License](#license)

## What is tsuser-online-notify?

**tsuser-online-notify** is a TeamSpeak-Client Bot that sends telegram messages when predefined clients connect or
disconnect.

## Features

* Notification on user connect
* Notification on user disconnect
* Applies nickname changes
* Infinite number of users can be set (via nickname or UUID)

## Requirements

* Java 17
* TeamSpeak-Server to connect
* Telegram Bot (created by the @BotFather)

## Build

Building the application can be done via the included maven wrapper.

```
git clone https://github.com/Skyleiger/tsuser-online-notify.git
cd tsuser-online-notify
./mvnw clean package
```

## Setup

Copy the tsuser-online-notify.jar from the `target` folder of the build directory.
Then the application should be started with the following command:

```
java -jar tsuser-online-notify.jar
```

The application should then fail with an error.
This is the case because some environment variables have to be configured for operation.
See the output for more information.

## Author

* [Skyleiger](https://github.com/Skyleiger)

## License

See [LICENSE](https://github.com/tsuser-online-notify/blob/master/LICENSE) file for the tsuser-online-notify license.