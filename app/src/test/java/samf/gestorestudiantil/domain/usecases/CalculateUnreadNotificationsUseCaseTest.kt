package samf.gestorestudiantil.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.repositories.EstudianteRepository

class CalculateUnreadNotificationsUseCaseTest {

    private val estudianteRepository = mockk<EstudianteRepository>()
    private val useCase = CalculateUnreadNotificationsUseCase(estudianteRepository)

    @Test
    fun `calcula correctamente notificaciones no leidas cuando hay posts y tareas mas recientes`() = runTest {
        val asignaturaId = "asig_001"
        val ultimaVisitaTimestamp = 1000L
        val ultimaVezMap = mapOf(asignaturaId to ultimaVisitaTimestamp)
        
        val asignaturas = listOf(
            Asignatura(id = asignaturaId, nombre = "Test")
        )

        coEvery { estudianteRepository.getCountNuevosPosts(asignaturaId, ultimaVisitaTimestamp) } returns 1
        coEvery { estudianteRepository.getCountNuevasTareas(asignaturaId, ultimaVisitaTimestamp) } returns 2

        val resultado = useCase.invoke(asignaturas, ultimaVezMap)

        assertEquals(3, resultado[0].numNotificaciones)
    }

    @Test
    fun `devuelve cero si el repositorio no encuentra nuevos elementos despues de la ultima visita`() = runTest {
        val asignaturaId = "asig_002"
        val ultimaVisitaTimestamp = 2000L
        val ultimaVezMap = mapOf(asignaturaId to ultimaVisitaTimestamp)
        
        val asignaturas = listOf(
            Asignatura(id = asignaturaId, nombre = "Test")
        )

        coEvery { estudianteRepository.getCountNuevosPosts(asignaturaId, ultimaVisitaTimestamp) } returns 0
        coEvery { estudianteRepository.getCountNuevasTareas(asignaturaId, ultimaVisitaTimestamp) } returns 0

        val resultado = useCase.invoke(asignaturas, ultimaVezMap)

        assertEquals(0, resultado[0].numNotificaciones)
    }
}
