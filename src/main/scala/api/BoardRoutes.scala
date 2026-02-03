package api

import zio.http._

object boardRouter:
  val api = Routes(
    Method.GET / "hello" / string("name") -> handler {
      (name: String, _: Request) =>
        Response.text(s"Hello $name")
    }
  )
