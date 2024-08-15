package example.com.models

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// Exposed ORM 테이블 정의
object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val age = integer("age")
    val password = varchar("password", 64)  // 비밀번호 필드 추가

    override val primaryKey = PrimaryKey(id)
}

// 데이터 클래스 정의 (직렬화를 위해 Serializable 사용)
@Serializable
data class User(val id: Int? = null, val name: String, val age: Int, val password: String)
