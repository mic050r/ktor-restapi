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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.userRoutes() {

    route("/users") {

        // PUT - 유저 정보 수정
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val user = call.receive<User>()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@put
            }

            val updated = transaction {
                Users.update({ Users.id eq id }) {
                    it[name] = user.name
                    it[email] = user.email
                    it[password] = user.password  // 비밀번호 업데이트
                }
            }

            if (updated == 0) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(HttpStatusCode.OK, "User updated successfully")
            }
        }

        // DELETE - 유저 삭제
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@delete
            }

            val deleted = transaction {
                Users.deleteWhere { Users.id eq id }
            }

            if (deleted == 0) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(HttpStatusCode.OK, "User deleted successfully")
            }
        }
    }
}
