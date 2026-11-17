package com.bluemarlin.drinkdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TraitTest {
    @Test
    fun `wine and whiskey share four traits and differ in their own`() {
        val wine = Trait.of(DrinkType.Wine)
        val whiskey = Trait.of(DrinkType.Whiskey)

        assertEquals(Trait.shared, wine.filter { it.shared })
        assertEquals(Trait.shared, whiskey.filter { it.shared })
        assertEquals(listOf(Trait.Acidity, Trait.Tannin), wine.filterNot { it.shared })
        assertEquals(listOf(Trait.Peat, Trait.AlcoholBurn), whiskey.filterNot { it.shared })
    }

    // 순서값은 상관 계산의 x축이다. 뒤집히면 모든 판정 방향이 뒤집힌다.
    @Test
    fun `answer levels are ordered low to high`() {
        assertEquals(0, TraitAnswer.Low.level)
        assertEquals(1, TraitAnswer.Mid.level)
        assertEquals(2, TraitAnswer.High.level)
    }

    @Test
    fun `mid answers are kept like any other answer`() {
        val input =
            TasteInput()
                .with(Trait.Body, TraitAnswer.High)
                .with(Trait.Sweetness, TraitAnswer.Mid)
                .with(Trait.Intensity, TraitAnswer.Low)

        // '보통'도 답이다 — 그대로 남아 판정에 쓰인다.
        assertEquals(3, input.answers.size)
        assertEquals(TraitAnswer.Mid, input[Trait.Sweetness])
    }
}
