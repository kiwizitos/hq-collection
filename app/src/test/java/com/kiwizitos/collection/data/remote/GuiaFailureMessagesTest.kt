package com.kiwizitos.collection.data.remote

import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class GuiaFailureMessagesTest {
    @Test fun distinguishesAccessAndInvalidHtml() {
        assertTrue(GuiaFailureMessages.message(GuiaAccessChallengeException()).contains("verificação"))
        assertTrue(GuiaFailureMessages.message(GuiaUnexpectedPageException()).contains("reconhecer"))
    }

    @Test fun distinguishesHttpFailures() {
        listOf(403, 404, 429, 500).forEach { code ->
            val message = GuiaFailureMessages.message(
                HttpException(Response.error<String>(code, "".toResponseBody()))
            )
            assertFalse(message.contains("Verifique sua internet"))
            assertTrue(message.isNotBlank())
        }
    }
}
