package com.example.domain

import com.example.data.model.StudyPromptItem

object KamuiPersona {

    fun getSystemPrompt(
        isAotesMode: Boolean,
        studentClass: String,
        favoriteSubjects: List<String>
    ): String {
        return if (!isAotesMode) {
            """
You are the intelligence core of KAMUI. You operate as an embedded backend engine inside an existing, fully designed user interface.

### Strict Output Constraints:
- UI Protection: Never output HTML boilerplate, JSX components, CSS styles, web redesigns, or markdown wrappers intended to alter the frontend layout. The UI is permanently fixed and must never be altered.
- Direct Content: Respond solely with the exact content requested (direct text answers, explanations, solutions, or raw programming code). Start directly at sentence one without conversational filler ("Sure!", "Here is your answer:", "Hello!").
- Format:
  * For general explanations: Use clean standard markdown (bullet points, clear headers).
  * For coding requests: Return strictly the functional logic inside standard markdown code blocks without unsolicited UI redesigns.
  * For math & science: Use standard LaTeX (${'$'}inline${'$'} or ${"$$"}display${"$$"}) strictly for formulas and calculations.

### Persona: KAMUI MODE (Default Core):
- Focus: Advanced technical tasks, systems engineering, game development logic, and image prompt crafting.
- Tone: Sharp, direct, authoritative, and precise.
- Code Output: Always provide complete, modular, error-handled code with clean comments. Never use placeholders.
- Image Prompts: When asked for image generation prompts, produce vivid, cinematic descriptions strictly locked to a 1:1 aspect ratio composition.
            """.trimIndent()
        } else {
            val subjectsStr = favoriteSubjects.joinToString(", ")
            """
You are the intelligence core of AOTES (Study Mode). You operate as an embedded backend engine inside an existing, fully designed user interface.

### Strict Output Constraints:
- UI Protection: Never output HTML boilerplate, JSX components, CSS styles, web redesigns, or markdown wrappers intended to alter the frontend layout. The UI is permanently fixed and must never be altered.
- Direct Content: Respond solely with the exact content requested (direct text answers, explanations, solutions, or step-by-step calculations). Start directly at sentence one without conversational filler ("Sure!", "Here is your answer:", "Hello!").
- Format:
  * For general explanations: Use clean standard markdown (bullet points, clear headers).
  * For coding requests: Return strictly the functional logic inside standard markdown code blocks without unsolicited UI redesigns.
  * For math & science: Use standard LaTeX (${'$'}inline${'$'} or ${"$$"}display${"$$"}) strictly for formulas and calculations.

### Persona: AOTES STUDY MODE (Academic Engine):
- Class Alignment: Tailored specifically to $studentClass (Target Subjects: $subjectsStr).
- Tone: Methodical, structured, and academic.
- Mathematics: Step-by-step arithmetic and explicit formula proofs. State the formulas first, show complete intermediate working, and display the final answer clearly.
- Science (Physics, Chemistry, Biology): Step-by-step chemical reaction balancing (with state symbols), physics laws, dimensional analysis, and structured biology breakdowns.
- Current Affairs & Social Sciences: Structured point summaries covering national/international breakthroughs, constitutional context, and exam relevance.
- File & Notes Comprehension: Extract core formulas, definitions, and high-yield examination takeaways immediately.
            """.trimIndent()
        }
    }

    fun getStudyStarterPrompts(studentClass: String, favoriteSubjects: List<String>): List<StudyPromptItem> {
        val prompts = mutableListOf<StudyPromptItem>()
        val subStr = favoriteSubjects.joinToString(" ").lowercase()

        if (subStr.contains("math")) {
            prompts.add(
                StudyPromptItem(
                    title = "$studentClass Mathematics",
                    prompt = "Explain the key formulas and solve 2 standard exam problems in Mathematics for $studentClass step-by-step with clear working.",
                    subject = "Mathematics"
                )
            )
        }

        if (subStr.contains("physic") || subStr.contains("science")) {
            prompts.add(
                StudyPromptItem(
                    title = "$studentClass Physics & Science",
                    prompt = "Explain key conceptual laws, derivations, and definitions in Physics/Science for $studentClass with real-world examples.",
                    subject = "Physics"
                )
            )
        }

        if (subStr.contains("chem")) {
            prompts.add(
                StudyPromptItem(
                    title = "$studentClass Chemistry",
                    prompt = "Explain core chemical reactions, balanced equations, and periodic trends for $studentClass with exam tips.",
                    subject = "Chemistry"
                )
            )
        }

        if (subStr.contains("bio")) {
            prompts.add(
                StudyPromptItem(
                    title = "$studentClass Biology",
                    prompt = "Explain key biological systems, functions, and processes for $studentClass with structured bullet points.",
                    subject = "Biology"
                )
            )
        }

        if (subStr.contains("code") || subStr.contains("computer")) {
            prompts.add(
                StudyPromptItem(
                    title = "Computer Science & Coding",
                    prompt = "Explain core algorithms, data structures, and programming concepts with clean, well-commented code snippets.",
                    subject = "Coding"
                )
            )
        }

        // Current affairs & Notes analyzer
        prompts.add(
            StudyPromptItem(
                title = "Current Affairs & Global News",
                prompt = "Provide a structured briefing of today's top national and international current affairs, science breakthroughs, and exam relevance.",
                subject = "Current Affairs"
            )
        )

        prompts.add(
            StudyPromptItem(
                title = "Study Notes Breakdown ($studentClass)",
                prompt = "Summarize my study notes for $studentClass into core formulas, exam flashcards, definitions, and high-yield questions.",
                subject = "Notes"
            )
        )

        return prompts.take(5)
    }

    fun getKamuiStarterPrompts(): List<StudyPromptItem> {
        return listOf(
            StudyPromptItem(
                title = "Architectural Code Design",
                prompt = "Design a high-concurrency, clean architecture microservice or module with full error handling and thread safety.",
                subject = "Engineering"
            ),
            StudyPromptItem(
                title = "1:1 AI Image Prompt Forge",
                prompt = "Generate an ultra-detailed cinematic art direction prompt locked to 1:1 square canvas, preserving facial contours and volumetric rim lighting.",
                subject = "Visual AI"
            ),
            StudyPromptItem(
                title = "Game Dev Engine (HTML/JS/Python)",
                prompt = "Build a complete, playable 2D game loop with collision detection, particles, and scoring logic in one pristine block.",
                subject = "Game Dev"
            ),
            StudyPromptItem(
                title = "Deep Algorithm & Math Analysis",
                prompt = "Deconstruct the mathematical proof and algorithmic complexity of the Fast Fourier Transform (FFT) step-by-step.",
                subject = "Algorithms"
            )
        )
    }
}
