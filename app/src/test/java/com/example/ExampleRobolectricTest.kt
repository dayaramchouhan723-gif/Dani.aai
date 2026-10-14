package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AssistantPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dani AI", appName)
  }

  @Test
  fun `verify dani assistant personas`() {
    val gf = AssistantPersona.GF_MODE
    assertEquals("GF Mode (Kitsune / Hinglish)", gf.title)
    assertTrue(gf.promptModifier.contains("Dani"))

    val nautanki = AssistantPersona.NAUTANKI
    assertEquals("Nautanki Drama", nautanki.title)

    val hungry = AssistantPersona.HUNGRY
    assertNotNull(hungry.description)
  }
}
