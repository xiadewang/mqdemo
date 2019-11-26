# 消息队列（MQ）系列详解之四--RocketMQ入门代码demo
## 重要名词和API
### Producer
消息队列的生产者，需要与NameServer建立连接，从NameServer获取Topic路由信息，并向提供Topic服务的Broker Master建立连接；Producer无状态，看集群部署；

### Consumer
消息队列的消费者，同样与NameServer建立连接，从NameServer获取Topic路由信息，并向提供Topic服务的Broker Master，Slave建立连接；

### Message
顾名思义，消息。即生产者发送的消息。
### Topic
Topic是生产者在发送消息和消费者在拉取消息的类别。Topic与生产者和消费者之间的关系非常松散。具体来说，一个Topic可能有0个，一个或多个生产者向它发送消息；相反，一个生产者可以发送不同类型Topic的消息。类似的，消费者组可以订阅一个或多个主题，只要该组的实例保持其订阅一致即可。
Topic在Google翻译中解释为话题。我们可以理解为第一级消息类型，类比于书的标题。在应用系统中，一个Topic标识为一类消息类型，比如交易信息。
 在Producer中使用Topic：
```java
 Message msg = new Message("交易信息" /* Topic */, "交易创建",("orderid= " + orderid).getBytes(RemotingHelper.DEFAULT_CHARSET));
```
在Consumer中订阅Topic：
```java
consumer.subscribe("交易信息", "*");
```
### Tag
标签，换句话的意思就是子主题，为用户提供了额外的灵活性。有了标签，来自同一业务模块的具有不同目的的消息可以具有相同的主题和不同的标记。标签有助于保持代码的清晰和连贯，同时标签也方便RocketMQ提供的查询功能。
Tag在Google翻译中解释为标签。我们可以理解为第二级消息类型，类比于书的目录，方便检索使用消息。在应用系统中，一个Tag标识为一类消息中的二级分类，比如交易信息下的交易创建、交易完成。
在Producer中使用Tag：
```java
 Message msg = new Message("交易信息" /* Topic */, "交易创建"  /* Tag */,("orderid= " + orderid).getBytes(RemotingHelper.DEFAULT_CHARSET));
```
在Consumer中订阅Tag：
```java
consumer.subscribe("交易信息", "交易创建||交易完成");// * 代表订阅Topic下的所有消息
```
### GroupName
和现实世界中一样，RocketMQ中也有组的概念。代表具有相同角色的生产者组合或消费者组合，称为生产者组或消费者组。

作用是在集群HA的情况下，一个生产者down之后，本地事务回滚后，可以继续联系该组下的另外一个生产者实例，不至于导致业务走不下去。在消费者组中，可以实现消息消费的负载均衡和消息容错目标。
 另外，有了GroupName，在集群下，动态扩展容量很方便。只需要在新加的机器中，配置相同的GroupName。启动后，就立即能加入到所在的群组中，参与消息生产或消费。
在Producer中使用GroupName：
```java
DefaultMQProducer producer = new DefaultMQProducer("group_name_1");// 使用GroupName来初始化Producer，如果不指定，就会使用默认的名字：DEFAULT_PRODUCER
```
  在Consumer中使用GroupName：
```java
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group_name_1");// 使用GroupName来初始化Consumer，如果不指定，就会使用默认的名字：DEFAULT_CONSUMER
```
RocketMQ使用Producer、Consumer、Message、Topic、Tag和GroupName简单的几个概念，就能实现相关功能了，下面我们开始编写代码进行测试吧。

## 代码编写
### pom依赖
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.rocketmq</groupId>
            <artifactId>rocketmq-client</artifactId>
            <version>4.5.2</version>
        </dependency>
    </dependencies>
```

###  创建生产者测试类RocketMQProducerTest
```java
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

```
###  创建消息消费监听器RocketMQListener
```java
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
```

### 创建消费者测试类RocketMQConsumerTest
```java
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
```
