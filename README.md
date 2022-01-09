# Matchmaker

Matchmaker is a cross-platform matchmaking plugin for Waterfall, Velocity, and PaperSpigot>=1.8.8.

## Developers

### Maven

Insert the following snippets into your POM.xml.

For the repository:

```xml

<repositories>
    ...
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/EnderQuestMC/Matchmaker</url>
    </repository>
    ...
</repositories>
```

For the dependency:

```xml

<dependencies>
    ...
    <dependency>
        <groupId>quest.ender</groupId>
        <artifactId>common</artifactId>
        <version>{version}</version>
    </dependency>
    ...
</dependencies>
```

Replace `{version}` with the current version. You can see the current version below. Don't include the "v".

![Current Version](https://img.shields.io/github/v/release/regulad/Matchmaker)

The name of the plugin on all platforms except Velocity is `Matchmaker`, as opposed to `supermatchmaker` on Velocity.

### Messages

Data will be sent on `matchmaker:out`, and data will be received on `matchmaker:in`.

* Subchannel `SendToGame`
    * Data `Name of a game.`
        * Returns `Name of the game that the player was sent to. If it fails, return the string "null".`
          Please note that this task may take a while to return.
* Subchannel `GetGameStats`
    * Data `Name of a game.`
        * Returns `Name of the game that data was requested on.`
        * Returns `Number of players in servers hosting that game. If the game is not found, return 0.`
* Subchannel `GetGame`
    * Returns `Name of the game the player is currently in.`
* Subchannel `GetGames`
    * Returns `A list of all games the server can connect you to, seperated by a ,`
* Subchannel `SentToGame`
    * Data `name of the game`
        * This message will be dispatched when a player joins the server via Matchmaker. May be multiple seperated by
          a `, `. Note that only one message will be sent a server, and the player used to send it may be disregarded.
    