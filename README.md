# Kordis
[![CircleCI](https://circleci.com/gh/Tea-Ayataka/Kordis.svg?style=svg)](https://circleci.com/gh/Tea-Ayataka/Kordis)
[![](https://jitpack.io/v/Tea-Ayataka/Kordis.svg)](https://jitpack.io/#Tea-Ayataka/Kordis)  

Kordis is a lightweight Kotlin wrapper for the Discord API. Basically, *A discord bot library that doesn't suck*.

# Installation
with Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
    
dependencies {
    implementation 'com.github.Tea-Ayataka:Kordis:0.1.3'
}
```
with Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Tea-Ayataka</groupId>
    <artifactId>Kordis</artifactId>
    <version>0.1.3</version>
</dependency>
```

# Example
```kotlin
fun main(args: Array<String>) = runBlocking {
    TestBot().start()
}

class TestBot {
    suspend fun start() {
        val client = Kordis.create {
            token = "< insert your bot token here >"
            
            // Simple Event Handler
            addHandler<UserJoinEvent> {
                println(it.member.name + " has joined")
            }
            
            // Annotation based Event Listener
            addListener(this@TestBot)
        }
    }
    
    @EventHandler
    suspend fun onMessageReceive(event: MessageReceiveEvent) {
        // Simple ping-pong
        if(event.message.content.equals("!ping", true)){
            event.message.channel.send("!pong")
        }
        
        // Sending an embedded message
        if (event.message.content == "!serverinfo") {
            event.message.channel.send {
                embed {
                    author(name = server.name)
                    field("ID", server.id)
                    field("Server created", server.timestamp.formatAsDate(), true)
                    field("Members", server.members.joinToString { it.name }, true)
                    field("Text channels", server.textChannels.joinToString { it.name })
                    field("Voice channels", server.voiceChannels.joinToString { it.name }.ifEmpty { "None" })
                    field("Emojis", server.emojis.size, true)
                    field("Roles", server.roles.joinToString { it.name }, true)
                    field("Owner", server.owner!!.mention, true)
                    field("Region", server.region.displayName, true)
                }
            }
        }
        
        // Adding a role
        if (event.message.content.equals("!member", true)) {
            val server = event.server ?: return
            val member = event.message.member ?: return

            server.roles.findByName("Member", true)?.let {
                member.addRole(it)
            }
        }
    }
```

# Dependencies
* Kotlin 1.3.20 (JVM 11)
* Kotlin Coroutines 1.1.1
* Gson 2.8.5
