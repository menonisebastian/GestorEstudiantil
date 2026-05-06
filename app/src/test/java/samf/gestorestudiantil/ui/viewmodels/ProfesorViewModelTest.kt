package samf.gestorestudiantil.ui.viewmodels

import android.content.Context
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import samf.gestorestudiantil.MainDispatcherRule
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.ProfesorRepository
import samf.gestorestudiantil.domain.repositories.TareaRepository
import samf.gestorestudiantil.domain.repositories.UserRepository

class ProfesorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profesorRepository = mockk<ProfesorRepository>()
    private val tareaRepository = mockk<TareaRepository>()
    private val notificationRepository = mockk<NotificationRepository>()
    private val userRepository = mockk<UserRepository>()
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `cargarContenidoAsignatura actualiza el estado con los datos de los repositorios`() = runTest {
        val asignaturaId = "test_asig"
        val unidades = emptyList<samf.gestorestudiantil.data.models.Unidad>()
        val posts = emptyList<samf.gestorestudiantil.data.models.Post>()
        val tareas = emptyList<samf.gestorestudiantil.data.models.Tarea>()

        coEvery { profesorRepository.getUnidades(asignaturaId) } returns flowOf(unidades)
        coEvery { profesorRepository.getPosts(asignaturaId) } returns flowOf(posts)
        coEvery { tareaRepository.getTareasPorAsignatura(asignaturaId) } returns flowOf(tareas)

        val viewModel = ProfesorViewModel(
            profesorRepository,
            tareaRepository,
            notificationRepository,
            userRepository,
            context
        )

        viewModel.cargarContenidoAsignatura(asignaturaId)

        val state = viewModel.state.value
        assertEquals(unidades, state.unidades)
        assertEquals(posts, state.posts)
        assertEquals(tareas, state.tareas)
    }
}
