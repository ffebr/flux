package layers

import infra.RedisConsumer
import layers.RedisConfigLayer

object RedisConsumerLayer:
  val layer =
    RedisConfigLayer.layer.fresh >>> RedisConsumer.layer
