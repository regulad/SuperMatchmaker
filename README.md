# Matchmaker

Matchmaker is a BungeeCord plugin used to make matches on [ender.quest](https://www.ender.quest).

## Developers

### Maven

Insert the following snippets into your POM.xml.

For the repository:

```xml
<repositories>
    ...
    <repository>
        <id>regulad-releases</id>
        <url>https://nexus.regulad.xyz/repository/maven-releases/</url>
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
        <artifactId>Matchmaker</artifactId>
        <version>{version}</version>
    </dependency>
    ...
</dependencies>
```

Replace `{version}` with the current version. You can see the current version below. Don't include the "v".

![Current Version](https://img.shields.io/github/v/release/EnderQuestMC/Matchmaker)

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
