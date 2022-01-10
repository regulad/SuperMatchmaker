# SuperMatchmaker

SuperMatchmaker is a cross-platform matchmaking plugin for Waterfall and PaperSpigot>=1.8.8.

Velocity, Glowstone and Sponge support coming soon.

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
        <url>https://maven.pkg.github.com/regulad/SuperMatchmaker</url>
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

The name of the plugin on PaperSpigot and Waterfall is `Matchmaker` for compatability reasons. On any and all other
platforms, it is simply `SuperMatchmaker`

### API Usage

```java
MatchmakerAPI api=MatchmakerAPI.getInstance();

        CompletableFuture<Collection<String>>games=api.getGames();

        games.thenApply((gameCollection)->{
        for(String game:gameCollection){
        System.out.println(game);
        }
        });
```
