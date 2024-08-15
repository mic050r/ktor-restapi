package example.com.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import example.com.dto.Credentials
import example.com.dto.UserResponse
import example.com.models.User
import example.com.models.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes() {
    route("/users") {
        // POST - 유저 생성
        post {
            val user = call.receive<User>()
            transaction {
                Users.insert {
                    it[name] = user.name
                    it[email] = user.email
                    it[password] = user.password  // 비밀번호 저장
                }
            }
            call.respond(HttpStatusCode.Created, "User added successfully")
        }

        // POST - 로그인
        post("/login") {
            val credentials = call.receive<Credentials>()

            // 데이터베이스에서 유저 조회
            val user = transaction {
                Users.select { Users.name eq credentials.name }
                    .map { User(it[Users.id], it[Users.name], it[Users.email], it[Users.password]) }
                    .singleOrNull()
            }

            if (user != null && user.password == credentials.password) {  // 비밀번호 검증
                val token = JWT.create()
                    .withAudience("ktor-audience")
                    .withIssuer("ktor-issuer")
                    .withClaim("name", credentials.name)
                    .sign(Algorithm.HMAC256("secret"))

                call.respond(hashMapOf("token" to token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
        // GET - 모든 유저 조회
        get {
            val users = transaction {
                Users.selectAll().map {
                    UserResponse(it[Users.id], it[Users.name], it[Users.email])  // 비밀번호를 제외하고 응답
                }
            }
            call.respond(users)
        }

        // GET - 특정 유저 조회
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val user = transaction {
                Users.select { Users.id eq id }
                    .map { UserResponse(it[Users.id], it[Users.name], it[Users.email]) }
                    .singleOrNull()
            }

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }
    }

    // GET - 모든 유저 조회
    get {
        val users = transaction {
            Users.selectAll().map {
                UserResponse(it[Users.id], it[Users.name], it[Users.email])  // 비밀번호를 제외하고 응답
            }
        }
        call.respond(users)
    }

    // GET - 특정 유저 조회
    get("/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            return@get
        }

        val user = transaction {
            Users.select { Users.id eq id }
                .map { UserResponse(it[Users.id], it[Users.name], it[Users.email]) }
                .singleOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
        } else {
            call.respond(user)
        }
    }

}