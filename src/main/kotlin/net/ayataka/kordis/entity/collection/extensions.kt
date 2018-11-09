package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.server.Role
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.user.User

fun NameableEntitySet<Member>.find(user: User) =
        find(user.id)

fun NameableEntitySet<Member>.findByTag(tag: String, ignoreCase: Boolean = false): Member? =
        find { it.tag.equals(tag, ignoreCase) }

fun NameableEntitySet<User>.findByTag(tag: String, ignoreCase: Boolean = false): User? =
        find { it.tag.equals(tag, ignoreCase) }

val NameableEntitySet<Member>.botUser
    get() = find { it.id == it.client.botUser.id }!!

val NameableEntitySet<Role>.everyone
    get() = find { it.position == 0 && it.name == "@everyone" }
            ?: throw IllegalStateException("couldn't be found @everyone role")