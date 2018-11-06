package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.server.member.Member

interface MemberSet : NameableEntitySet<Member>, IterableEntitySet<Member> {
    fun findByTag(tag: String, ignoreCase: Boolean = false): Member?
}