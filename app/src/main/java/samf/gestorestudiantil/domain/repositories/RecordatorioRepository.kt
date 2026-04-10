package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.Recordatorio

interface RecordatorioRepository {
    fun getRecordatorios(usuarioId: String): Flow<List<Recordatorio>>
    suspend fun guardarRecordatorio(recordatorio: Recordatorio)
    suspend fun eliminarRecordatorio(recordatorioId: String)
}
