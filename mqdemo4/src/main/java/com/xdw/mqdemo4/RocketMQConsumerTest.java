package com.xdw.mqdemo4;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

import java.util.UUID;

public class RocketMQConsumerTest {
    public static void main(String[] args) {
        String nameServer = "127.0.0.1:9876";  //nameServer
        String topics = "MQ-DEMO4-TEST";  //主题
        String consumerMqGroupName = "CONSUMER-MQ-GROUP";  //消费者集群名称，不配置的话默认为DEFAULT_CONSUMER
        RocketMQListener mqListener = new RocketMQListener();  //创建消息消费监听实例
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerMqGroupName);  //创建消费者
        consumer.setNamesrvAddr(nameServer);  //设置消费者的nameServer
        try {
            consumer.subscribe(topics, "*");  //消费者通过主题和tag（子主题）订阅消息，此处*代表匹配所有tag
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        consumer.setInstanceName(UUID.randomUUID().toString());  //设置消费者实例名称
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);  //设置消费的起始点，默认为CONSUME_FROM_LAST_OFFSET
        consumer.registerMessageListener((MessageListenerConcurrently) mqListener);  //绑定消息消费监听器

        try {
            consumer.start();  //启动消费
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }
}
