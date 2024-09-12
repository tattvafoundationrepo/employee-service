package digit.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class Consumer {

    @KafkaListener(topics = {"${employee-service-create-topic}"})
    public void listen(final HashMap<String, Object> record) {



    }
}
