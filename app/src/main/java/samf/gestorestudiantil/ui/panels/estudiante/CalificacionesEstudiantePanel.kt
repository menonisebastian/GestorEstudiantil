package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.ui.components.AsignaturaCard

@Composable
fun CalificacionesEstudiantePanel(
    asignaturas: List<Asignatura>,
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit
)
{
    var searchText by remember { mutableStateOf("") }

    val filteredAsignaturas by remember(asignaturas, searchText) {
        derivedStateOf {
            if (searchText.isEmpty()) asignaturas
            else asignaturas.filter {
                it.nombre.contains(searchText, ignoreCase = true) || 
                it.acronimo.contains(searchText, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {


            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            )
            {
                items(filteredAsignaturas)
                {
                    asignatura ->
                    AsignaturaCard(modifier = Modifier,asignatura, userRole = "ESTUDIANTE", onClick = {
                        onAsignaturaClick(asignatura)
                    })
                }
            }
        }
    }
}
