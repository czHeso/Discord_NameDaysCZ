import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.awt.Color
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

suspend fun waitForTime(targetTime: LocalTime) {
    while (LocalTime.now() != targetTime) {
        delay(1000) // počkej 1 sekundu před opakováním
    }
}


suspend fun main() = runBlocking {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    val url = "https://svatkyapi.cz/api/day/"
    val response: HttpResponse = client.get(url)
    println(response)


    @Serializable
    data class Svatek(
        val date: String,
        val name: String,
        val monthNumber: Int?,
        val dayNumber: Int?,
        val isHoliday: Boolean?,
        val holidayName: String?
    )

    var kdoMaSvatek = client.get(url).body<Svatek>()
    println(kdoMaSvatek)

    val token = "INSERT_YOUR_TOKEN"

    val currentDateTime: java.util.Date = java.util.Date()
    println(currentDateTime)




    val targetTime = LocalTime.parse("08:00").toString()

    val channelIds = listOf("977874654737879070", "1147477222588960789", "1168636911015952484")

    while(true)
    {
        Thread.sleep(59000)
        val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")).toString()
        println(timeNow)
        if(timeNow == targetTime){

            val jda = JDABuilder.createDefault(token)
                .addEventListeners(object : ListenerAdapter() {
                    override fun onReady(event: ReadyEvent) {
                        super.onReady(event)

                        for (channelId in channelIds) {
                            val targetChannel = event.jda.getTextChannelById(channelId)

                            val embed = EmbedBuilder()
                                .setTitle("DNEŠNÍ SVÁTEK")
                                .setDescription("Dneska má svátek ${kdoMaSvatek.name}")
                                .setColor(Color.ORANGE)

                            if (kdoMaSvatek.isHoliday == true) {
                                embed.addField("Navíc je dneska", "${kdoMaSvatek.holidayName}", false)
                            } else {
                                println("Nic")
                            }

                            targetChannel?.sendMessageEmbeds(embed.build())?.queue()
                        }
                    }
                })
                .build()

            jda.awaitReady()

        }


    }


}