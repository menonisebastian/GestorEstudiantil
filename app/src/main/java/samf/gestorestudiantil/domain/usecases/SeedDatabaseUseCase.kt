package samf.gestorestudiantil.domain.usecases

import samf.gestorestudiantil.domain.repositories.AdminRepository
import javax.inject.Inject

class SeedDatabaseUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(jsonlLines: List<String>) {
        adminRepository.seedDatabase(jsonlLines)
    }
}
