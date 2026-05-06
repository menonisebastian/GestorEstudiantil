package samf.gestorestudiantil.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MappersUITest {

    @Test
    fun testFormatearFechaParaMostrar() {
        assertEquals("01/01/2025", formatearFechaParaMostrar("2025/01/01"))
        assertEquals("01/01/2025", formatearFechaParaMostrar("2025-01-01"))
        
        assertEquals("1 de Enero de 2025", formatearFechaParaMostrar("2025/01/01", prettyDate = true))
        assertEquals("15 de Mayo de 2023", formatearFechaParaMostrar("2023/05/15", prettyDate = true))
        assertEquals("31 de Diciembre de 2024", formatearFechaParaMostrar("2024/12/31", prettyDate = true))
        
        assertEquals("", formatearFechaParaMostrar(""))
        assertEquals("invalid-date", formatearFechaParaMostrar("invalid-date"))
    }

    @Test
    fun testObtenerInicialesDeNombre() {
        assertEquals("SM", obtenerInicialesDeNombre("Sergio Morán"))
        assertEquals("S", obtenerInicialesDeNombre("Sergio"))
        assertEquals("SF", obtenerInicialesDeNombre("Sergio Morán Fiel")) 
        assertEquals("JD", obtenerInicialesDeNombre("  juan  doe  "))
        assertEquals("", obtenerInicialesDeNombre(""))
        assertEquals("", obtenerInicialesDeNombre(null))
    }

    @Test
    fun testStringCapitalize() {
        assertEquals("Matutino", "matutino".capitalize())
        assertEquals("Vespertino", "VESPERTINO".capitalize())
        assertEquals("Hola", "HOLA".capitalize())
        assertEquals("Hola", "hola".capitalize())
        assertEquals("", "".capitalize())
    }
}
