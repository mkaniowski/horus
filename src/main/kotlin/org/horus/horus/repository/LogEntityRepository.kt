package org.horus.horus.repository

import org.horus.horus.model.LogEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LogEntityRepository : JpaRepository<LogEntity, UUID> {
}