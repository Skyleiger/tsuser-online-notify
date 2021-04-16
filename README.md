# TSUserOnlineNotify

## Table of Contents

* [What is TSUserOnlineNotify?](#what-is-tsuseronlinenotify)
* [Features](#features)
* [Requirements](#requirements)
* [Build](#build)
* [Setup](#setup)
* [Author](#author)
* [License](#license)

## What is TSUserOnlineNotify?

**TSUserOnlineNotify** is a TeamSpeak-Client Bot that sends telegram messages when predefined clients connect or
disconnect.

## Features

* Notification on user connect
* Notification on user disconnect
* Applies nickname changes
* Infinite number of users (via nickname or UUID) can be seta

## Requirements

* Java 11
* TeamSpeak-Server to connect
* Telegram Bot (created by the @BotFather)

## Build

Building the application requires maven 3.

```
git clone https://github.com/TSUserOnlineNotify.git
cd TSUserOnlineNotify
mvn clean package
```

## Setup

Copy the TSUserOnlineNotify-X.jar (different for each version) from the `target` folder of the build directory. Then the
application should be started with the following command:

```
java -jar TSUserOnlineNotify-X.jar
```

If you want to use the Sentry integration, you must also specify the Sentry DSN key. Then the command should look like
this:

```
java -Dsentry.dsn=<your-sentry-dsn> -jar TSUserOnlineNotify-X.jar
```

`<your-sentry-dsn>` must be replaced with the Sentry DSN key.

The application should then close with a database error. Now a config.yml was created, which must be customized. When
this is done the application is functional and can load modules for crawling, for example.

## Author

* **Dominic Wienzek** - [Skyleiger](https://github.com/Skyleiger)

## License

See [LICENSE](https://github.com/TSUserOnlineNotify/blob/master/LICENSE) file for the TSUserOnlineNotify license.