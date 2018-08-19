package it.alexov.kkt.configs

import it.alexov.kkt.model.Person
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.Serializer
import org.springframework.beans.factory.annotation.Value

@Configuration
class KafkaConfig {

    class PersonSerializer: Serializer<Person> {
        override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        }
        override fun serialize(topic: String?, data: Person?): ByteArray {
            return ProtoBuf.dump(data!!)
        }
        override fun close() {
        }
    }

    class PersonDeserializer: Deserializer<Person> {
        override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        }
        override fun deserialize(topic: String?, data: ByteArray?): Person {
            return ProtoBuf.load(data!!)
        }
        override fun close() {
        }
    }

    @Value("\${kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean
    fun configs(): Map<String, Any> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        return props
    }

    @Bean
    fun producerFactory(): ProducerFactory<Long, Person> {
        return DefaultKafkaProducerFactory(configs(), LongSerializer(), PersonSerializer())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<Long, Person> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<Long, Person> {
        return DefaultKafkaConsumerFactory(configs(), LongDeserializer(), PersonDeserializer())
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Long, Person> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, Person>()
        factory.consumerFactory = consumerFactory()
        return factory
    }

}