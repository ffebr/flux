// For more information on writing tests, see
import munit.FunSuite
import utils.uuidShortCode
import model.BoardId
import java.util.UUID
import model.value

class uuidShortCodeSuite extends FunSuite:
  test("gen should produce a predictable short code for a  given UUID") {
    val knownUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val expectedCode = "10000000"
    val generatedID = uuidShortCode.gen(uuid = knownUuid)
    assertEquals(generatedID, BoardId(expectedCode))
  }
  test("gen should produce a code of the specified lenght") {
    val uuid = UUID.randomUUID()
    val length = 10
    val generatedID = uuidShortCode.gen(length = length, uuid = uuid)
    assertEquals(generatedID.value.length, length)
  }
