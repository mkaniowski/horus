package org.horus.horus.repository

import org.horus.horus.model.LastAnalyzedTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LastAnalyzedTimestampRepository : JpaRepository<LastAnalyzedTimestamp, String> {
}