package layers

import infra.RedisConsumer
import layers.RedisConfigLayer
import zio.ZLayer
import infra.BoardRepository
import infra.BoardEventBus

object RedisConsumerLayer:
  val layer: ZLayer[BoardRepository & BoardEventBus, Any, RedisConsumer] =
    RedisConfigLayer.layer.fresh >>> RedisConsumer.layer
