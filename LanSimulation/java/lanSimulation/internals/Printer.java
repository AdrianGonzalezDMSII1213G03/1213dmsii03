package lanSimulation.internals;

import lanSimulation.Network;

public class Printer extends Node {

	public Printer(byte type, String name) {
		super(type, name);
		
	}
	
	public Printer(byte type, String name, Node nextNode) {
		super(type, name, nextNode);
	}

	/**
	Write a printable representation of #receiver on the given #buf.
	<p><strong>Precondition:</strong> isInitialized();</p>
	 * @param network TODO
	 * @param buf TODO
	*/
	public void printOn(Network network, StringBuffer buf) {
	    assert network.isInitialized();
	    Node currentNode = this;
	    do {
            buf.append("Printer ");
            buf.append(currentNode.name_);
            buf.append(" [Printer]");
	        buf.append(" -> ");
	        currentNode = send(currentNode);
	    } while (currentNode != this);
	    buf.append(" ... ");
	}

	/**
	Write a HTML representation of #receiver on the given #buf.
	 <p><strong>Precondition:</strong> isInitialized();</p>
	 * @param network TODO
	 * @param buf TODO
	 */
	public void printHTMLOn(Network network, StringBuffer buf) {
	assert network.isInitialized();
	
	buf.append("<HTML>\n<HEAD>\n<TITLE>LAN Simulation</TITLE>\n</HEAD>\n<BODY>\n<H1>LAN SIMULATION</H1>");
	Node currentNode = this;
	buf.append("\n\n<UL>");
	do {
	    buf.append("\n\t<LI> ");
	    buf.append("Printer ");
	    buf.append(currentNode.name_);
	    buf.append(" [Printer]");
	    buf.append(" </LI>");
	    currentNode = send(currentNode);
	} while (currentNode != this);
	buf.append("\n\t<LI>...</LI>\n</UL>\n\n</BODY>\n</HTML>\n");
	}

	/**
	Write an XML representation of #receiver on the given #buf.
	<p><strong>Precondition:</strong> isInitialized();</p>
	 * @param network TODO
	 * @param buf TODO
	*/
	public void printXMLOn(Network network, StringBuffer buf) {
	assert network.isInitialized();
	
	Node currentNode = this;
	buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<network>");
	do {
	    buf.append("\n\t");
	    buf.append("<printer>");
	    buf.append(currentNode.name_);
	    buf.append("</printer>");
	    currentNode = send(currentNode);
	} while (currentNode != this);
	buf.append("\n</network>");
	}

}
