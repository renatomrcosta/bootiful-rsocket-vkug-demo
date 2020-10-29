package com.example.vkugdemo

import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveAndAwait
import org.springframework.messaging.rsocket.retrieveFlow
import java.util.UUID

@SpringBootTest
internal class RsocketControllerTest {

    @Autowired
    private lateinit var builder: RSocketRequester.Builder

    @Value("\${spring.rsocket.server.port}")
    private var port: Int = 0

    @Test
    fun requestResponse() {
        runBlocking {
            val requester = builder
                .connectTcp("localhost", port)
                .block()!!

            try {
                val uuid = UUID.randomUUID()
                val result = requester
                    .route("rr")
                    .data(uuid)
                    .retrieveAndAwait<Transaction>()

                Assertions.assertEquals(result.id, uuid)
            } finally {
                requester.rsocket()?.dispose()
            }
        }
    }

    @Test
    fun channel() {
        runBlocking {
            val requester = builder
                .connectTcp("localhost", port)
                .block()!!

            val entryFlow = flow {
                while (true) {
                    emit("my funny string")
                }
            }

            try {
                requester
                    .route("channel")
                    .data(entryFlow)
                    .retrieveFlow<String>()
                    .buffer(20)
                    .take(10)
                    .transform { result ->
                        repeat(5) {
                            emit(result)
                        }
                    }
                    .collect {
                        println(it)
                    }
            } finally {
                requester.rsocket()?.dispose()
            }
        }
    }
}
