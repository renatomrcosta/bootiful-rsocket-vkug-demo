package com.example.vkugdemo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@SpringBootApplication
class VkugDemoApplication

fun main(args: Array<String>) {
    runApplication<VkugDemoApplication>(*args)
}

@Controller
class RsocketController(
    private val transactionService: TransactionService
) {
    @MessageMapping("rr")
    suspend fun requestResponse(id: UUID): Transaction {
        return transactionService.getById(id)
    }

    @MessageMapping("fnf")
    suspend fun fnf(): Unit = coroutineScope {
        withContext(Dispatchers.IO) {
            Thread.sleep(2_000)
        }

        println("Hey, I'm working")
    }

    @MessageMapping("stream")
    suspend fun stream(): Flow<Transaction> = flow {
        repeat(20) {
            emit(transactionService.getById(UUID.randomUUID()))
        }
    }

    @MessageMapping("channel")
    suspend fun stream(entryChannel: Flow<String>): Flow<String> {
        entryChannel
            .take(20)
            .collect {
                println(it)
            }

        return flow {
            while (true) {
                emit("random string")
            }
        }
    }
}

@Service
class TransactionService {
    suspend fun getById(id: UUID) = randomTransaction(id)
}

data class Transaction(
    val id: UUID,
    val description: String? = null,
    val type: String,
    val currency: String,
    val amount: BigDecimal
)

fun randomTransaction(id: UUID?) = Transaction(
    id = id ?: UUID.randomUUID(),
    description = "Description",
    type = "Type",
    currency = "EUR",
    amount = 123.0.toBigDecimal()
)
