package example.com

import example.com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 데이터베이스 설정을 여기서 초기화
    configureDatabase()

    // 다른 설정들
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()
}

fun Application.configureDatabase() {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/test"
        driverClassName = "org.postgresql.Driver"
        username = "mic050r"
        password = "123456"
        maximumPoolSize = 10
    }

    try {
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        // 연결 성공 시 로그 출력
        environment.log.info("Database connected successfully!")
    } catch (e: Exception) {
        // 연결 실패 시 오류 로그 출력
        environment.log.error("Database connection failed: ${e.message}")
    }
}
