package samf.gestorestudiantil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.secondaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import samf.gestorestudiantil.data.models.Tarea

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
                    if (onEditUnidad != null) {
                        IconButton(onClick = onEditUnidad) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Unidad", tint = surfaceDimColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (onDeleteUnidad != null) {
                        IconButton(onClick = onDeleteUnidad) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Unidad", tint = errorColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                    if (onAddPost != null) {
                        IconButton(onClick = onAddPost) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir Post", tint = primaryColor)
                        }
                    }
                    if (onAddTarea != null) {
                        IconButton(onClick = onAddTarea) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Añadir Tarea", tint = primaryColor)
                        }
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

                if (tarea.adjunto != null) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TextButton(
                        onClick = {
                            tarea.adjunto?.urlDescarga?.let { url ->
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "No se puede abrir el archivo", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tarea.adjunto?.nombreArchivo ?: "Ver adjunto",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
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
                            tint = if (post.visible) primaryColor else surfaceDimColor,
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
                    text = "Por ${post.autorNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(post.fechaCreacion),
                    style = MaterialTheme.typography.bodySmall,
                    color = surfaceDimColor
                )
            }
        }
    }
}
