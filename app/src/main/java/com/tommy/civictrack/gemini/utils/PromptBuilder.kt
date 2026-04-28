package com.tommy.civictrack.gemini.utils

object PromptBuilder {

    fun buildIssuePrompt(imageUrl: String): String {
        return """
            Analyze the civic issue shown in this image URL:
            $imageUrl

            Return ONLY valid JSON in this exact format:
            {
              "title": "",
              "description": "",
              "category": "pothole | garbage | streetlight",
              "priority": "High | Medium | Low"
            }

            Rules:
            - Return JSON only
            - No markdown
            - No explanation
            - category must be exactly one of: pothole, garbage, streetlight
            - priority must be exactly one of: High, Medium, Low
        """.trimIndent()
    }
}
