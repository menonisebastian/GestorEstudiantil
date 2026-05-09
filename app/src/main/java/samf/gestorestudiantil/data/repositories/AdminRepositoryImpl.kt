package samf.gestorestudiantil.data.repositories

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Clase
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.ScrapedCourse
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.data.models.Unidad
import samf.gestorestudiantil.data.models.Post
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.data.models.Entrega
import com.google.firebase.Timestamp
import io.github.jan.supabase.storage.Storage
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.enums.TipoEvaluacion as TipoEvaluacionEnum
import samf.gestorestudiantil.domain.repositories.AdminRepository
import samf.gestorestudiantil.R
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: Storage,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : AdminRepository {

    private val gson = Gson()

    override fun getUsuariosPorCentro(centroId: String): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { doc ->
                        val rol = doc.getString("rol")
                        when (rol) {
                            "ESTUDIANTE" -> doc.toObject(User.Estudiante::class.java)
                            "PROFESOR" -> doc.toObject(User.Profesor::class.java)
                            "ADMIN" -> doc.toObject(User.Admin::class.java)
                            else -> doc.toObject(User.Incompleto::class.java)
                        }
                    }
                    trySend(users)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun aprobarUsuario(usuarioId: String) {
        val userRef = db.collection("usuarios").document(usuarioId)
        val userSnap = userRef.get().await()

        userRef.update("estado", "ACTIVO").await()

        if (userSnap.getString("rol") == "ESTUDIANTE") {
            val cursoId = userSnap.getString("cursoId") ?: ""
            val turno = userSnap.getString("turno") ?: ""
            val cicloNum = userSnap.getLong("cicloNum")?.toInt() ?: 1

            val clasesQuery = db.collection("clases")
                .whereEqualTo("cursoGlobalId", cursoId)
                .whereEqualTo("turno", turno.lowercase().trim())
                .whereEqualTo("cicloNum", cicloNum)
                .get().await()

            for (doc in clasesQuery.documents) {
                doc.reference.update("estudiantesIds", FieldValue.arrayUnion(usuarioId))
            }
        }
    }

    override suspend fun desaprobarUsuario(usuarioId: String) {
        val userRef = db.collection("usuarios").document(usuarioId)
        val userSnap = userRef.get().await()

        userRef.update("estado", "PENDIENTE").await()

        if (userSnap.getString("rol") == "ESTUDIANTE") {
            val cursoId = userSnap.getString("cursoId") ?: ""
            val turno = userSnap.getString("turno") ?: ""
            val cicloNum = userSnap.getLong("cicloNum")?.toInt() ?: 1

            val clasesQuery = db.collection("clases")
                .whereEqualTo("cursoGlobalId", cursoId)
                .whereEqualTo("turno", turno.lowercase().trim())
                .whereEqualTo("cicloNum", cicloNum)
                .get().await()

            for (doc in clasesQuery.documents) {
                doc.reference.update("estudiantesIds", FieldValue.arrayRemove(usuarioId))
            }
        }
    }

    override suspend fun eliminarUsuario(usuarioId: String) {
        val userRef = db.collection("usuarios").document(usuarioId)

        // 1. Buscar TODAS las clases que contengan el ID de este usuario
        val clasesQuery = db.collection("clases")
            .whereArrayContains("estudiantesIds", usuarioId)
            .get().await()

        // 2. Eliminar el usuario del array 'estudiantesIds' en los documentos encontrados
        val batch = db.batch()
        for (doc in clasesQuery.documents) {
            batch.update(doc.reference, "estudiantesIds", FieldValue.arrayRemove(usuarioId))
        }
        batch.commit().await()

        // 3. Finalmente, eliminar el documento del usuario
        userRef.delete().await()
    }

    override suspend fun actualizarDatosUsuario(usuarioId: String, updates: Map<String, Any?>) {
        db.collection("usuarios").document(usuarioId).update(updates).await()
    }

    override fun getCentros(): Flow<List<Centro>> = callbackFlow {
        val subscription = db.collection("centros").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(Centro::class.java))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getClasesPorCentro(centroId: String): Flow<List<Clase>> = callbackFlow {
        val subscription = db.collection("clases")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Clase::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getCursosPorCentro(centroId: String): Flow<List<Curso>> = callbackFlow {
        val subscription = db.collection("cursos")
            .whereEqualTo("centroId", centroId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Curso::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAsignaturasSinProfesor(turno: String): Flow<List<Asignatura>> = callbackFlow {
        val query = db.collection("asignaturas")
            .whereEqualTo("profesorId", "")
            
        val finalQuery = if (turno.isNotEmpty()) {
            query.whereEqualTo("turno", turno.lowercase().trim())
        } else {
            query
        }

        val subscription = finalQuery.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                trySend(snapshot.toObjects(Asignatura::class.java))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getAsignaturasPorProfesor(profesorId: String): Flow<List<Asignatura>> = callbackFlow {
        val subscription = db.collection("asignaturas")
            .whereEqualTo("profesorId", profesorId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Asignatura::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAsignaturasPorCurso(cursoId: String, turno: String): Flow<List<Asignatura>> = callbackFlow {
        val subscription = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("turno", turno.lowercase().trim())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Asignatura::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getHorarios(cursoId: String, cicloNum: Int, turno: String): Flow<List<Horario>> = callbackFlow {
        val subscription = db.collection("horarios")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("cicloNum", cicloNum)
            .whereEqualTo("turno", turno.lowercase().trim())
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Horario::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun asignarAsignaturaAProfesor(asignaturaId: String, profesorId: String) {
        val userDoc = db.collection("usuarios").document(profesorId).get().await()
        val nombreProfesor = userDoc.getString("nombre") ?: context.getString(R.string.label_unknown_professor)

        val updates = mapOf(
            "profesorId" to profesorId,
            "profesorNombre" to nombreProfesor
        )
        db.collection("asignaturas").document(asignaturaId).update(updates).await()
        
        val horariosSnapshot = db.collection("horarios")
            .whereEqualTo("asignaturaId", asignaturaId)
            .get().await()
            
        val batch = db.batch()
        for (doc in horariosSnapshot.documents) {
            batch.update(doc.reference, mapOf(
                "profesorId" to profesorId,
                "profesorNombre" to nombreProfesor
            ))
        }
        batch.commit().await()

        actualizarAcronimosProfesor(profesorId)
    }

    override suspend fun desasignarAsignatura(asignaturaId: String, profesorId: String) {
        val updates = mapOf(
            "profesorId" to "",
            "profesorNombre" to ""
        )
        db.collection("asignaturas").document(asignaturaId).update(updates).await()

        val horariosSnapshot = db.collection("horarios")
            .whereEqualTo("asignaturaId", asignaturaId)
            .get().await()

        val batch = db.batch()
        for (doc in horariosSnapshot.documents) {
            batch.update(doc.reference, mapOf(
                "profesorId" to "",
                "profesorNombre" to ""
            ))
        }
        batch.commit().await()

        actualizarAcronimosProfesor(profesorId)
    }

    override suspend fun asignarTutorAClase(claseId: String, tutorId: String) {
        db.collection("clases").document(claseId).update("tutorId", tutorId).await()
    }

    private suspend fun actualizarAcronimosProfesor(profesorId: String) {
        val snapshot = db.collection("asignaturas")
            .whereEqualTo("profesorId", profesorId)
            .get().await()

        val asignaturas = snapshot.toObjects(Asignatura::class.java)

        val listaIdsAsignaturas = asignaturas.map { it.id }

        db.collection("usuarios").document(profesorId)
            .update(
                mapOf(
                    "asignaturasImpartidas" to listaIdsAsignaturas
                )
            ).await()
    }

    override suspend fun guardarCentro(centro: Centro) {
        val ref = if (centro.id.isEmpty()) db.collection("centros").document() else db.collection("centros").document(centro.id)
        if (centro.id.isEmpty()) centro.id = ref.id
        ref.set(centro, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarCentro(centroId: String) {
        db.collection("centros").document(centroId).delete().await()
    }

    override suspend fun guardarCurso(curso: Curso) {
        val ref = if (curso.id.isEmpty()) db.collection("cursos").document() else db.collection("cursos").document(curso.id)
        if (curso.id.isEmpty()) curso.id = ref.id
        ref.set(curso, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarCurso(cursoId: String) {
        db.collection("cursos").document(cursoId).delete().await()
    }

    override suspend fun guardarAsignatura(asignatura: Asignatura) {
        val ref = if (asignatura.id.isEmpty()) db.collection("asignaturas").document() else db.collection("asignaturas").document(asignatura.id)
        if (asignatura.id.isEmpty()) asignatura.id = ref.id
        ref.set(asignatura, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarAsignatura(asignaturaId: String) {
        db.collection("asignaturas").document(asignaturaId).delete().await()
    }

    override suspend fun guardarHorario(horario: Horario) {
        horario.turno = horario.turno.lowercase().trim()
        
        if (horario.asignaturaId.isNotEmpty()) {
            val asigDoc = db.collection("asignaturas").document(horario.asignaturaId).get().await()
            if (asigDoc.exists()) {
                horario.profesorId = asigDoc.getString("profesorId") ?: ""
                horario.profesorNombre = asigDoc.getString("profesorNombre") ?: ""
            }
        }

        val ref = if (horario.id.isEmpty()) db.collection("horarios").document() else db.collection("horarios").document(horario.id)
        if (horario.id.isEmpty()) horario.id = ref.id
        ref.set(horario, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun eliminarHorario(horarioId: String) {
        db.collection("horarios").document(horarioId).delete().await()
    }

    override suspend fun seedDatabase(jsonlLines: List<String>) {
        val idCentro = "ies_comercio"
        db.collection("centros").document(idCentro).set(
            hashMapOf(
                "id" to idCentro,
                "nombre" to "I.E.S Comercio",
                "tipo" to "Instituto de Educación Secundaria"
            )
        ).await()

        var batch = db.batch()
        var operationCount = 0

        for (line in jsonlLines) {
            if (line.trim().isEmpty()) continue
            val sc = gson.fromJson(line, ScrapedCourse::class.java)
            if (sc._status != "ok") continue

            val cursoId = "${idCentro}_${sc.acronimo}".replace(" ", "_")
            val cursoRef = db.collection("cursos").document(cursoId)
            val turnosNormalizados = (sc.turnos_disponibles ?: emptyList()).map { it.lowercase().trim() }

            val cursoData = hashMapOf(
                "id"                to cursoId,
                "centroId"          to idCentro,
                "acronimo"          to sc.acronimo,
                "nombre"            to sc.nombre_curso,
                "tipo"              to sc.tipo,
                "modalidad"         to (sc.modalidad ?: "presencial"),
                "turnosDisponibles" to turnosNormalizados,
                "urlInfo"           to (sc.url ?: ""),
                "horasTotalesCurso" to extraerNumero(sc.horas_totales_curso),
                "iconoName"         to (sc.iconoName ?: "School"),
                "colorFondoHex"     to (sc.colorFondoHex ?: "#D0E1FF"),
                "colorIconoHex"     to (sc.colorIconoHex ?: "#2563EB")
            )

            batch.set(cursoRef, cursoData, com.google.firebase.firestore.SetOptions.merge())
            operationCount++

            val ciclos = sc.ciclos ?: emptyList()
            val turnos = turnosNormalizados.ifEmpty { listOf("matutino") }

            for (turno in turnos) {
                for (cicloBloque in ciclos) {
                    val cicloRaw = cicloBloque.ciclo
                    val cicloNum = cicloAInt(cicloRaw)

                    for (asig in cicloBloque.asignaturas) {
                        val asigId = "${cursoId}_${cicloNum}_${asig.acronimo}_${turno}".replace(" ", "_").lowercase()
                        val asigRef = db.collection("asignaturas").document(asigId)

                        val asigData = hashMapOf(
                            "id"             to asigId,
                            "cursoId"        to cursoId,
                            "centroId"       to idCentro,
                            "acronimo"       to asig.acronimo,
                            "nombre"         to asig.nombre,
                            "ciclo"          to cicloRaw,
                            "cicloNum"       to cicloNum,
                            "turno"          to turno,
                            "horasTotales"   to extraerNumero(asig.horas_totales),
                            "horasSemanales" to extraerNumero(asig.horas_semanales),
                            "iconoName"      to (asig.iconoName ?: "Class"),
                            "colorFondoHex"  to (asig.colorFondoHex ?: "#E8E8E8"),
                            "colorIconoHex"  to (asig.colorIconoHex ?: "#6B7280")
                        )

                        batch.set(asigRef, asigData, com.google.firebase.firestore.SetOptions.merge())
                        operationCount++

                        if (operationCount >= 400) {
                            try {
                                batch.commit().await()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                throw e
                            }
                            batch = db.batch()
                            operationCount = 0
                        }
                    }
                }
            }
        }

        if (operationCount > 0) {
            try {
                batch.commit().await()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }


    override suspend fun generarClasesPorDefecto(centroId: String) {
        val batch = db.batch()

        val asignaturasGlobales = db.collection("asignaturas").get().await().toObjects(Asignatura::class.java)

        val cursosGlobales = db.collection("cursos")
            .whereEqualTo("centroId", centroId)
            .get()
            .await()
            .toObjects(Curso::class.java)

        val estudiantesSnapshots = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .whereEqualTo("rol", "ESTUDIANTE")
            .whereEqualTo("estado", "ACTIVO")
            .get()
            .await()
            .documents

        val asignaturasAgrupadas = asignaturasGlobales.groupBy { asignatura ->
            Triple(asignatura.cursoId, asignatura.turno.uppercase(), asignatura.cicloNum)
        }

        asignaturasAgrupadas.forEach { (claveAgrupacion, listaMaterias) ->
            val (cursoId, turnoMayuscula, cicloNum) = claveAgrupacion

            if (cursoId.isEmpty() || turnoMayuscula.isEmpty()) return@forEach

            val cursoReal = cursosGlobales.find { it.id == cursoId }
            val acronimoReal = cursoReal?.acronimo ?: return@forEach

            val letraTurno = turnoMayuscula.first().uppercaseChar()
            val idClase = "${acronimoReal}${letraTurno}${cicloNum}"

            val idsEstudiantes = estudiantesSnapshots.filter { doc ->
                val docCursoId = doc.getString("cursoId") ?: ""
                val docTurno = doc.getString("turno") ?: ""
                val docCicloNum = doc.getLong("cicloNum")?.toInt() ?: -1

                docCursoId == cursoId &&
                        docTurno.uppercase() == turnoMayuscula &&
                        docCicloNum == cicloNum
            }.map { it.id }

            val claseRef = db.collection("clases").document(idClase)

            val nuevaClase = Clase(
                id = idClase,
                centroId = centroId,
                cursoGlobalId = cursoId,
                cicloNum = cicloNum,
                turno = listaMaterias.first().turno,
                tutorId = null,
                estudiantesIds = idsEstudiantes,
                asignaturasIds = listaMaterias.map { it.id }
            )

            batch.set(claseRef, nuevaClase)
        }

        batch.commit().await()
    }

    // --- HELPER PARA CREAR USUARIOS EN AUTH SIN DESLOGUEAR AL ADMIN ---
    private suspend fun createAuthUserSinDesloguear(email: String, pass: String): String {
        return try {
            // Utilizamos una instancia secundaria para no cerrar la sesión principal
            var secondaryApp = FirebaseApp.getApps(context).find { it.name == "SecondaryAuthApp" }
            if (secondaryApp == null) {
                val options = FirebaseApp.getInstance().options
                secondaryApp = FirebaseApp.initializeApp(context, options, "SecondaryAuthApp")
            }
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp!!)
            val result = secondaryAuth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: UUID.randomUUID().toString()
            secondaryAuth.signOut() // Cerramos la secundaria
            uid
        } catch (e: Exception) {
            e.printStackTrace()
            UUID.randomUUID().toString() // Fallback si el correo ya existe
        }
    }

    override suspend fun generarAlumnosFalsos() {
        val nombres = listOf("Alejandro", "María", "Daniel", "Lucía", "Pablo", "Paula", "Hugo", "Daniela", "Álvaro", "Sara", "Javier", "Elena", "Marcos", "Laura", "Diego", "Carmen", "Mario", "Alba", "David", "Irene")
        val apellidos = listOf("García", "Fernández", "González", "Rodríguez", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Martín", "Ruiz", "Díaz", "Álvarez", "Moreno", "Muñoz", "Romero", "Alonso", "Gutiérrez", "Navarro", "Torres")

        val estudiantesDAM1 = mutableListOf<String>()
        val estudiantesDAM2 = mutableListOf<String>()

        val batch = db.batch()

        for (i in 1..40) {
            val nombre = nombres.random()
            val apellido = apellidos.random()
            
            // Limpiar acentos para el email
            val emailBase = "${nombre.lowercase()}.${apellido.lowercase()}$i@iescomercio.com"
            val email = emailBase.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("ñ", "n")

            // Crear en Authentication (Contraseña: 123456)
            val uid = createAuthUserSinDesloguear(email, "123456")

            val cicloNum = if (i <= 20) 1 else 2

            val estudiante = User.Estudiante(
                id = uid,
                nombre = "$nombre $apellido",
                email = email,
                centroId = "ies_comercio",
                estado = "ACTIVO",
                rol = "ESTUDIANTE",
                cursoId = "ies_comercio_DAM",
                curso = "DAM",
                turno = "vespertino",
                cicloNum = cicloNum
            )

            batch.set(db.collection("usuarios").document(uid), estudiante)

            if (cicloNum == 1) estudiantesDAM1.add(uid) else estudiantesDAM2.add(uid)
        }

        // Ejecutar creación de usuarios
        batch.commit().await()

        // --- Actualizar el modelo Clase ---
        val claseBatch = db.batch()
        val clase1Ref = db.collection("clases").document("DAMV1")
        val clase2Ref = db.collection("clases").document("DAMV2")

        // Asegurarnos de que las clases existen (Merge)
        claseBatch.set(clase1Ref, mapOf("id" to "DAMV1", "centroId" to "ies_comercio", "cursoGlobalId" to "ies_comercio_DAM", "cicloNum" to 1, "turno" to "vespertino"), SetOptions.merge())
        claseBatch.set(clase2Ref, mapOf("id" to "DAMV2", "centroId" to "ies_comercio", "cursoGlobalId" to "ies_comercio_DAM", "cicloNum" to 2, "turno" to "vespertino"), SetOptions.merge())

        // Añadir a estudiantesIds usando arrayUnion para no sobreescribir otros existentes
        if (estudiantesDAM1.isNotEmpty()) {
            claseBatch.update(clase1Ref, "estudiantesIds", FieldValue.arrayUnion(*estudiantesDAM1.toTypedArray()))
        }
        if (estudiantesDAM2.isNotEmpty()) {
            claseBatch.update(clase2Ref, "estudiantesIds", FieldValue.arrayUnion(*estudiantesDAM2.toTypedArray()))
        }

        claseBatch.commit().await()
    }

    override suspend fun generarProfesoresFalsos() {
        val nombres = listOf("Marcos", "Laura", "Javier", "Ana", "Diego", "Carmen", "Mario", "Elena", "David", "Alba")
        val apellidos = listOf("Ruiz", "Díaz", "Álvarez", "Moreno", "Muñoz", "Romero", "Alonso", "Gutiérrez", "Navarro", "Torres")

        val batch = db.batch()

        for (i in 1..20) {
            val nombre = nombres.random()
            val apellido = apellidos.random()
            
            val emailBase = "prof.${nombre.lowercase()}.${apellido.lowercase()}$i@iescomercio.com"
            val email = emailBase.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("ñ", "n")

            val uid = createAuthUserSinDesloguear(email, "123456")

            val profesor = User.Profesor(
                id = uid,
                nombre = "$nombre $apellido",
                email = email,
                centroId = "ies_comercio",
                estado = "ACTIVO",
                rol = "PROFESOR",
                departamento = User.Profesor.DEPARTAMENTOS.random(),
                turno = "vespertino",
                asignaturasImpartidas = emptyList()
            )

            batch.set(db.collection("usuarios").document(uid), profesor)
        }

        batch.commit().await()
    }

    override suspend fun generarContenidoAcademicoDummy() {
        val subjectUnits = mapOf(
            "PROG" to listOf("Introducción a Kotlin", "Estructuras de Control", "Programación Orientada a Objetos", "Colecciones y Ficheros", "Excepciones"),
            "BD" to listOf("Modelo Relacional", "SQL Básico", "Diseño de Base de Datos", "PL/SQL y Procedimientos", "Bases de Datos NoSQL"),
            "LM" to listOf("HTML5 y Estructura Web", "CSS3 y Diseño", "XML y Esquemas", "Transformaciones XSLT", "Sindicación de Contenidos"),
            "SI" to listOf("Hardware y Componentes", "Sistemas Operativos Libres", "Sistemas Operativos Propietarios", "Redes Locales", "Seguridad Básica"),
            "ED" to listOf("Entornos de Desarrollo IDE", "Control de Versiones Git", "Pruebas Unitarias", "Optimización y Refactorización", "Diagramas de Clase"),
            "FOL" to listOf("Salud Laboral", "Derecho del Trabajo", "El Contrato de Trabajo", "Seguridad Social", "Búsqueda de Empleo"),
            "PMDM" to listOf("Arquitectura Android", "Interfaces de Usuario", "Persistencia de Datos", "Servicios y Notificaciones", "Publicación en Stores"),
            "PSP" to listOf("Programación Multihilo", "Comunicaciones en Red", "Servicios de Red", "Seguridad y Criptografía", "Programación Concurrente"),
            "AD" to listOf("Ficheros de Texto y Binarios", "Conectores JDBC", "Object-Relational Mapping (ORM)", "Bases de Datos Objeto-Relacionales", "XML en Bases de Datos"),
            "DI" to listOf("Diseño de Interfaces", "Componentes Visuales", "Informes y Gráficos", "Usabilidad y Accesibilidad", "Empaquetado y Distribución"),
            "SGE" to listOf("Sistemas ERP", "Instalación y Configuración", "Personalización de Módulos", "Extracción de Información", "Seguridad en SGE"),
            "EIE" to listOf("Espíritu Emprendedor", "Idea de Negocio", "Plan de Marketing", "Plan Económico-Financiero", "Trámites de Constitución")
        )

        val asignaturasSnap = db.collection("asignaturas")
            .whereEqualTo("cursoId", "ies_comercio_DAM")
            .get().await()

        val asignaturas = asignaturasSnap.toObjects(Asignatura::class.java)
        
        var batch = db.batch()
        var ops = 0

        for (asig in asignaturas) {
            val units = subjectUnits[asig.acronimo.uppercase()] ?: 
                        listOf("Introducción", "Fundamentos", "Avanzado", "Proyecto Final")
            
            for ((index, unitName) in units.withIndex()) {
                val unitId = UUID.randomUUID().toString()
                val unidad = Unidad(
                    id = unitId,
                    nombre = unitName,
                    descripcion = "Contenidos detallados de $unitName para ${asig.nombre}.",
                    asignaturaId = asig.id,
                    orden = index + 1,
                    visible = true
                )
                batch.set(db.collection("unidades").document(unitId), unidad)
                ops++

                for (p in 1..2) {
                    val postId = UUID.randomUUID().toString()
                    val post = Post(
                        id = postId,
                        titulo = "Resumen ${if (p == 1) "Teórico" else "Práctico"} - $unitName",
                        contenido = "En este post se resume el contenido ${if (p == 1) "teórico fundamental" else "práctico con ejemplos"} de $unitName.",
                        autorId = asig.profesorId.ifEmpty { "admin_dummy" },
                        autorNombre = asig.profesorNombre.ifEmpty { "Profesor Asignado" },
                        asignaturaId = asig.id,
                        unidadId = unitId,
                        visible = true
                    )
                    batch.set(db.collection("posts").document(postId), post)
                    ops++
                }

                val tareaId = UUID.randomUUID().toString()
                val tarea = Tarea(
                    id = tareaId,
                    titulo = "Entregable: $unitName",
                    descripcion = "Realiza los ejercicios propuestos de $unitName y sube el resultado aquí.",
                    claseId = if (asig.cicloNum == 1) "DAMV1" else "DAMV2",
                    profesorId = asig.profesorId.ifEmpty { "admin_dummy" },
                    asignaturaId = asig.id,
                    centroId = asig.centroId,
                    unidadId = unitId,
                    fechaLimiteEntrega = Timestamp(Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))),
                    visible = true
                )
                batch.set(db.collection("tareas").document(tareaId), tarea)
                ops++

                if (ops > 400) {
                    batch.commit().await()
                    batch = db.batch()
                    ops = 0
                }
            }
        }
        if (ops > 0) batch.commit().await()
    }

    override suspend fun generarCalificacionesParaArturo() {
        val emailArturo = "arturomenoni@gmail.com"
        val userSnap = db.collection("usuarios")
            .whereEqualTo("email", emailArturo)
            .get().await()

        val arturo = userSnap.documents.firstOrNull()?.toObject(User.Estudiante::class.java) ?: return
        val arturoId = arturo.id

        // Obtenemos todas las asignaturas de DAM (cursoId: ies_comercio_DAM)
        val asignaturasSnap = db.collection("asignaturas")
            .whereEqualTo("cursoId", "ies_comercio_DAM")
            .get().await()

        val asignaturas = asignaturasSnap.toObjects(Asignatura::class.java)

        val comentarios = listOf(
            "Buen trabajo, sigue así.",
            "Debes mejorar la presentación.",
            "Excelente resolución del problema.",
            "Faltan algunos apartados obligatorios.",
            "Conceptos bien asimilados.",
            "Participación activa en clase.",
            "Resultado satisfactorio.",
            "Se nota el esfuerzo en la práctica."
        )

        var batch = db.batch()
        var ops = 0

        for (asig in asignaturas) {
            // Generamos de 2 a 4 evaluaciones por asignatura
            val numEvaluaciones = (2..4).random()
            for (i in 1..numEvaluaciones) {
                val evaluacionId = UUID.randomUUID().toString()
                val nota = (50..100).random() / 10.0 // Notas entre 5.0 y 10.0
                
                val evaluacion = Evaluacion(
                    id = evaluacionId,
                    nombre = "Evaluación $i de ${asig.acronimo}",
                    nota = nota,
                    estudianteId = arturoId,
                    asignaturaId = asig.id,
                    visible = true,
                    comentario = comentarios.random(),
                    tipoEvaluacion = TipoEvaluacionEnum.entries.random()
                )

                batch.set(db.collection("evaluaciones").document(evaluacionId), evaluacion)
                ops++

                if (ops >= 400) {
                    batch.commit().await()
                    batch = db.batch()
                    ops = 0
                }
            }
        }

        if (ops > 0) {
            batch.commit().await()
        }
    }

    private fun extraerNumero(texto: String?): Int {
        if (texto.isNullOrEmpty()) return 0
        return texto.replace(Regex("\\D"), "").toIntOrNull() ?: 0
    }

    private fun cicloAInt(cicloStr: String?): Int {
        if (cicloStr.isNullOrEmpty()) return 1
        return cicloStr.trim().firstOrNull()?.digitToIntOrNull() ?: 1
    }

    override suspend fun limpiarPapelera() {
        val limite = System.currentTimeMillis() - (24L * 60 * 60 * 1000)
        
        // 1. TAREAS: Borrado físico de marcadas para eliminar
        val tareasABorrar = db.collection("tareas")
            .whereLessThan("fechaEliminacion", limite)
            .get().await().toObjects(Tarea::class.java)
        
        for (tarea in tareasABorrar) {
            borrarTareaFisicamente(tarea)
        }

        // 2. POSTS: Borrado físico de marcados para eliminar
        val postsABorrar = db.collection("posts")
            .whereLessThan("fechaEliminacion", limite)
            .get().await()
        for (doc in postsABorrar.documents) {
            doc.reference.delete().await()
        }

        // 3. UNIDADES: Borrado físico de marcadas para eliminar + sus hijos
        val unidadesABorrar = db.collection("unidades")
            .whereLessThan("fechaEliminacion", limite)
            .get().await().toObjects(Unidad::class.java)
        
        for (unidad in unidadesABorrar) {
            // Borrar hijos de la unidad (tareas y posts)
            val postsHijos = db.collection("posts").whereEqualTo("unidadId", unidad.id).get().await()
            for (doc in postsHijos.documents) doc.reference.delete().await()
            
            val tareasHijas = db.collection("tareas").whereEqualTo("unidadId", unidad.id).get().await().toObjects(Tarea::class.java)
            for (tarea in tareasHijas) borrarTareaFisicamente(tarea)
            
            db.collection("unidades").document(unidad.id).delete().await()
        }
    }

    private suspend fun borrarTareaFisicamente(tarea: Tarea) {
        val bucket = storage.from("gestor-estudiantil")
        // Entregas
        val entregas = db.collection("entregas").whereEqualTo("tareaId", tarea.id).get().await().toObjects(Entrega::class.java)
        for (entrega in entregas) {
            try { bucket.delete(entrega.adjunto.supabasePath) } catch (e: Exception) {}
            db.collection("entregas").document(entrega.id).delete().await()
        }
        // Archivo tarea
        tarea.adjunto?.let { try { bucket.delete(it.supabasePath) } catch (e: Exception) {} }
        // Documento tarea
        db.collection("tareas").document(tarea.id).delete().await()
    }
}
