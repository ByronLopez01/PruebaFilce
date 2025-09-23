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

}


