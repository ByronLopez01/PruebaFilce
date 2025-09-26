package com.Xlog.restController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Xlog.clases.GpioModulo;



@RestController
@RequestMapping("/API/Bienvenido")
public class BienvenidoController {

	private static final Logger logger = LoggerFactory.getLogger(BienvenidoController.class);
	
	@GetMapping("/Ingreso/{codigo}")
	public ResponseEntity<String> IngresoCodigo(@PathVariable String codigo) {
		
		logger.info("Codigo Escaneado: " + codigo);
		
		if (GpioModulo.status_puertaDelantera()) {
			if (GpioModulo.status_puertaTrasera()) {
				logger.info("-------Abriendo puerta trasera-------");
				GpioModulo.ONLY_AbrirPuertaTrasera();
				GpioModulo.encenderLuz();
				

			} else {
				logger.info("------- Puerta trasera abierta prendiendo Luz...-------");
				GpioModulo.encenderLuz();
			}

		} else {
			
			logger.info("La puerta delantera esta abierta");
			logger.info("Cerrando puerta delantera");
			GpioModulo.ONLY_cerrarPuertaDelantera();
			if (GpioModulo.check_puertaDelantera_cerrada()) {
				logger.info("" + GpioModulo.status_puertaDelantera());
				logger.info("-------Abriendo puerta trasera-------");
				// GpioModulo.Esperarhasta_puertatrasera_Cerrada();
				GpioModulo.ONLY_AbrirPuertaTrasera();
				GpioModulo.encenderLuz();
			}
			
		}
		
		return ResponseEntity.ok("Sucess");
	}
	
	@GetMapping("/CerrarPuerta")
	public ResponseEntity<String> cerrarPuerta() {
		
		if(GpioModulo.status_puertaDelantera()==false) {
			
			logger.info("Cerrando puerta delantera...");
			
			GpioModulo.ONLY_cerrarPuertaDelantera();	
		}
		
		return ResponseEntity.ok("Sucess");
		
		
	}
}
