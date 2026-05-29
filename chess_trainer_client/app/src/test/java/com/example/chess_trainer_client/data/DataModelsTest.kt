package com.example.chess_trainer_client.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class DataModelsTest {
    private val gson = Gson()

    @Test
    fun puzzleDeserializationWorks() {
        val json = """
            {
              "id": "p1",
              "fen": "8/8/8/8/8/8/8/8",
              "solution": ["e2e4"],
              "description": "Test puzzle",
              "difficulty": "easy"
            }
        """.trimIndent()

        val puzzle = gson.fromJson(json, Puzzle::class.java)

        assertEquals("p1", puzzle.id)
        assertEquals("8/8/8/8/8/8/8/8", puzzle.fen)
        assertEquals(listOf("e2e4"), puzzle.solution)
        assertEquals("Test puzzle", puzzle.description)
        assertEquals("easy", puzzle.difficulty)
    }

    @Test
    fun openingDeserializationWorks() {
        val json = """
            {
              "id": "o1",
              "name": "Ruy Lopez",
              "moves": ["e2e4", "e7e5", "g1f3"],
              "description": "Classic opening"
            }
        """.trimIndent()

        val opening = gson.fromJson(json, Opening::class.java)

        assertEquals("o1", opening.id)
        assertEquals("Ruy Lopez", opening.name)
        assertEquals(listOf("e2e4", "e7e5", "g1f3"), opening.moves)
        assertEquals("Classic opening", opening.description)
    }

    @Test
    fun aiMoveModelsDeserialize() {
        val requestJson = """
            {"fen": "8/8/8/8/8/8/8/8"}
        """.trimIndent()
        val responseJson = """
            {"move": "e2e4"}
        """.trimIndent()

        val request = gson.fromJson(requestJson, AiMoveRequest::class.java)
        val response = gson.fromJson(responseJson, AiMoveResponse::class.java)

        assertEquals("8/8/8/8/8/8/8/8", request.fen)
        assertEquals("e2e4", response.move)
    }
}

