import java.util.*;
//poate fi si abstracta
class Room { //INVOKER (nu putem tine logica pentru LIGHTS in base-clase ROOM, deoarece poate avem si alte lumini ce nu sunt in camere, 
			//si astfel am avea cod duplicat(actiunea este EXTRASA intr-un obiect)
	
	private Command command; //retinem COMANDA si pe ea apelam .execute()
	//putem retine (List<Command>) pentru a executa mai multe
	
	public Room() { //poate fi primita si la CONSTRUCTOR
	}
	
	public void setCommand(Command command) { 
		this.command = command;
	}
	
	public void executeCommand() { //de aici ON/OFF pt lumina
		command.execute();
	}
}

class Bathroom extends Room { 
	//code extra
}

class Kitchen extends Room { 
	//code extra
}

class Bedroom extends Room { 
	private FloorLamp floorLamp;
	
	public Bedroom() { 
		this.floorLamp = new FloorLamp();
	}
}

class FloorLamp { 
	private Command command; 
	
	public FloorLamp() { 
	}
	
	public void setCommand(Command command) {
		this.command = command;
	}
	
	public void executeCommand() { //CALL pe comanda, nu retii obiectul pe care se executa actiunea
		command.execute();
	}
}







class Light { //RECEIVER (aici scrii LOGICA) 
	private boolean switchedOn;
	
	public void switchLights() { //TINEM LOGICA COMENZII in clasa pe care o executam
		switchedOn = !switchedOn;
	}
}

interface Command { 
	void execute(); //asa este STANDARD, doar 1 metoda de a executa, poate UNDO daca ai nevoie
}

class SwitchLightsCommand implements Command { 
	private Light light; 
	
	public SwitchLightsCommand(Light light) { //AICI trm param un obiect
										//deoarece vrem sa EXECUTAM COMANDA fix pe aceta, nu altul
		this.light = light;
	}
	
	public void execute() { 
		light.switchLights();
	}
}

class CloseDoorCommand implements Command { 
	//retinem OBIECT 
	
	//constructor(OBIECT pe care executam)
	
	public void execute() { 
		//apelam metoda pe obiectul ATRIBUT
	}
}








class House { 
	private List<Room> rooms; 
	
	public House() { 
		this.rooms = new ArrayList<>();
	}
	
	public void addRoom(Room room) { 
		rooms.add(room);
	}
}


public class Main { 
	public static void main(String[] args) { 
		System.out.println("MERGE");
	}
}