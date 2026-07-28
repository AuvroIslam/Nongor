package org.nongor.app.actions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantActionParserTest {

    @Test fun extractsActionAndStripsTagFromText() {
        val raw = "Sure, opening that now. " +
            "<app_action>{\"type\":\"open_app\",\"app\":\"Maps\",\"query\":\"shelter\"}</app_action>"
        val parsed = AssistantActionParser.parse(raw)
        assertEquals("Sure, opening that now.", parsed.text)
        assertEquals("open_app", parsed.action?.type)
        assertEquals("maps", parsed.action?.app)          // normalized to lowercase
        assertEquals("shelter", parsed.action?.query)
    }

    @Test fun noTagMeansNoAction() {
        val parsed = AssistantActionParser.parse("Just a normal reply with no action.")
        assertNull("no action tag -> null action", parsed.action)
        assertEquals("Just a normal reply with no action.", parsed.text)
    }

    @Test fun malformedActionJsonIsIgnored() {
        val parsed = AssistantActionParser.parse("text <app_action>{not valid json}</app_action>")
        assertNull("malformed JSON -> null action, not a crash", parsed.action)
    }

    @Test fun actionWithBlankTypeIsRejected() {
        val parsed = AssistantActionParser.parse("<app_action>{\"app\":\"maps\"}</app_action>")
        assertNull("action without a type is dropped", parsed.action)
    }

    @Test fun textFallsBackToLabelWhenOnlyTagPresent() {
        val raw = "<app_action>{\"type\":\"open_url\",\"uri\":\"https://who.int\"," +
            "\"label\":\"Open WHO\"}</app_action>"
        val parsed = AssistantActionParser.parse(raw)
        assertTrue("empty surrounding text falls back to the label", parsed.text.isNotEmpty())
        assertEquals("open_url", parsed.action?.type)
    }
}
