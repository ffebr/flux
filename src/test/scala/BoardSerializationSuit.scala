import model.Board
import model.BoardId
import model.BoardResponse
import model.Word
import model.WordCount
import munit.FunSuite
import zio.json.*

import java.time.Instant

class BoardSerializationSuit extends FunSuite:
  test("serialize board matches expected JSON") {
    val testBoardWithWords: Board =
      Board(
        id = BoardId("test-123"),
        question = "Best Idea?",
        expiresAt = Instant.parse("2026-02-15T16:29:54.832009500Z"),
        words = Map(
          Word("scala") -> WordCount(3),
          Word("zio") -> WordCount(5)
        )
      )

    val expectedJson =
      """
         |{
         |  "id": "test-123",
         |  "question": "Best Idea?",
         |"expiresAt":"2026-02-15T16:29:54.832009500Z",
         |  "words": {
         |    "scala": 3,
         |    "zio": 5
         |  }
         |}
       """.stripMargin.replaceAll("\\s", "")

    val serializedJson =
      BoardResponse.fromBoard(testBoardWithWords).toJson.replaceAll("\\s", "")
    assertEquals(expectedJson, serializedJson)
  }

  test("serialize and deserialize board correctly") {
    val testBoardWithWords: Board =
      Board(
        id = BoardId("test-123"),
        question = "Best Idea?",
        expiresAt = Instant.parse("2026-02-15T16:29:54.832009500Z"),
        words = Map(
          Word("scala") -> WordCount(3),
          Word("zio") -> WordCount(5)
        )
      )

    val json = testBoardWithWords.toJson
    val decoded = json.fromJson[Board]
    assertEquals(
      decoded,
      Right(testBoardWithWords)
    )
  }
