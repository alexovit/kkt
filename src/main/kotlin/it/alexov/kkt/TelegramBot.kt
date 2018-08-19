package it.alexov.kkt

import it.alexov.kkt.model.Person
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

/**
 *
 * This example bot is an echo bot that just repeats the messages sent to him
 *
 */
@Component
class TelegramBot : TelegramLongPollingBot(), KafkaConnectorListener {

    @Value("\${bot.token}")
    private val token: String? = null

    @Value("\${bot.chat-id}")
    private val chatId: String? = null

    @Value("\${bot.username}")
    private val username: String? = null

    override fun getBotToken(): String? {
        return token
    }

    override fun getBotUsername(): String? {
        return username
    }

    @Autowired
    lateinit var kafkaConnector: KafkaConnector

    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        logger.info("Telegram bot started")
        logger.info("username: {}, token: {}", username, token)
        kafkaConnector.addListener(this)
    }

    override fun onClosing() {
        kafkaConnector.removeListener(this)
        super.onClosing()
        logger.info("bot closing")
    }

    fun getMessage(person: Person): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = chatId
        sendMessage.text = "${person.name} - ${person.email} - ${person.phone}"
        return sendMessage
    }

    override fun onUpdateReceived(update: Update?) { }

    override fun personMessage(id: Long, person: Person) {
        if(chatId != null) {
            try {
                execute(getMessage(person))
                kafkaConnector.send("output",person)
            } catch (e: TelegramApiException) {
                e.printStackTrace()
                logger.error("")
                kafkaConnector.send("errors",person)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TelegramBot::class.java)
    }

}
