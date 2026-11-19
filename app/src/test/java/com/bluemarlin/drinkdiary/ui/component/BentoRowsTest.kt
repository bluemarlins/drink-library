package com.bluemarlin.drinkdiary.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class BentoRowsTest {
    @Test
    fun `two columns pair single cells and give wide cells their own row`() {
        assertEquals(
            listOf(listOf(0), listOf(1, 2), listOf(3), listOf(4, 5)),
            bentoRows(listOf(2, 1, 1, 2, 1, 1), columns = 2),
        )
    }

    @Test
    fun `four columns pack a wide and two single cells on one row`() {
        assertEquals(
            listOf(listOf(0, 1, 2), listOf(3, 4)),
            bentoRows(listOf(2, 1, 1, 2, 2), columns = 4),
        )
    }

    @Test
    fun `a cell that does not fit starts a new row instead of overflowing`() {
        assertEquals(
            listOf(listOf(0), listOf(1)),
            bentoRows(listOf(1, 2), columns = 2),
        )
    }
}
