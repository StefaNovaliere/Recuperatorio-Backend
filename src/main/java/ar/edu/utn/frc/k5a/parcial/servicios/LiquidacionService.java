package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.TarjetaInexistenteException;
import ar.edu.utn.frc.k5a.parcial.modelo.Consumo;
import ar.edu.utn.frc.k5a.parcial.modelo.Tarjeta;

import java.io.IOException;
import java.util.List;

public interface LiquidacionService {

    /**
     * Genera UNA liquidación para una tarjeta en particular y para un mes/año en particular.
     * @param idTarjeta El ID (no el número) de la tarjeta cuya liquidación se va a generar
     * @param anio El Año del período de la liquidación
     * @param mes El mes del período de la liquidación
     * @return LiquidacionDTO con los datos de la liquidación generada
     * @Throws TarjetaInexistenteException si la tarjeta solicitada no existe
     */
    List<String> tarjetasConConsumoEnMoneda(String moneda, int anio, int mes);
    LiquidacionDTO generarLiquidacion(long idTarjeta, int anio, int mes) throws TarjetaInexistenteException;
    List<LiquidacionDTO> buscarLiquidacionPeriodo(int anio, int mes);
    long contarLiquidacionesPeriodo(int anio, int mes);
    double totalDescuentosDelPeriodo(int anio, int mes);
    double totalAPagarDelPeriodo(int anio, int mes);
    long tarjetasQueGastaronMasDe(double monto, int anio, int mes);
    long contarConsumosDeTarjeta(String numero, int anio, int mes);
    double totalConsumidoEnMoneda(String moneda, int anio, int mes);
    List<Consumo> monedasUsadasPorTarjeta(String numero, int anio, int mes);
    /**
     * Retorna una lista con el Número (NO el ID) de las tarjetas que no tienen una liquidación
     * para el Año y Mes especificados.
     * @param anio El año a consultar
     * @param mes El mes a consultar
     * @return Lista de números de tarjetas sin liquidaciones.
     */
    List<String> numerosSinLiquidar(int anio, int mes);

    /**
     * Realiza la liquidación de un lote de tarjetas, leyendo este lote de un archivo CSV
     * con los siguientes campos:
     * ID Tarjeta;Año;Mes
     *
     * @param rutaArchivo Ruta del archivo a procesar
     * @return lista con las liquidaciones procesadas.
     */

    List<LiquidacionDTO> liquidarLote(String rutaArchivo) throws IOException;

}
