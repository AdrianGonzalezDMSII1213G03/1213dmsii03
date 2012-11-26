package lanSimulation.internals;

public class Printer extends Node {

	public Printer(byte type, String name) {
		super(type, name);
		
	}
	
	public Printer(byte type, String name, Node nextNode) {
		super(type, name, nextNode);
	}

}
