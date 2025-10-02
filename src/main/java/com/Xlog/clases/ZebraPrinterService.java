package com.Xlog.clases;

import org.springframework.stereotype.Service;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;

/**
 * Servicio para encapsular la lógica de conexión e impresión Zebra.
 */
@Service
public class ZebraPrinterService {

	/**
     * Intenta conectar y enviar un comando ZPL de prueba a la impresora.
     * Si la conexión falla, devuelve un mensaje de error.
     * @param theIpAddress La dirección IP de la impresora Zebra.
     * @return Un mensaje de resultado de la operación (comienza con "Éxito" o "Error").
     */
	public String sendZplTest(String theIpAddress) {
        Connection thePrinterConn = null;
        String result = "";

        try {
            // 1. Instanciar y abrir la conexión. Si falla, salta a catch(ConnectionException)
            thePrinterConn = new TcpConnection(theIpAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT);
            thePrinterConn.open(); 
            
            // 2. Si la conexión fue exitosa, enviar datos.
            String zplData = "^XA^FO20,20^A0N,25,25^FDZebra Test OK^FS^XZ";
            thePrinterConn.write(zplData.getBytes());
            
            result = "Éxito REAL: Conectado y datos ZPL enviados a la IP " + theIpAddress;

        } catch (ConnectionException e) {
            
            // 3. Si la conexión falla, devolver un mensaje de error claro.
            result = "Error de Conexión: No se pudo conectar a la impresora en " + theIpAddress + 
                     ". Mensaje detallado: " + e.getMessage();
            
        } catch (Exception e) {
            // 4. Manejo de otros errores (p. ej., problemas con el hardware/SO).
            result = "Error CRÍTICO: Integración del SDK fallida o error inesperado. Mensaje: " + e.getMessage();
            
        } finally {
            // 5. Cerrar la conexión, asegurando que se liberen los recursos.
            if (thePrinterConn != null) {
                try {
                    thePrinterConn.close();
                } catch (ConnectionException e) {
                    // Ignorar error al cerrar
                }
            }
        }
        return result;
    }
}