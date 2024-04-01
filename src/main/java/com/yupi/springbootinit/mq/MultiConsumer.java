package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {

  private static final String TASK_QUEUE_NAME = "multi";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();

      for (int i = 0; i < 2; i++) {
          final Channel channel = connection.createChannel();

          channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

          // 一个队列最多同时接受1个未确认的消息
          channel.basicQos(1);
          // 如何处理消息
          int finalI = i;
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
              String message = new String(delivery.getBody(), "UTF-8");


              try {
                  // 处理工作
                  System.out.println(" [x] Received '" + "编号" + finalI + "的队列接受到了" +  message + "'");
                  Thread.sleep(10000);
              } catch (InterruptedException e) {
                  throw new RuntimeException(e);
              } finally {
                  System.out.println(" [x] Done");
                  channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
              }
          };
          channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
      }

  }

}