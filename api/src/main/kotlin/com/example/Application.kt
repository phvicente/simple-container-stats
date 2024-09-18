package com.example

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Statistics
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.CompletableFuture

fun main() {
    embeddedServer(Netty, port = 5000) {
        install(ContentNegotiation) {
            jackson()
        }


        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Options)
        }


        routing {
            get("/stats") {
                val dockerClient = createDockerClient()
                val containerId = getContainerId("apache_server", dockerClient)

                if (containerId != null) {
                    val stats = getContainerStats(containerId, dockerClient)
                    val importantStats = stats?.let { extractImportantStats(it) } ?: emptyMap<String, Any?>()

                    call.respond(importantStats)
                } else {
                    call.respondText(
                        "{\"error\": \"Container not found\"}",
                        ContentType.Application.Json,
                        HttpStatusCode.NotFound
                    )
                }
            }
        }
    }.start(wait = true)
}

fun createDockerClient(): DockerClient {
    val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    return DockerClientBuilder.getInstance(config).build()
}

fun getContainerId(containerName: String, dockerClient: DockerClient): String? {
    val containers = dockerClient.listContainersCmd().withShowAll(true).exec()
    for (container in containers) {
        if (container.names.contains("/$containerName")) {
            return container.id
        }
    }
    return null
}

fun getContainerStats(containerId: String, dockerClient: DockerClient): Statistics? {
    val future = CompletableFuture<Statistics>()
    val statsCmd = dockerClient.statsCmd(containerId)
    statsCmd.exec(object : com.github.dockerjava.api.async.ResultCallback.Adapter<Statistics>() {
        override fun onNext(stats: Statistics) {
            future.complete(stats)
            this.close()
        }

        override fun onError(throwable: Throwable) {
            future.completeExceptionally(throwable)
            this.close()
        }
    })
    return future.get()
}

fun extractImportantStats(stats: Statistics): Map<String, Any?> {
    val cpuStats = stats.cpuStats
    val preCpuStats = stats.preCpuStats
    val memoryStats = stats.memoryStats
    val networks = stats.networks

    return mapOf(
        "cpu_stats" to cpuStats,
        "precpu_stats" to preCpuStats,
        "memory_stats" to memoryStats,
        "networks" to networks
    )
}
