package com.Xlog.clases;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.Doc;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.exception.UnsupportedPinPullResistanceException;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import com.pi4j.wiringpi.GpioUtil;

import jssc.SerialPort;
import jssc.SerialPortException;



//si hay alguna falla desde hoy 10/01/24 es por checktemperatura 
//comentar @component y compilar 
@Component
public class GpioModulo {
	final static Logger logger = LoggerFactory.getLogger(GpioModulo.class);

	private static final int TIEMPO_MAXIMO_ACTIVACION = 1; // Definir el tiempo máximo en segundos
	private static Timer timer;

	private static boolean comandoEjecutado = false;


	public static void encenderLuz() {

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pinLuz = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07);
		pinLuz.low();
		gpio.unprovisionPin(pinLuz);
		gpio.shutdown();

	}

	public static void apagarLuz() {

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pinLuz = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07);

		pinLuz.high();

		gpio.unprovisionPin(pinLuz);
		gpio.shutdown();

	}




	public static void ejecutarComandoUbuntu(String comando) {
		try {
			// Ejecuta el comando en el terminal de Ubuntu
			Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", comando });
			System.out.println("Comando ejecutado correctamente.");
		} catch (IOException e) {
			System.out.println("Error al ejecutar el comando: " + e.getMessage());
		}
	}

	public static void ejecutarScript(String rutaScript) {
		try {
			// Crea el proceso para ejecutar el script
			Process proceso = Runtime.getRuntime().exec("sh " + rutaScript);

			// Lee la salida del proceso
			BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
			String linea;
			while ((linea = reader.readLine()) != null) {
				System.out.println(linea);
			}

			// Espera a que el proceso termine
			int exitCode = proceso.waitFor();

			// Imprime el código de salida del proceso
			System.out.println("El script se ejecutó con código de salida: " + exitCode);

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}


	public static void AccionarCintaTransportadora() {
		logger.info("---------------Accionando Cinta -------------");
		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput myPinCintaTransportadora = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21);
		myPinCintaTransportadora.low();

		gpio.unprovisionPin(myPinCintaTransportadora);
		gpio.shutdown();

	}

	public static void DetenerCintaTransportadora() {

		logger.info("----------- Deteniendo Cinta --------------");

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput myPinCintaTransportadora = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21); // PIN NUMBER
		// (AMARILLO)
		// "My LED", // PIN FRIENDLY NAME (optional)
		// PinState.LOW); // PIN STARTUP STATE (optional)

		// myPinCintaTransportadora.toggle();
		myPinCintaTransportadora.high();
		gpio.unprovisionPin(myPinCintaTransportadora);
		gpio.shutdown();

	}

	public static void ONLY_AbrirPuertaTrasera() {

		try {

			final GpioController gpio = GpioFactory.getInstance();

			GpioPinDigitalOutput PinPuertaTrasera = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, // PIN NUMBER
																										// (AMARILLO)
					"My LED", // PIN FRIENDLY NAME (optional)
					PinState.LOW); // PIN STARTUP STATE (optional)

			// Thread.sleep(5000);

			PinPuertaTrasera.isLow();

			gpio.shutdown();

			gpio.unprovisionPin(PinPuertaTrasera);

		} catch (Exception e) {
			System.out.println("Nose puede usar GPIO CONTROL en este equipo");
		}

	}

	public static void ONLY_CerrarPuertaTrasera() {

		try {

			final GpioController gpio = GpioFactory.getInstance();

			GpioPinDigitalOutput PinPuertaTrasera = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, // PIN NUMBER
																										// (AMARILLO)
					"My LED", // PIN FRIENDLY NAME (optional)
					PinState.HIGH); // PIN STARTUP STATE (optional)

			// Thread.sleep(1000);
			PinPuertaTrasera.isLow();

			gpio.unprovisionPin(PinPuertaTrasera);
			gpio.shutdown();

		} catch (Exception e) {
		
			System.out.println("Nose puede usar GPIO CONTROL en este equipo");
		}

	}

	

	public static void pruebaMetodo(int contador) {
		int cont = 0;
		while (cont != contador) {
			cont++;
			System.out.println("Ejecuntando prueba");

		}

	}

	public static void pruebaCerrarpuerta() {

		System.out.println("Cerrando");
		if (timer != null) {
			timer.cancel();
		}
	}

	public static void ONLY_AbrirPuertaDelantera() {

		try {
			final GpioController gpio = GpioFactory.getInstance();

			GpioPinDigitalOutput PinPuertaDelantera = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, // PIN NUMBER
																										// (AMARILLO)
					"My LED", // PIN FRIENDLY NAME (optional)
					PinState.LOW); // PIN STARTUP STATE (optional)

			// Thread.sleep(1000);
			PinPuertaDelantera.isHigh();

			gpio.unprovisionPin(PinPuertaDelantera);
			gpio.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}

	}

	public static void ONLY_cerrarPuertaDelantera() {

		try {
			final GpioController gpio = GpioFactory.getInstance();

			GpioPinDigitalOutput PinPuertaDelantera = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, // PIN NUMBER
																										// (AMARILLO)
					"My LED", // PIN FRIENDLY NAME (optional)
					PinState.HIGH); // PIN STARTUP STATE (optional)

			PinPuertaDelantera.isLow();

			gpio.unprovisionPin(PinPuertaDelantera);
			gpio.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static boolean status_puertaDelantera() {

		final GpioController gpio = GpioFactory.getInstance();
//original 15
		//cuidadempresrial 05
		GpioPinDigitalInput pinLOw = gpio.provisionDigitalInputPin(RaspiPin.GPIO_15); // PIN RESISTANCE (optional)

		boolean status = false;

		if (pinLOw.isHigh()) {

			logger.info("Puerta delantera cerrada");
			// System.out.println(PinConfirmacionPuertaDelantera.isLow());
			status = true;

		} else {

			logger.info("Puerta delantera abierta");
			// System.out.println(PinConfirmacionPuertaDelantera.isHigh());
			status = false;
			// break;

		}

		gpio.unprovisionPin(pinLOw);
		gpio.shutdown();

		return status;
	}

	public static boolean status_puertaTrasera() {

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalInput pinLOw = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29); // PIN RESISTANCE
		// (optional)

		// GpioPinDigitalInput pinHigh =
		// gpio.provisionDigitalInputPin(RaspiPin.GPIO_06);
		boolean status = false;

		// Thread.sleep(1000);
		if (pinLOw.isHigh()) {

			logger.info("Puerta trasera cerrada");

			status = true;

		} else {

			// if (pinHigh.isHigh()) {

			logger.info("Puerta trasera abierta");
			// System.out.println(pinHigh.isHigh());
			status = false;

		}
		gpio.unprovisionPin(pinLOw);
		gpio.shutdown();
		return status;
	}

	public static boolean status_puertaTraseraAbierta() {

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalInput pinLOw = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28); // PIN RESISTANCE

		boolean status = false;

		if (pinLOw.isHigh()) {

			status = true;

		} else {

			status = false;

		}
		gpio.unprovisionPin(pinLOw);
		gpio.shutdown();
		return status;

	}

	public static void verificarPuertaTraseraAbierta() {
		Timer temporizador = new Timer();
		int maximoSegundos = 50;
		
		while (!status_puertaTraseraAbierta())
			;
		temporizador.cancel();
		temporizador.purge();
	}

	public static boolean check_puertaDelantera_cerrada() {

		Timer temporizador = new Timer();
		int maximoSegundos = 50;
		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalInput pinLow = gpio.provisionDigitalInputPin(RaspiPin.GPIO_15);

		boolean status = false;

		// logger.info("Antes del while check estado");
		// logger.info("Estado del pin " + pinLow.getState());
		int contador = 0;

		
		while (contador < 2) {
			if (!pinLow.isLow()) {
				contador++;
				if (contador == 2)
					status = true;
			} else {

				contador = 0;

			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		temporizador.cancel();
		temporizador.purge();
		gpio.unprovisionPin(pinLow);
		gpio.shutdown();

		return status;
	}

	
	public static double temperatura() {

		String temperatura;
		double temperaturaDouble = 0;
		try {
			temperatura = obtenerTemperaturaStatic();
			temperaturaDouble = Double.parseDouble(temperatura);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return temperaturaDouble;

	}

	private static String obtenerTemperaturaStatic() throws IOException {
		// Para Linux
		Process process = Runtime.getRuntime().exec("/usr/bin/vcgencmd measure_temp");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("temp=")) {
					return line.substring(5, line.indexOf("'"));
				}
			}
		}
		return "N/A";
	}

	private String obtenerTemperatura() throws IOException {
		// Para Linux
		Process process = Runtime.getRuntime().exec("/usr/bin/vcgencmd measure_temp");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("temp=")) {
					return line.substring(5, line.indexOf("'"));
				}
			}
		}
		return "N/A";

		/* Windows */
		/*
		 * try { Process process = Runtime.getRuntime().exec(
		 * "wmic /namespace:\\\\root\\cimv2 path Win32_PerfFormattedData_Counters_ThermalZoneInformation get Temperature"
		 * ); try (InputStream inputStream = process.getInputStream(); BufferedReader
		 * reader = new BufferedReader(new InputStreamReader(inputStream))) { String
		 * line; while ((line = reader.readLine()) != null) {
		 * 
		 * try { int temperatureValue = Integer.parseInt(line.trim());
		 * //System.out.println( + temperatureValue); String celsius =
		 * agregarComa(temperatureValue); return celsius + "°C"; } catch
		 * (NumberFormatException e) { // Ignore lines that are not valid integers } } }
		 * } catch (IOException e) { e.printStackTrace(); // Handle the exception
		 * according to your needs }
		 * 
		 * return "N/A";
		 */

	}

	private static String agregarComa(int temperatura) {

		String temp = Integer.toString(temperatura);

		if (temp.length() > 2) {

			StringBuilder sb = new StringBuilder(temp);

			sb.insert(2, ',');

			return sb.toString();
		}
		return temp;
	}

	public static boolean check_puertaTrasera_cerrada() {

		Timer temporizador = new Timer();
		int maximoSegundos = 30;
		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalInput pinLow = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29);

		boolean status = false;

		int contador = 0;

		while (contador < 2) {
			if (!pinLow.isLow()) {

				contador++;
				if (contador == 2)
					status = true;
			} else {

				contador = 0;

			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		temporizador.cancel();
		temporizador.purge();
		gpio.unprovisionPin(pinLow);
		gpio.shutdown();

		return status;

	}

	

	public static void desactivarClasificador() throws InterruptedException {

		logger.info("------------------- Desactivando Clasificador ------------------");

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pinClasificador = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25);

		pinClasificador.high();

		gpio.unprovisionPin(pinClasificador);
		gpio.shutdown();
	}

	public static void accionarClasificador() throws InterruptedException {

		logger.info("--------------------- Accionando Clasificador ------------------");

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pinClasificador = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25);

		pinClasificador.low();

		gpio.unprovisionPin(pinClasificador);

		gpio.shutdown();

		System.out.println("Accionando clasificador");

	}

	public static void accionarRodillos() {

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pinClasificador = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);

		logger.info("-------------Activando Sorter--------");
		pinClasificador.low();

		gpio.unprovisionPin(pinClasificador);

		gpio.shutdown();

		System.out.println("Accionando Rodillos");

	}

	public static void desactivarRodillos() {

		final GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pinClasificador = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);
		logger.info("-------------Desactivando Sorter--------");
		pinClasificador.high();

		gpio.unprovisionPin(pinClasificador);
		gpio.shutdown();
	}
	
	
	
	/* Implementar metodo valido para imprimir en impresora zebra */
	public static void imprimirZPLEnZebra(String zplResponse) {
		if (zplResponse == null || zplResponse.isEmpty()) {
			logger.error("Respuesta ZPL vacía o nula");
			return;
		}

		try {
			// limpiar zpl
			String zpl = extraerZPLDeRespuesta(zplResponse);
			
			zpl = ajustarZPLParaCorteManual(zpl);
			
			//String modoDirectThermal = "^XA\n^MTD\n^XZ\n";
	        //String zplConModo = modoDirectThermal + zpl;
			// imprimir etiqueta
			imprimirZPLCrudo(zpl);

			logger.info("ZPL enviado a impresora Zebra correctamente");
		} catch (Exception e) {
			logger.error("Error al imprimir en Zebra: {}", e.getMessage());
		}
	}

	/**
	 * Metodo que extrae el ZPL del formato JSON ["..."]
	 */
	private static String extraerZPLDeRespuesta(String jsonResponse) {
		
		logger.info("ZPL antes de limpiar " + jsonResponse);
		String limpio = jsonResponse.replaceAll("^\\[\"|\"\\]$", "");
		
	    // Decodificar secuencias de escape si las hay
	    limpio = limpio.replace("\\n", "\n").replace("\\\"", "\"");
	    logger.info("ZPL limpio " + limpio);
	    return limpio;
	}

	/**
	 * Metodo que envia el ZPL directamente a la impresora
	 */
	private static void imprimirZPLCrudo(String zpl) throws IOException {
		 try {
		        byte[] zplBytes = zpl.getBytes("US-ASCII");

		        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		        Doc doc = new SimpleDoc(zplBytes, flavor, null);

		        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		        PrintService zebra = null;
		        for (PrintService service : services) {
		            if (service.getName().contains("ZDesigner ZD230")) { // cambia si el nombre es diferente en tu PC
		                zebra = service;
		                break;
		            }
		        }

		        if (zebra != null) {
		           
		        	DocPrintJob job = zebra.createPrintJob();
		            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
		            attrs.add(new Copies(1));
		            job.print(doc, attrs);

		            logger.info("ZPL enviado correctamente a la impresora Zebra por USB.");
		        } else {
		            logger.error("No se encontró la impresora Zebra ZD230 instalada en el sistema.");
		        }
		    } catch (PrintException e) {
		        logger.error("Error al imprimir en Zebra: {}", e.getMessage());
		    }
	}

	
	private static String ajustarZPLParaCorteManual(String zpl) {
	    // 1. Reemplazar ^MMC (Cutter) por ^MMT (Tear Off)
	    zpl = zpl.replaceAll("\\^MMC", "^MMT");
	    
	    // 2. Asegurar que ^PQ (Print Quantity) tenga el parámetro de corte correcto
	    zpl = zpl.replaceAll("\\^PQ[^\\^]*", "^PQ1,0,1,N");  // N = No rebobinar
	    
	    // 3. Opcional: Añadir comandos de calibración si es necesario
	    if (!zpl.contains("^MC")) {
	        zpl = zpl.replaceFirst("\\^XA", "^XA\n^MCY");  // ^MCY = Calibrar al inicio
	    }
	    
	    logger.info("ZPL modificado para corte manual: " + zpl);
	    return zpl;
	}

	public static String generarEtiqueta(String nombre, String telefono) {
	    return "^XA\n" +
	           "^FO50,50^A0N,40,40^FDNombre: " + nombre + "^FS\n" +
	           "^FO50,100^A0N,40,40^FDContacto: " + telefono + "^FS\n" +
	           "^XZ";
	}
	
	public static String generarEtiquetaStilo(String nombre, String telefono) {
	    return "^XA\n" +
	           // Cuadro 
	           "^FO30,30^GB540,140,4^FS\n" + 

	           // Nombre
	           "^FO50,50^A0N,50,50^FDNombre:^FS\n" +
	           "^FO250,50^A0N,50,50^FD" + nombre + "^FS\n" +

	           // Contacto
	           "^FO50,120^A0N,40,40^FDContacto:^FS\n" +
	           "^FO200,120^A0N,40,40^FD" + telefono + "^FS\n" +

	           // QR centrado 
	           "^FO200,300^BQN,2,10^FDLA,https://www.xlog.com^FS\n" +

	           // Pie de página
	           "^FO0,720^A0N,50,50^F1000,1,0,C,0^FDXlog^FS\n" +
	           "^XZ";
	}

	
	public static double leerPuertoSerie3() {
	    double peso = 0;
	    boolean pesoValido = false;

	    SerialPort serialPort = null;

	    try {
	        serialPort = new SerialPort("/dev/ttyUSB0");
	        serialPort.openPort();
	        serialPort.setParams(
	                SerialPort.BAUDRATE_9600,
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE
	        );

	        int tiempoMaximoEspera = 10000;
	        long tiempoInicio = System.currentTimeMillis();

	        while (!pesoValido && (System.currentTimeMillis() - tiempoInicio) < tiempoMaximoEspera) {
	            if (serialPort.getInputBufferBytesCount() > 0) {
	                byte[] buffer = serialPort.readBytes(serialPort.getInputBufferBytesCount());
	                String line = new String(buffer, StandardCharsets.US_ASCII);
	                System.out.println("RAW recibido: " + line);

	                // Filtrar solo dígitos y punto
	                line = line.replaceAll("[^\\d.]", "");
	                System.out.println("Datos filtrados: " + line);

	                // Intentar tomar el último número válido
	                try {
	                    if (!line.isEmpty()) {
	                        peso = Double.parseDouble(line);
	                        pesoValido = true;
	                        System.out.println("Peso válido encontrado: " + peso);
	                    }
	                } catch (NumberFormatException e) {
	                    System.out.println("No se pudo parsear el peso: " + line);
	                }
	            }

	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }

	    } catch (SerialPortException ex) {
	        System.out.println("Error al leer datos del puerto serial: " + ex.getMessage());
	    } finally {
	        try {
	            if (serialPort != null && serialPort.isOpened()) {
	                serialPort.closePort();
	            }
	        } catch (SerialPortException e) {
	            e.printStackTrace();
	        }
	    }

	    if (!pesoValido) {
	        peso = 0.32;
	        System.out.println("Murió la pesa :(");
	    }

	    return peso;
	}


	public static double leerPuertoEscuelamilitar() {
	    double peso = 0;
	    boolean pesoValido = false;

	    SerialPort serialPort = null;

	    try {
	        serialPort = new SerialPort("/dev/ttyUSB0");
	        serialPort.openPort();
	        serialPort.setParams(
	                SerialPort.BAUDRATE_9600,
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE
	        );

	        int tiempoMaximoEspera = 10000;
	        long tiempoInicio = System.currentTimeMillis();

	        Pattern patronPeso = Pattern.compile("(-?\\d+\\.\\d+)k"); 
	        // Captura números con decimales seguidos de 'k'

	        while (!pesoValido && (System.currentTimeMillis() - tiempoInicio) < tiempoMaximoEspera) {
	            if (serialPort.getInputBufferBytesCount() > 0) {
	                byte[] buffer = serialPort.readBytes(serialPort.getInputBufferBytesCount());
	                String line = new String(buffer, StandardCharsets.US_ASCII);
	                System.out.println("RAW recibido: " + line);

	                Matcher matcher = patronPeso.matcher(line);
	                if (matcher.find()) {
	                    try {
	                        peso = Double.parseDouble(matcher.group(1));
	                        pesoValido = true;
	                        System.out.println("Peso válido encontrado: " + peso + " kg");
	                    } catch (NumberFormatException e) {
	                        System.out.println("No se pudo parsear el peso en: " + matcher.group(1));
	                    }
	                } else {
	                    System.out.println("No se encontró número con formato en la línea");
	                }
	            }

	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }

	    } catch (SerialPortException ex) {
	        System.out.println("Error al leer datos del puerto serial: " + ex.getMessage());
	    } finally {
	        try {
	            if (serialPort != null && serialPort.isOpened()) {
	                serialPort.closePort();
	            }
	        } catch (SerialPortException e) {
	            e.printStackTrace();
	        }
	    }

	    if (!pesoValido) {
	        peso = 0.32; // Valor fallback
	        System.out.println("Murió la pesa :(");
	    }

	    return peso;
	}
	
	
	public static double leerPuertoSerieDebug() {
		 double peso = 0;
		    boolean pesoValido = false;

		    SerialPort serialPort = null;

		    try {
		        serialPort = new SerialPort("/dev/ttyUSB0");
		        serialPort.openPort();
		        serialPort.setParams(
		                SerialPort.BAUDRATE_9600,
		                SerialPort.DATABITS_8,
		                SerialPort.STOPBITS_1,
		                SerialPort.PARITY_NONE
		        );

		        int tiempoMaximoEspera = 10000; // 10 segundos
		        long tiempoInicio = System.currentTimeMillis();

		        while (!pesoValido && (System.currentTimeMillis() - tiempoInicio) < tiempoMaximoEspera) {
		            if (serialPort.getInputBufferBytesCount() > 0) {
		                byte[] buffer = serialPort.readBytes(serialPort.getInputBufferBytesCount());
		                String raw = new String(buffer, StandardCharsets.US_ASCII);
		                System.out.println("RAW recibido: " + raw);

		                // Regex: = dos letras o números + + + número decimal + opcional letra
		                Pattern pattern = Pattern.compile("=[A-Z0-9]{2}\\+([0-9]+\\.[0-9]+)[a-zA-Z]?");
		                Matcher matcher = pattern.matcher(raw);

		                if (matcher.find()) {
		                    String valor = matcher.group(1);
		                    try {
		                        peso = Double.parseDouble(valor);
		                        System.out.println("Peso válido encontrado: " + peso);
		                        pesoValido = true;
		                        break; // tomamos solo el primer peso válido
		                    } catch (NumberFormatException e) {
		                        System.out.println("Error parseando peso: " + valor);
		                    }
		                }
		            }

		            try {
		                Thread.sleep(100);
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		        }

		    } catch (SerialPortException ex) {
		        System.out.println("Error al leer datos del puerto serial: " + ex.getMessage());
		    } finally {
		        try {
		            if (serialPort != null && serialPort.isOpened()) {
		                serialPort.closePort();
		            }
		        } catch (SerialPortException e) {
		            e.printStackTrace();
		        }
		    }

		    if (!pesoValido) {
		        peso = 0.0; // valor por defecto si no se recibe peso válido
		        System.out.println("No se recibió peso válido en el tiempo de espera.");
		    }

		    return peso;
	}

	    
	public static double leerDatosBalanza() {

		double peso;


		leerPuertoSerie3();
		peso = leerPuertoSerie3();
		logger.info("Peso " + peso);
		return peso;

	}


}


