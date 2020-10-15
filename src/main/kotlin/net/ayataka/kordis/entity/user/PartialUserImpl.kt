package net.ayataka.kordis.entity.user

import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity

class PartialUserImpl(client: DiscordClientImpl, id: Long) : PartialUser, DiscordEntity(client, id) {
    override fun toString(): String {
        return "PartialUserImpl(id='$id')"
    }
}