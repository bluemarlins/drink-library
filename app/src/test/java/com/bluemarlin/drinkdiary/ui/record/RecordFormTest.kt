package com.bluemarlin.drinkdiary.ui.record

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// 저장 버튼이 꺼져 있으면 무엇을 채워야 하는지 알려 줘야 한다. 예전에는 이름만 안내해서,
// 이름을 넣고도 별점이 0이면 이유 없이 버튼이 꺼져 있었다.
class RecordFormTest {
    @Test
    fun `names exactly what is still missing`() {
        assertEquals("이름과 별점을 남기면 저장할 수 있어요", RecordForm().missingForSave)
        assertEquals("이름을 입력하면 저장할 수 있어요", RecordForm(rating = 3.5).missingForSave)
        assertEquals("별점을 남기면 저장할 수 있어요", RecordForm(name = "Barolo").missingForSave)
        assertEquals("이름을 입력하면 저장할 수 있어요", RecordForm(name = "  ", rating = 3.5).missingForSave)
    }

    @Test
    fun `a named and rated form can be saved and has nothing to explain`() {
        val form = RecordForm(name = "Barolo", rating = 0.5)

        assertTrue(form.isSavable)
        assertNull(form.missingForSave)
        assertFalse(RecordForm(name = "Barolo").isSavable)
    }
}
