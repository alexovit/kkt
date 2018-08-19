package it.alexov.kkt

import com.palantir.docker.compose.DockerComposeRule
import com.palantir.docker.compose.configuration.ProjectName
import com.palantir.docker.compose.connection.DockerPort
import com.palantir.docker.compose.connection.waiting.HealthChecks
import it.alexov.kkt.model.Person
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.junit.BeforeClass
import org.junit.ClassRule
import org.assertj.core.api.Assertions.assertThat
import java.util.*
import kotlinx.serialization.protobuf.ProtoBuf
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer

@RunWith(SpringRunner::class)
@SpringBootTest
class KafkaIntegrationTest {

    @Test
    fun testSendReceive() {
        Thread.sleep(10000)
        val senderProps = HashMap<String, Any>()
        senderProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = KAFKA_HOST
        senderProps["key.serializer"] = LongSerializer::class.java
        senderProps["value.serializer"] = ByteArraySerializer::class.java
        val pf = DefaultKafkaProducerFactory<Long, ByteArray>(senderProps)
        val template = KafkaTemplate(pf, true)
        template.defaultTopic = INPUT_TOPIC
        val data = ProtoBuf.dump(Person(1,"John Doe","john.doe@sixpack.com", "+18903456489"))
        template.sendDefault(1,data)

        val consumerProps = HashMap<String, Any>()
        consumerProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = KAFKA_HOST
        consumerProps[ConsumerConfig.GROUP_ID_CONFIG] = GROUP_NAME
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        consumerProps["key.deserializer"] = LongDeserializer::class.java
        consumerProps["value.deserializer"] = ByteArrayDeserializer::class.java
        val cf = DefaultKafkaConsumerFactory<ByteArray, ByteArray>(consumerProps)

        val consumer = cf.createConsumer()
        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC))
        val records = consumer.poll(10000)
        consumer.commitSync()

        assertThat(records.count()).isEqualTo(1)
        assertThat(records.iterator().next().value()).isEqualTo(data)
    }

    companion object {

        private val INPUT_TOPIC = "input"
        private val OUTPUT_TOPIC = "output"
        private val GROUP_NAME = "testingGroup"
        private val KAFKA_HOST = "localhost:9092"
        private val KAFKA_SERVICE = "kkt-kafka"

        @ClassRule
        @JvmField
        var docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose-kafka.yml")
            .waitingForService(KAFKA_SERVICE, HealthChecks.toHaveAllPortsOpen())
            .build()

        @BeforeClass
        @JvmStatic
        fun setup() { }
    }

}
