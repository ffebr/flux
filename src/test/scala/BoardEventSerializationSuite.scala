import munit.FunSuite
import zio.json.*
import model.*

class BoardEventSerializationSuite extends FunSuite:
  test("serialize and deserialize WordAdded") {
    val event: BoardEvent = WordAdded(BoardId("123"), Word("test"))
    val json = event.toJson
    val decoded = json.fromJson[BoardEvent]
    assertEquals(decoded, Right(event))
  }

  test("serialize and deserialize BoardCreated as BoardEvent") {
    val event: BoardEvent = BoardCreated(BoardId("123"))
    val json = event.toJson
    val decoded = json.fromJson[BoardEvent]
    assertEquals(decoded, Right(event))
  }

  test(
    "serialize as BoardCreated but deserialize as BoardEvent (simulates the bug)"
  ) {
    val bc = BoardCreated(BoardId("123"))
    val json = bc.toJson // Serializes as BoardCreated, not BoardEvent!
    val decoded = json.fromJson[BoardEvent]
    // This will fail because the disambiguator is missing!
    assert(decoded.isLeft)
    assertEquals(decoded, Left("(invalid disambiguator)"))
  }
