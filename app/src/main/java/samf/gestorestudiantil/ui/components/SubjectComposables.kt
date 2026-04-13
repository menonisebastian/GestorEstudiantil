package samf.gestorestudiantil.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.secondaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Campaign
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@Composable
fun UnidadCard(
    unidad: Unidad,
    posts: List<Post>,
    tareas: List<Tarea> = emptyList(),
    onAddPost: (() -> Unit)? = null, // null if student
    onAddTarea: (() -> Unit)? = null,
    onEditUnidad: (() -> Unit)? = null,
    onDeleteUnidad: (() -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    onDeletePost: ((Post) -> Unit)? = null,
    onEditTarea: ((Tarea) -> Unit)? = null,
    onDeleteTarea: ((Tarea) -> Unit)? = null,
    onTareaClick: ((Tarea) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Unidad ${unidad.orden}: ${unidad.nombre}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        if (onEditUnidad != null) { // Indicador de visibilidad solo para profesor
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (unidad.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (unidad.visible) primaryColor else surfaceDimColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (unidad.descripcion.isNotEmpty()) {
                        Text(
                            text = unidad.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = surfaceDimColor
                        )
                    }
                }
                Row {
                    if (onAddPost != null || onAddTarea != null || onEditUnidad != null || onDeleteUnidad != null) {
                        FloatingPill(
                            items = listOfNotNull(
                                onAddPost?.let {
                                    MenuItem(
                                        text = "Añadir Publicación",
                                        icon = Icons.Default.Campaign,
                                        onClick = it,
                                        iconTint = secondaryColor
                                    )
                                },
                                onAddTarea?.let {
                                    MenuItem(
                                        text = "Añadir Tarea",
                                        icon = Icons.AutoMirrored.Filled.Assignment,
                                        onClick = it,
                                        iconTint = tertiaryColor
                                    )
                                },
                                onEditUnidad?.let {
                                    MenuItem(
                                        text = "Editar Unidad",
                                        icon = Icons.Default.Edit,
                                        onClick = it,
                                        iconTint = primaryColor
                                    )
                                },
                                onDeleteUnidad?.let {
                                    MenuItem(
                                        text = "Eliminar Unidad",
                                        icon = Icons.Default.Delete,
                                        onClick = it,
                                        iconTint = errorColor,
                                        isDestructive = true
                                    )
                                }
                            ),
                            expandedIcon = Icons.Default.ArrowDropUp,
                            shape = CircleShape,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (posts.isEmpty() && tareas.isEmpty()) {
                Text(
                    text = "No hay contenido en esta unidad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = surfaceDimColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                posts.forEach { post ->
                    PostCard(
                        post = post,
                        onEdit = onEditPost?.let { { it(post) } },
                        onDelete = onDeletePost?.let { { it(post) } }
                    )
                }
                tareas.forEach { tarea ->
                    TareaCard(
                        tarea = tarea,
                        onEdit = onEditTarea?.let { { it(tarea) } },
                        onDelete = onDeleteTarea?.let { { it(tarea) } },
                        onClick = onTareaClick?.let { { it(tarea) } }
                    )
                }
            }
        }
    }
}

@Composable
fun TareaCard(
    tarea: Tarea,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (onEdit != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (tarea.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (tarea.visible) primaryColor else surfaceDimColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (onEdit != null || onDelete != null) {
                    Row {
                        onEdit?.let {
                            IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = surfaceDimColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        onDelete?.let {
                            IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = errorColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (tarea.descripcion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tarea.adjunto != null) {
                val estudianteViewModel: EstudianteViewModel = viewModel()
                val profesorViewModel: ProfesorViewModel = viewModel()
                Surface(
                    onClick = {
                        tarea.adjunto?.let { adjunto ->
                            if (onEdit != null) { // Si onEdit no es null, es un profesor
                                profesorViewModel.descargarArchivo(adjunto.supabasePath, adjunto.nombreArchivo)
                            } else {
                                estudianteViewModel.descargarArchivo(adjunto.supabasePath, adjunto.nombreArchivo)
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = tertiaryColor.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = tertiaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tarea.adjunto?.nombreArchivo ?: "Ver adjunto",
                            style = MaterialTheme.typography.bodySmall,
                            color = tertiaryColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val limite = dateFormat.format(tarea.fechaLimiteEntrega.toDate())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = surfaceDimColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Entrega hasta: $limite",
                        style = MaterialTheme.typography.bodySmall,
                        color = surfaceDimColor
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.titulo,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (onEdit != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (post.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (post.visible) textColor else surfaceDimColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (onEdit != null || onDelete != null) {
                    Row {
                        onEdit?.let {
                            IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = surfaceDimColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        onDelete?.let {
                            IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = errorColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(post.fechaCreacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = surfaceDimColor
                )
            }
        }
    }
}
