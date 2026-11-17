package com.bluemarlin.drinkdiary.domain.model

@JvmInline
value class TasteInput(
    val answers: Map<Trait, TraitAnswer> = emptyMap(),
) {
    operator fun get(trait: Trait): TraitAnswer? = answers[trait]

    fun with(
        trait: Trait,
        answer: TraitAnswer,
    ): TasteInput = TasteInput(answers + (trait to answer))
}
