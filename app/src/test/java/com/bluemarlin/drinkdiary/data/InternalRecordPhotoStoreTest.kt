package com.bluemarlin.drinkdiary.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.local.InternalRecordPhotoStore
import com.bluemarlin.drinkdiary.domain.model.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class InternalRecordPhotoStoreTest {
    private lateinit var context: Context
    private lateinit var store: InternalRecordPhotoStore
    private val bytes = byteArrayOf(1, 2, 3, 4, 5)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        store = InternalRecordPhotoStore(context)
        File(context.filesDir, "record_photos").deleteRecursively()
    }

    private fun sourceFile(): File = File(context.cacheDir, "picked.jpg").apply { writeBytes(bytes) }

    // 사진 선택기 주소는 앱이 재시작되면 읽을 수 없다. 기록에는 앱이 소유한 사본의 주소가 들어가야 한다.
    @Test
    fun `import copies the picked photo into app storage`() =
        runBlocking {
            val result = store.import(Uri.fromFile(sourceFile()).toString())

            val stored = File(Uri.parse((result as AppResult.Success).value).path!!)
            assertEquals(File(context.filesDir, "record_photos").canonicalPath, stored.parentFile!!.canonicalPath)
            assertArrayEquals(bytes, stored.readBytes())
            assertEquals(listOf(result.value), store.listStored().map { it.uri })
        }

    @Test
    fun `a source that cannot be read fails without leaving a file behind`() =
        runBlocking {
            val result = store.import(Uri.fromFile(File(context.cacheDir, "missing.jpg")).toString())

            assertTrue(result is AppResult.Failure)
            assertEquals(emptyList<Any>(), store.listStored())
        }

    @Test
    fun `delete removes stored copies but never files outside its folder`() =
        runBlocking {
            val outside = sourceFile()
            val stored = (store.import(Uri.fromFile(outside).toString()) as AppResult.Success).value

            store.delete(Uri.fromFile(outside).toString())
            store.delete(stored)

            assertTrue(outside.exists())
            assertEquals(emptyList<Any>(), store.listStored())
        }
}
