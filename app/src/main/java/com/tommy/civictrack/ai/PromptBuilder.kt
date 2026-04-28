package com.tommy.civictrack.ai

object PromptBuilder {

    fun buildIssuePrompt(imageUrl: String = ""): String {
        return """
            Analyze the civic issue shown in this image.
            (e.g., a pothole, overflowing garbage, or a broken streetlight).

            Return ONLY valid JSON in this exact format:
            {
              "title": "Short descriptive title",
              "description": "Brief explanation of the issue",
              "category": "pothole | garbage | streetlight",
              "priority": "High | Medium | Low"
            }

            Rules:
            - Return JSON only
            - No markdown (no ```json blocks)
            - No explanation
            - category must be exactly one of: pothole, garbage, streetlight
            - priority must be exactly one of: High, Medium, Low
        """.trimIndent()
    }
}
