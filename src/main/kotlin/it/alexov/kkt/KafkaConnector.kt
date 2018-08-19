package it.alexov.kkt

import it.alexov.kkt.model.Person
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

interface KafkaConnectorListener {
    fun personMessage(id: Long, person: Person)
}

@Component
class KafkaConnector {

    val listeners = mutableListOf<KafkaConnectorListener>()

    fun addListener(listener: KafkaConnectorListener) {
        listeners += listener
    }

    fun removeListener(listener: KafkaConnectorListener) {
        listeners -= listener
    }

    @Suppress("SpringKotlinAutowiring")
    @Autowired
    @Qualifier("kafkaTemplate")
    lateinit var kafkaTemplate: KafkaTemplate<Long, Person>

    fun send(topic: String, person: Person) {
        log.info("sending message \"$person\" back to kafka")
        kafkaTemplate.send(topic, person.id, person)
    }

    @KafkaListener(topics = ["input"], containerFactory = "kafkaListenerContainerFactory")
    fun listen(consumerRecord: ConsumerRecord<Long?, Person?>) {
        if(consumerRecord.value() != null) {
            val key = consumerRecord.key()!!
            val value = consumerRecord.value()!!
            log.info("received from kafka: \"$value\"")
            listeners.forEach { listener -> listener.personMessage(key, value) }
        } else {
            log.error("bad record returned from kafka \"$consumerRecord\"")
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(KafkaConnector::class.java)
    }
}