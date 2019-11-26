package com.xdw.mqdemo4;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.UUID;

public class RocketMQProducerTest {

    public static void main(String[] args) {
        String nameServer = "127.0.0.1:9876";   //nameServer
        String topics = "MQ-DEMO4-TEST";        //主题
        String producerMqGroupName = "PRODUCER-MQ-GROUP";    //生产者者集群名称，不配置的话默认为DEFAULT_PRODUCER
        DefaultMQProducer sender = new DefaultMQProducer(producerMqGroupName);  //创建生产者
        sender.setNamesrvAddr(nameServer);    //设置生产者的nameServer
        sender.setInstanceName(UUID.randomUUID().toString());  //设置生产者的实例名称
        try {
            sender.start();   //启动生产者
        } catch (MQClientException e) {
            e.printStackTrace();
        }

        //构建多个消息，生产者发送消息
        for (int i = 0; i < 5; i++) {
            Message message = new Message();  //创建消息
            message.setTopic(topics);  //设置消息主题
            message.setBody(("I send message to RocketMQ " + i).getBytes());   //设置消息内容
            try {
                SendResult result = sender.send(message);  //生产者发送消息
                SendStatus status = result.getSendStatus();  //获取消息发送状态
                System.out.println("messageId=" + result.getMsgId() + ", status=" + status);  //打印消息ID和状态
            } catch (MQClientException e) {
                e.printStackTrace();
            } catch (RemotingException e) {
                e.printStackTrace();
            } catch (MQBrokerException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
