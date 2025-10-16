package com.Xlog.restController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Xlog.clases.GpioModulo;

@RestController
@RequestMapping("/API/EntregaProducto")
@CrossOrigin
public class EntregaProductoController {
	
	private static final Logger logger = LoggerFactory.getLogger(BienvenidoController.class);
	
	@GetMapping("/Entregando")
	public ResponseEntity<String> entregaProducto() {
		
		logger.info("Iniciando proceso. Esperando que se presione el botón...");
		
		long tiempoInicio = System.currentTimeMillis();
		long tiempoMaximoEspera = 30000; // 30 segundos de espera máxima
		boolean botonFuePresionado = false;
		
		// Bucle que espera por el botón, pero con un límite de tiempo.
		while (System.currentTimeMillis() - tiempoInicio < tiempoMaximoEspera) {
			if (GpioModulo.statusBotonConfirmacion()) {
				botonFuePresionado = true;
				break; // Salimos del bucle porque ya se presionó
			}
			// Pausa para no saturar la CPU
			try {
				Thread.sleep(200); // Comprueba 5 veces por segundo
			} catch (InterruptedException e) {
				logger.error("El hilo de espera fue interrumpido", e);
				break;
			}
		}
		
		if (botonFuePresionado) {
			
			logger.info("Botón presionado. Procediendo a cerrar la puerta trasera.");
			GpioModulo.ONLY_CerrarPuertaTrasera();
			
			GpioModulo.apagarLuz();
			
			if (GpioModulo.check_puertaTrasera_cerrada()) {
				logger.info("Puerta trasera confirmada como cerrada.");
				logger.info("-------Abriendo puerta Delantera-------");
				
				GpioModulo.ONLY_AbrirPuertaDelantera();
				GpioModulo.encenderLuz();
			} else {
				logger.warn("Fallo: La puerta trasera no se cerró correctamente después del comando.");
				return ResponseEntity.ok("Error: La puerta trasera no se cerró.");
			}
			
			return ResponseEntity.ok("Success");

		} else {
			// Si el bucle terminó por tiempo, el botón nunca se presionó.
			logger.warn("Timeout: El botón no fue presionado en {} segundos.", tiempoMaximoEspera / 1000);
			return ResponseEntity.ok("Error: Timeout, el boton no fue presionado.");
		}
	}

	@GetMapping("/Imprimir")
	public ResponseEntity<String> imprimirContacto(){
		
		//String zpl = GpioModulo.generarEtiqueta("Byron Lopez", "+5696337788");
		String zpl = GpioModulo.generarEtiquetaStilo("Byron Lopez", "+5696337788");
		
		GpioModulo.imprimirZPLEnZebra(zpl);
		return ResponseEntity.ok("");
	}
	
	
	@GetMapping("/TestSerie")
	public ResponseEntity<String> testPuertoserie(){
		
		double peso = GpioModulo.leerPuertoSerie3();
		
		return ResponseEntity.ok("Peso: "+peso);
		
	}
	@GetMapping("/TestBalanza")
	public ResponseEntity<String> testBalanza(){
		
		double peso = GpioModulo.leerPuertoEscuelamilitar();
		
		return ResponseEntity.ok("Peso: "+peso);
		
	}
}
