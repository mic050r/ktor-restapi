package example.com.routes

import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import example.com.models.User
import example.com.models.Users
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.get

fun Route.userRoutes() {
    route("/users") {
        // GET - 모든 유저 조회
        get {
            val users = transaction {
                Users.selectAll().map {
                    User(it[Users.id], it[Users.name], it[Users.age])
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
                Users.select { Users.id eq id }.map {
                    User(it[Users.id], it[Users.name], it[Users.age])
                }.singleOrNull()
            }

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        // POST - 유저 생성
        post {
            val user = call.receive<User>()
            transaction {
                Users.insert {
                    it[name] = user.name
                    it[age] = user.age
                }
            }
            call.respond(HttpStatusCode.Created, "User added successfully")
        }

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
                    it[age] = user.age
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
