package layers

import zio.ZLayer
import zio.Ref
import model.BoardEvent
import model.BoardId
import zio.Hub

object BoardStateLayer:
  // val boardRefLayer = ZLayer.fromZIO(Ref.make(Map.empty[BoardId, BoardEvent]))
  val boardHubLayer = ZLayer.scoped(Hub.unbounded[BoardEvent])

  val layer = boardHubLayer
