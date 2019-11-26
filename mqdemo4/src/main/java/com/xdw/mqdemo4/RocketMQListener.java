package com.xdw.mqdemo4;

import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

public class RocketMQListener  implements MessageListenerConcurrently {


    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt message : msgs) {
            String msg = new String(message.getBody());
            System.out.println("msg data from rocketMQ:" + msg);
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
