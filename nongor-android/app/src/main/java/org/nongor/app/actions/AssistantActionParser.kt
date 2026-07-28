package org.nongor.app.actions

import com.google.gson.Gson

object AssistantActionParser {

    private val gson = Gson()
    private val actionRegex = Regex("<app_action>(.*?)</app_action>", setOf(RegexOption.DOT_MATCHES_ALL))

    fun parse(raw: String): ParsedAssistantResponse {
        val match = actionRegex.find(raw)
        val action = match?.groups?.get(1)?.value?.trim()?.let(::parseAction)
        val cleaned = raw.replace(actionRegex, "").trim().ifBlank {
            action?.label ?: ""
        }
        return ParsedAssistantResponse(text = cleaned, action = action)
    }

    private fun parseAction(json: String): AssistantAction? =
        runCatching { gson.fromJson(json, AssistantAction::class.java) }
            .getOrNull()
            ?.normalize()
            ?.takeIf { it.type.isNotBlank() }

    private fun AssistantAction.normalize(): AssistantAction =
        copy(
            type = type.trim().lowercase(),
            app = app?.trim()?.lowercase(),
            query = query?.trim()?.ifBlank { null },
            uri = uri?.trim()?.ifBlank { null },
            label = label?.trim()?.ifBlank { null },
        )
}
