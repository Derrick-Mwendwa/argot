package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SpecValidationTest {

    @Test
    fun duplicateOptionNamesAreRejected() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(
                OptionSpec(names = listOf("--x"), converter = StringConverter),
                FlagSpec(names = listOf("--x")),
            ),
        )
        val e = assertFailsWith<IllegalArgumentException> { spec.validate() }
        assertTrue("duplicate name '--x'" in e.message!!)
    }

    @Test
    fun optionNameMustStartWithDash() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(OptionSpec(names = listOf("port"), converter = IntConverter)),
        )
        assertFailsWith<IllegalArgumentException> { spec.validate() }
    }

    @Test
    fun multiplePositionalMustBeLast() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(
                ArgumentSpec(name = "files", converter = StringConverter, multiple = true),
                ArgumentSpec(name = "dest", converter = StringConverter),
            ),
        )
        val e = assertFailsWith<IllegalArgumentException> { spec.validate() }
        assertTrue("must be declared last" in e.message!!)
    }

    @Test
    fun requiredPositionalCannotFollowOptional() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(
                ArgumentSpec(name = "a", converter = StringConverter, required = false),
                ArgumentSpec(name = "b", converter = StringConverter, required = true),
            ),
        )
        val e = assertFailsWith<IllegalArgumentException> { spec.validate() }
        assertTrue("cannot follow an optional" in e.message!!)
    }

    @Test
    fun atMostOneMultiplePositional() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(
                ArgumentSpec(name = "a", converter = StringConverter, multiple = true),
                ArgumentSpec(name = "b", converter = StringConverter, multiple = true),
            ),
        )
        assertFailsWith<IllegalArgumentException> { spec.validate() }
    }
}
