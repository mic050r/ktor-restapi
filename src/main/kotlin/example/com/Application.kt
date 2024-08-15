package example.com

import example.com.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import example.com.models.Users
import example.com.routes.authRoutes
import example.com.routes.userRoutes
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 설정 함수 호출
    configureDatabase()     // 데이터베이스 설정
    configureSerialization()  // 직렬화 설정
    configureHTTP()         // HTTP 관련 설정
    configureSecurity()     // 보안 설정
    configureRouting()      // 라우팅 설정
}

fun Application.configureDatabase() {
    val dotenv = dotenv()  // .env 파일 로드

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

    // 데이터베이스 테이블 생성
    transaction {
        SchemaUtils.drop(Users)
        SchemaUtils.create(Users)  // Users 테이블 생성
    }
}

fun Application.configureRouting() {
    routing {
        authenticate("auth-jwt") { // jwt 인증이 필요한 라우팅
            userRoutes()  // 유저 관련 라우팅 추가
        }
        authRoutes() // 로그인, 회원가입 라우팅 추가
    }
}