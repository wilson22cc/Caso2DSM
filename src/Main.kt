import java.text.SimpleDateFormat
import java.util.*

data class Cliente(
    val Codigo: Int,
    var Nombre: String,
    var Telefono: String,
    var Correo: String
)

data class Orden(
    val Codigo: Int,
    val cliente: Cliente,
    var TipoComprobante: String,
    var FechaVenta: Date,
    var Estado: EstadoOrden,
    var MontoTotal: Double,
    val pagos: MutableList<Pago> = mutableListOf()
) {
    fun actualizarEstado(nuevoEstado: EstadoOrden) {
        this.Estado = nuevoEstado
        println("El estado de la orden ha sido actualizado a: $Estado")
    }

    fun procesarPagoParcial(monto: Double): Boolean {
        if (monto > 0) {
            pagos.add(Pago.Efectivo(moneda = "PEN"))
            MontoTotal -= monto
            println("Pago de $monto registrado. Saldo pendiente: $MontoTotal")
            return true
        }
        return false
    }
}

data class DetalleVenta(
    val Codigo: Int,
    val orden: Orden,
    val producto: Producto?,
    var Cantidad: Int?,
    var Descuento: Double
)

data class Producto(
    val Codigo: Int,
    var Nombre: String,
    var Precio: Double,
    var Impuesto: Double,
    var Stock: Int
)

sealed class Pago {
    data class TarjetaCredito(val numero: String, val caducidad: String, val tipo: String) : Pago()
    data class Efectivo(val moneda: String) : Pago()
    data class Cheque(val nombre: String, val entidadBancaria: String) : Pago()
}

enum class EstadoOrden {
    PENDIENTE, PAGADO, PROCESANDO, ENVIADO, ENTREGADO
}

fun main() {
    val productos = listOf(
        Producto(1, "Laptop", 1500.00, 0.18, 15),
        Producto(2, "Smartphone", 800.00, 0.18, 30),
        Producto(3, "Tablet", 400.00, 0.18, 20),
        Producto(4, "Monitor", 250.00, 0.18, 25),
        Producto(5, "Teclado", 50.00, 0.18, 100),
        Producto(6, "Mouse", 30.00, 0.18, 120),
        Producto(7, "Impresora", 120.00, 0.18, 10),
        Producto(8, "Disco Duro Externo", 100.00, 0.18, 50),
        Producto(9, "Memoria USB", 20.00, 0.18, 200),
        Producto(10, "Cámara Web", 70.00, 0.18, 40),
        Producto(11, "Proyector", 600.00, 0.18, 5),
        Producto(12, "Auriculares", 45.00, 0.18, 75),
        Producto(13, "Altavoces", 90.00, 0.18, 60),
        Producto(14, "Micrófono", 65.00, 0.18, 80),
        Producto(15, "Router WiFi", 85.00, 0.18, 50),
        Producto(16, "Smartwatch", 220.00, 0.18, 35),
        Producto(17, "Televisor", 1100.00, 0.18, 8),
        Producto(18, "Consola de Videojuegos", 500.00, 0.18, 12),
        Producto(19, "Cargador Portátil", 35.00, 0.18, 150),
        Producto(20, "Batería Externa", 45.00, 0.18, 100)
    )

    val cliente = Cliente(1, "", "", "")
    print("Ingresa tu nombre: ")
    cliente.Nombre = readLine().orEmpty()
    print("Ingresa tu telefono: ")
    cliente.Telefono = readLine().orEmpty()
    print("Ingresa tu correo: ")
    cliente.Correo = readLine().orEmpty()

    var orden = Orden(
        Codigo = 1,
        cliente = cliente,
        TipoComprobante = "Factura",
        FechaVenta = Date(),
        Estado = EstadoOrden.PROCESANDO,
        MontoTotal = 0.0
    )

    var _MontoTotal = 0.0
    val ListaDetalle = mutableListOf<DetalleVenta>()
    var n = 0

    while (n == 0) {
        println("Elige un producto: ")
        productos.forEach { producto ->
            println("${producto.Codigo}. ${producto.Nombre} - Precio: ${producto.Precio} - Stock: ${producto.Stock}")
        }

        print("Producto (Codigo): ")
        val codigoProducto = readLine()?.toIntOrNull()
        val producto = productos.find { it.Codigo == codigoProducto }

        if (producto != null) {
            print("Cantidad: ")
            val cantidad = readLine()?.toIntOrNull() ?: 0

            if (producto.Stock >= cantidad) {
                producto.Stock -= cantidad
                val detalle = DetalleVenta(
                    Codigo = ListaDetalle.size + 1,
                    orden = orden,
                    producto = producto,
                    Cantidad = cantidad,
                    Descuento = 0.0
                )

                _MontoTotal += cantidad * producto.Precio
                ListaDetalle.add(detalle)
            } else {
                println("Stock insuficiente para el producto ${producto.Nombre}. Stock disponible: ${producto.Stock}")
            }
        } else {
            println("Producto no encontrado.")
        }

        println("¿Quieres comprar otro producto? 1. Si 2. No")
        if (readLine() == "2") {
            n = 1
        }
    }

    orden.MontoTotal = _MontoTotal
    println("Elige un tipo de pago:")
    println("1. Tarjeta de crédito")
    println("2. Efectivo")
    println("3. Cheque")

    val tipoPago = readLine()?.toIntOrNull()
    when (tipoPago) {
        1 -> {
            print("Número de tarjeta: ")
            val numero = readLine().orEmpty()
            print("Fecha de caducidad: ")
            val caducidad = readLine().orEmpty()
            print("Tipo (VISA o MASTERCARD): ")
            val tipo = readLine().orEmpty()
            orden.pagos.add(Pago.TarjetaCredito(numero, caducidad, tipo))
        }
        2 -> {
            print("Moneda: ")
            val moneda = readLine().orEmpty()
            orden.pagos.add(Pago.Efectivo(moneda))
        }
        3 -> {
            print("Nombre del cheque: ")
            val nombre = readLine().orEmpty()
            print("Entidad bancaria: ")
            val entidadBancaria = readLine().orEmpty()
            orden.pagos.add(Pago.Cheque(nombre, entidadBancaria))
        }
    }

    var saldoPendiente = orden.MontoTotal
    while (saldoPendiente > 0) {
        print("Ingresa el monto del pago: ")
        val montoPago = readLine()?.toDoubleOrNull() ?: 0.0
        if (orden.procesarPagoParcial(montoPago)) {
            saldoPendiente -= montoPago
        } else {
            println("Pago inválido.")
        }
    }

    orden.actualizarEstado(EstadoOrden.PAGADO)

    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    println("Orden de Venta: ${cliente.Nombre}")
    println("Cliente: ${cliente.Nombre}, Teléfono: ${cliente.Telefono}, Correo: ${cliente.Correo}")
    println("Fecha y hora: ${formato.format(orden.FechaVenta)}")
    ListaDetalle.forEach { detalle ->
        println("${detalle.producto?.Nombre} - Precio: ${detalle.producto?.Precio} - Cantidad: ${detalle.Cantidad}")
    }
    println("Monto Total: ${orden.MontoTotal}")
    println("Estado del Pedido: ${orden.Estado}")
}
