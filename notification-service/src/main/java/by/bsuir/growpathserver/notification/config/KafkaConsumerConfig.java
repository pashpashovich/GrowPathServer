package by.bsuir.growpathserver.notification.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import by.bsuir.growpathserver.common.model.kafka.ApplicationCreatedEvent;
import by.bsuir.growpathserver.common.model.kafka.TaskCompletedEvent;
import by.bsuir.growpathserver.common.model.kafka.UserInvitedEvent;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> baseConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserInvitedEvent> userInvitedListenerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerConfig());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserInvitedEvent.class.getName());
        DefaultKafkaConsumerFactory<String, UserInvitedEvent> cf =
                new DefaultKafkaConsumerFactory<>(props);
        ConcurrentKafkaListenerContainerFactory<String, UserInvitedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> applicationCreatedListenerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerConfig());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ApplicationCreatedEvent.class.getName());
        DefaultKafkaConsumerFactory<String, ApplicationCreatedEvent> cf =
                new DefaultKafkaConsumerFactory<>(props);
        ConcurrentKafkaListenerContainerFactory<String, ApplicationCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TaskCompletedEvent> taskCompletedListenerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerConfig());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TaskCompletedEvent.class.getName());
        DefaultKafkaConsumerFactory<String, TaskCompletedEvent> cf =
                new DefaultKafkaConsumerFactory<>(props);
        ConcurrentKafkaListenerContainerFactory<String, TaskCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
