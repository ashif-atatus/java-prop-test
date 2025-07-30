package com.jpt.service2;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.Acknowledgment;
import java.util.Map;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "JPT", groupId = "service2-group")
    public void consume(String message, Acknowledgment ack) {
      try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(message, Map.class);

            System.out.println("🎧 Received from: " + data.get("from"));
            System.out.println("🕒 Timestamp: " + data.get("timestamp"));
            System.out.println("💬 receivedData: " + data.get("receivedData"));

            ack.acknowledge();

      } catch (Exception e) {
          System.err.println("❌ Error processing message in Service 2: " + e.getMessage());
      }
    }
}
