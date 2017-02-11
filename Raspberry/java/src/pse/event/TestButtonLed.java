package pse.event;

import pse.devices.Light;
import pse.devices.ObservableButton;

public class TestButtonLed {
	public static void main(String[] args) {
		Light led = new pse.devices.emu.Led(4);
		ObservableButton button = new pse.devices.emu.ObservableButton(17);
		// Light led = new pse.modulo_lab_4_2.devices.dio_impl.Led(4);
		// Light led = new pse.modulo_lab_4_2.devices.p4j_impl.Led(4);
		// Button button = new modulo_lab_4_2.devices.p4j_impl.Button(17);
		new ButtonLedController(button,led).start();
	}

}
