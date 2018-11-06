package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.server.member.Member

class MemberSetImpl : MemberSet, NameableEntitySetImpl<Member>() {
    override fun findByTag(tag: String, ignoreCase: Boolean): Member? {
        return entities.values.find { it.tag.equals(tag, ignoreCase) }
    }
}