package it.alexov.kkt.model

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.Deserializer
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
data class Person(
    @SerialId(0) val id: Long,
    @SerialId(1) val name: String,
    @SerialId(2) val email: String,
    @SerialId(3) val phone: String
)


