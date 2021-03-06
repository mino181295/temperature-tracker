package pse.main;

import java.io.IOException;

import pse.common.BasicEventLoopController;
import pse.common.Event;
import pse.common.JSONController;
import pse.common.MsgType;
import pse.devices.ButtonPressed;
import pse.devices.Light;
import pse.devices.ObservableButton;
import pse.devices.ObservableTimer;
import pse.devices.Tick;

//Classe che incapsula il comportamento dell'event tracker in base all'evento e allo stato in cui si trova
public class EventTracker extends BasicEventLoopController {
	//Istanzio i due controller JSON che gestiscono l'aggiunta nel path indicato
	JSONController jsonTemperature = new JSONController(MainEventTracker.JSON_FOLDER, MsgType.TEMPERATURE);
	JSONController jsonAlarm = new JSONController(MainEventTracker.JSON_FOLDER, MsgType.ALARM);
	
	private Light greenLed1;
	private Light greenLed2;
	private Light alarmLed;
	//Variabile che conferma il funzionamento della comunicazione
	private boolean ackRecived = false;
	//Timer per l'evento di Tick
	private ObservableTimer timer;
	//Bottone per l'evento di pressione 
	private ObservableButton button;

	private enum State {CONNECTED, NOT_CONNECTED};
	private State currentState;

	public EventTracker(Light led1, Light led2, Light led3, ObservableButton button){

		this.greenLed1 = led1;
		this.greenLed2 = led2;
		this.alarmLed  = led3;
		
		this.button = button;
		button.addObserver(this);

		this.timer = new ObservableTimer();		
		timer.addObserver(this);
		//Ogni cinque secondi verifica che gli sia arrivato almeno un ack altrimenti considera la connessione non funzionante
		timer.start(5000);
		
		currentState = State.NOT_CONNECTED;
	}

	protected void processEvent(Event ev){
		switch (currentState){
		case CONNECTED:
			try {
				//Messaggio di temperatura che prende un valore
				if (ev instanceof TemperatureMsg){
					TemperatureMsg tempMsg = (TemperatureMsg) ev;
					float currentTemp = tempMsg.getValue();
					jsonTemperature.append(currentTemp);
					jsonTemperature.write();
					System.out.println("Message arrived : temperature ( "+ currentTemp +" )");					
					blinkLed(greenLed2);				
				} else if (ev instanceof ConnectionMsg){
					ackRecived = true;
					//System.out.println("Message recived : ack");
				} else if (ev instanceof PresenceMsg){
					//Messaggio di presenza rilevata che può essere vera o falsa se è un allarme o no
					PresenceMsg presenceMsg = (PresenceMsg) ev;				
					jsonAlarm.append(presenceMsg.isAlarm());
					jsonAlarm.write();
					
					if (presenceMsg.isAlarm()){
						alarmLed.switchOn();
						System.out.println("Alarm recived : true");
					} else {
						System.out.println("Alarm recived : false");
					}
					blinkLed(greenLed2);
				} else if (ev instanceof Tick){
					if (!ackRecived){
						currentState = State.NOT_CONNECTED;
						greenLed1.switchOff();
						greenLed2.switchOff();
						alarmLed.switchOff();
						//System.out.println("Ack not recived. Not working.");
					} else {
						//System.out.println("Ack recived. Working.");						
					}
					ackRecived = false;
				} else if (ev instanceof ButtonPressed){
					alarmLed.switchOff();
					//System.out.println("Button pressed : alarm stopped. ");
					break;
				}
			} catch (IOException ex){
				ex.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case NOT_CONNECTED:
			//Fase di connessione
			try {
				if (ev instanceof ConnectionMsg){
					//System.out.println("Message recived: working and connected.");
					currentState = State.CONNECTED;
					greenLed1.switchOn();
					ackRecived = true;
				}
			} catch (IOException ex){
				ex.printStackTrace();
			}
			break;
		}
	}
	
	private void blinkLed(Light led) throws InterruptedException, IOException{
		led.switchOn();
		waitFor(50);
		led.switchOff();	
	}
	
	private void waitFor(long ms) throws InterruptedException{
		Thread.sleep(ms);
	}
}
