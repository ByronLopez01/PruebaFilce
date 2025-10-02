package com.Xlog.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Xlog.clases.ZebraPrinterService;

/**
 * Endpoint REST para probar la conexión a la impresora Zebra.
 */
@RestController
@RequestMapping("/api/printer")
public class PrinterController {

    // Inyectamos nuestro servicio de conexión
    @Autowired
    private ZebraPrinterService printerService;

    // Clase interna para mapear el JSON de entrada de Postman
    private static class PrinterRequest {
        private String ipAddress;

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }

    @PostMapping("/test-tcp")
    public ResponseEntity<String> testTcpConnection(@RequestBody PrinterRequest request) {
        
        if (request.getIpAddress() == null || request.getIpAddress().trim().isEmpty()) {
            // Imprimir el error
            System.out.println("ERROR: Solicitud /test-tcp recibida sin IP."); 
            return ResponseEntity.badRequest().body("Error: Debe proporcionar la dirección IP de la impresora.");
        }

        String ipAddress = request.getIpAddress().trim();
        
        System.out.println("-------------------------------------------------");
        System.out.println("SOLICITUD RECIBIDA: POST /api/printer/test-tcp");
        System.out.println("IP de impresora a probar: " + ipAddress);
        System.out.println("-------------------------------------------------");
        
        String result = printerService.sendZplTest(ipAddress);

        // Imprimir el resultado del servicio
        System.out.println("RESULTADO de la prueba para " + ipAddress + ": " + result);
        
        if (result.startsWith("Éxito")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }
}