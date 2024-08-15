package example.com

import example.com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
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
    val dotenv = dotenv()

    val dbUrl = dotenv["DB_URL"] ?: "jdbc:postgresql://localhost:5432/defaultdb"
    val dbUser = dotenv["DB_USER"] ?: "defaultuser"
    val dbPassword = dotenv["DB_PASSWORD"] ?: "defaultpassword"

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        driverClassName = "org.postgresql.Driver"
        username = dbUser
        password = dbPassword
        maximumPoolSize = 10
    }

    try {
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        // 연결 성공 시 로그 출력
        environment.log.info("Database connected successfully! : $dbUrl")
    } catch (e: Exception) {
        // 연결 실패 시 오류 로그 출력
        environment.log.error("Database connection failed: ${e.message}")
    }
}
