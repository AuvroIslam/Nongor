package org.nongor.app.actions

data class AssistantAction(
    val type: String = "",
    val app: String? = null,
    val query: String? = null,
    val uri: String? = null,
    val label: String? = null,
)

data class ParsedAssistantResponse(
    val text: String,
    val action: AssistantAction? = null,
)
