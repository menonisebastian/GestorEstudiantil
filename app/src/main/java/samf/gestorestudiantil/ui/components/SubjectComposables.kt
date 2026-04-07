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
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun UnidadCard(
    unidad: Unidad,
    posts: List<Post>,
    onAddPost: (() -> Unit)? = null, // null if student
    onEditUnidad: (() -> Unit)? = null,
    onDeleteUnidad: (() -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    onDeletePost: ((Post) -> Unit)? = null
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
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Unidad", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                    if (onAddPost != null) {
                        IconButton(onClick = onAddPost) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir Post", tint = primaryColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (posts.isEmpty()) {
                Text(
                    text = "No hay publicaciones en esta unidad.",
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
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, surfaceDimColor.copy(alpha = 0.2f))
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
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
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
