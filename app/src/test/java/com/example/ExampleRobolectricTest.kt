package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MarkdownTableRepository
import com.example.model.DatabaseEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Main Database", appName)
  }

  @Test
  fun `test markdown table parsing and column expansion`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = MarkdownTableRepository(context)

    val singleColMarkdown = """
      | Names |
      | --- |
      | Alpha |
      | Omega |
    """.trimIndent()

    val parsed = repo.parseMarkdown(singleColMarkdown)
    assertEquals(2, parsed.entries.size)
    assertEquals("Alpha", parsed.entries[0].name)
    assertEquals("Omega", parsed.entries[1].name)

    // Now simulate user adding ID, Description, Stats, and Tags
    val updatedEntries = listOf(
      parsed.entries[0].copy(id = "001", description = "Scout unit", stats = "SPD: 100", tags = listOf("scout", "agile")),
      parsed.entries[1].copy(id = "002", description = "Supreme guardian", stats = "ATK: 99", tags = listOf("boss"))
    )

    val serialized = repo.serializeMarkdown(
      columns = listOf("Names", "ID", "Description", "Stats", "Tags"),
      entries = updatedEntries
    )

    assertTrue(serialized.contains("| Names | ID | Description | Stats | Tags |"))
    assertTrue(serialized.contains("| Alpha | 001 | Scout unit | SPD: 100 | scout, agile |"))
    assertTrue(serialized.contains("| Omega | 002 | Supreme guardian | ATK: 99 | boss |"))
  }
}

