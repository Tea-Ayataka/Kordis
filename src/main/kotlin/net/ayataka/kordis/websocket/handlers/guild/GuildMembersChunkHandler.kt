package net.ayataka.kordis.websocket.handlers.guild

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.server.ServerImpl
import net.ayataka.kordis.entity.server.member.MemberImpl
import net.ayataka.kordis.entity.user.UserImpl
import net.ayataka.kordis.event.events.server.ServerReadyEvent
import net.ayataka.kordis.event.events.server.user.UserJoinEvent
import net.ayataka.kordis.event.events.server.user.UserLeaveEvent
import net.ayataka.kordis.websocket.handlers.GatewayHandler

class GuildMembersChunkHandler : GatewayHandler {
    override val eventType = "GUILD_MEMBERS_CHUNK"

    override fun handle(client: DiscordClientImpl, data: JsonObject) {
        val server = client.servers.find(data["guild_id"].long) as? ServerImpl ?: return

        val objects = data["members"].jsonArray.map { it.jsonObject }
        val ids = objects.map { it["user"].jsonObject["id"].long }

        objects.forEach {
            val userObject = it["user"].jsonObject
            val userId = userObject["id"].long

            server.members.removeIf {
                if (it.id !in ids) {
                    client.eventManager.fire(UserLeaveEvent(it))
                    true
                } else {
                    false
                }
            }

            server.members.updateOrPut(userId, it) {
                val member = MemberImpl(
                        client, it, server,
                        client.users.getOrPut(userId) {
                            UserImpl(client, userObject)
                        }
                )

                if (server.initialized.get()) {
                    // This will be executed only when a member is inserted
                    client.eventManager.fire(UserJoinEvent(member))
                }

                member
            }
        }

        server.applyTemporaryPresences()

        if (!server.ready) {
            server.ready = true

            if (!server.initialized.getAndSet(true)) {
                client.eventManager.fire(ServerReadyEvent(server))
            }
        }
    }
}