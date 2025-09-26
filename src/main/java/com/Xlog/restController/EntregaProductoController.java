package com.Xlog.restController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Xlog.clases.GpioModulo;

@RestController
@RequestMapping("/API/EntregaProducto")
public class EntregaProductoController {
	
	private static final Logger logger = LoggerFactory.getLogger(BienvenidoController.class);
	
	@GetMapping("/Entregando")
	public ResponseEntity<String> entregaProducto() {
		
		GpioModulo.apagarLuz();
		
		if (GpioModulo.check_puertaTrasera_cerrada()) {
			logger.info("" + GpioModulo.status_puertaTrasera());
			logger.info("-------Abriendo puerta Delantera-------");
			
			GpioModulo.ONLY_AbrirPuertaDelantera();
			GpioModulo.encenderLuz();
		}
		
				
		return ResponseEntity.ok("Sucess");
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
